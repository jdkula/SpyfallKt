import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.Date

// TODO: Clean up this file...

const val USER_EXPIRATION_TIME: Long = 1
/* const */ val USER_EXPIRATION_UNIT = ChronoUnit.DAYS

fun createTables() {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")

    transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        create(Games, Players)
    }
}

fun createUser(userName: String): UserRegistrationInformation {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")

    val id: Int = transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        Players.insert {
            it[name] = userName
            it[expires_at] = Date().time + Duration.of(USER_EXPIRATION_TIME, USER_EXPIRATION_UNIT).toMillis()
        } get Players.id
    }

    return UserRegistrationInformation(id, userName)
}

fun renameUser(id: Int, userName: String) {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")
    transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        Players.update({
            Players.id eq id
        }) {
            it[name] = userName
        }
    }
}

fun ensureRegistered(id: Int, userName: String) {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")
    val numPlayers: Int = transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        Players.select {
            Players.id eq id
        }.toSet().size
    }
    if(numPlayers == 0) {
        createUser(userName)
    } else {
        renameUser(id, userName)
    }
}

fun joinGame(userId: Int, gameCode: String): GameInformation {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")
    var userNames = ArrayList<String>()
    var gameHasStarted: Boolean = false
    var startTime: Long? = null
    var pauseTime: Long? = null
    var totalTime: Long? = null
    var isSpy: Boolean? = null
    var firstPlayer: Int? = null
    var location: String? = null
    var role: String? = null

    transaction {
        val row = Games.select {
            Games.code eq gameCode
        }.single()
        gameHasStarted = row[Games.time_start] != null
        startTime = row[Games.time_start]
        pauseTime = row[Games.pause_time]
        isSpy = row[Games.spy_player] == userId
        location = if(isSpy == true) null else row[Games.location]
        totalTime = row[Games.total_time]

        Players.select {
            Players.game eq gameCode
        }.forEach {
                userNames.add(it[Players.name])
            }

        if(gameHasStarted) {
            // Select an unused role, or send an error.
            TODO("Need to implement list of locations first...")
        }

        Players.update({ Players.id eq userId }) {
            it[game] = gameCode
        }
    }
    return GameInformation(userId, gameCode, userNames.toList(), gameHasStarted, startTime, pauseTime, totalTime, isSpy, firstPlayer, location, role)
}

fun getUserName(userId: Int): String? {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")

    return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        Players.select {
            Players.id eq userId
        }.limit(1).elementAt(0)[Players.name]
    }
}