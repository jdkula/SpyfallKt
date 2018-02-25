package pw.jonak.spyfall.backend

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import pw.jonak.spyfall.backend.Sql.Games
import pw.jonak.spyfall.backend.Sql.Players
import pw.jonak.spyfall.common.GameInformation
import pw.jonak.spyfall.common.UserRegistrationInformation
import java.sql.Connection
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.Date
import java.util.Random


object SqlHelper {
    const val USER_EXPIRATION_TIME: Long = 1
    /* const */ val USER_EXPIRATION_UNIT = ChronoUnit.DAYS
    const val DATABASE_URL = "jdbc:sqlite:test.sqlite3"
    const val DATABASE_DRIVER = "org.sqlite.JDBC"

    fun createTables() {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)

        transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            create(Games, Players)
        }
    }

    fun createUser(userName: String): UserRegistrationInformation {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)

        val id: Int = transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Players.insert {
                it[name] = userName
                it[expires_at] = getNewExpireTime()
            } get Players.id
        }!!

        return UserRegistrationInformation(id, userName)
    }

    fun renameUser(id: Int, userName: String) {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)
        transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Players.update({
                Players.id eq id
            }) {
                it[name] = userName
            }
        }
    }

    fun ensureRegistered(id: Int, userName: String): UserRegistrationInformation {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)
        val numPlayers: Int = transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Players.select {
                Players.id eq id
            }.count()
        }
        return if (numPlayers == 0) {
            createUser(userName)
        } else {
            renameUser(id, userName)
            UserRegistrationInformation(id, userName)
        }
    }

    fun renewPlayer(id: Int) {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)
        transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Players.update({ Players.id eq id }) {
                it[expires_at] = getNewExpireTime()
            }
        }
    }

    fun getGameInfo(gameCode: String, userId: Int? = null): GameInformation? {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)
        val userNames = ArrayList<String>()
        var gameHasStarted = false
        var startTime: Long? = null
        var pauseTime: Long? = null
        var totalTime: Long? = null
        var firstPlayer: Int? = null
        var isSpy: Boolean? = null
        var location: String? = null
        var role: String? = null

        val gameExists = transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            val row = Games.select {
                Games.code eq gameCode
            }.singleOrNull() ?: return@transaction false
            gameHasStarted = row[Games.time_start] != null
            startTime = row[Games.time_start]
            pauseTime = row[Games.pause_time]
            totalTime = row[Games.total_time]
            if (userId != null) {
                isSpy = row[Games.spy_player] == userId
                if (isSpy == false) {
                    location = row[Games.location]
                    role = Players.select { Players.id eq userId }.singleOrNull()?.get(Players.role)
                }
            }

            val firstPlayerId = Games.select { Games.code eq gameCode }.singleOrNull()?.get(
                    Games.first_player
            )

            Players.select {
                Players.game eq gameCode
            }.forEachIndexed { index, it ->
                        userNames.add(it[Players.name])
                        if (it[Players.id] == firstPlayerId) {
                            firstPlayer = index
                        }
                    }

            true

        }
        return if (gameExists) {
            GameInformation(
                    userId,
                    gameCode,
                    userNames.toList(),
                    gameHasStarted,
                    startTime,
                    pauseTime,
                    totalTime,
                    isSpy,
                    firstPlayer,
                    location,
                    role
            )
        } else null
    }

    fun joinGame(userId: Int, gameCode: String): GameInformation? {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)

        val gameInfo = getGameInfo(gameCode)
        var newRole: String?

        if (gameInfo != null) {
            transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
                val userLastGame = Players.select { Players.id eq userId }.singleOrNull()?.get(Players.game)
                if (gameInfo.game_has_started && gameInfo.location != null && userLastGame != gameInfo.game_code) {
                    val remainingRoles =
                            AllLocations[gameInfo.location!!]
                                    ?.roles
                                    ?.subtract(
                                            Players.select {
                                                Players.game eq gameCode
                                            }.toSet().map { it[Players.role] }
                                    )
                    val possibleRole = remainingRoles?.singleOrNull()
                    newRole = possibleRole ?: AllLocations[gameInfo.location!!]?.roles?.singleOrNull()
                }

                Players.update({ Players.id eq userId }) {
                    it[game] = gameCode
                }
            }
            return GameInformation(
                    gameInfo.user_id,
                    gameInfo.game_code,
                    gameInfo.user_names,
                    gameInfo.game_has_started,
                    gameInfo.start_time,
                    gameInfo.pause_time,
                    gameInfo.total_time,
                    gameInfo.is_spy,
                    gameInfo.first_player,
                    gameInfo.location,
                    gameInfo.role
            )
        } else return null
    }

    fun getUserName(userId: Int): String? {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)

        return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Players.select {
                Players.id eq userId
            }.limit(1).elementAt(0)[Players.name]
        }
    }

    fun userExists(userId: Int): Boolean {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)
        return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            return@transaction Players.select { Players.id eq userId }.singleOrNull() != null
        }
    }

    const val GAME_CODE_LENGTH: Long = 4

    fun createGame(): String {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)

        return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            var rc: String
            do {
                rc = randomCode()
                val hasCode = Games.select { Games.code eq rc }.singleOrNull() != null
            } while (hasCode)
            Games.insert {
                it[code] = rc
            }
            rc
        }
    }

    fun prunePlayers() {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)
        transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Players.deleteWhere { Players.expires_at less Date().time }
        }
    }

    fun pruneGames() {
        Database.connect(DATABASE_URL, DATABASE_DRIVER)

        transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
            Games.selectAll().forEach {
                val playersInGame: Int = Players.select { Players.game eq it[Games.code] }.count()
                if(playersInGame == 0) {
                    Games.deleteWhere { Games.code eq it[Games.code] }
                }
            }
        }
    }

    fun randomCode(): String {
        // Thanks Paul Hicks! https://stackoverflow.com/questions/46943860/idiomatic-way-to-generate-a-random-alphanumeric-string-in-kotlin
        val source = "abcdefghijklmnopqrstuvwxyz"
        return Random().ints(GAME_CODE_LENGTH, 0, source.length).toArray()
                .map(source::get)
                .joinToString("")
    }

    fun getNewExpireTime(): Long =
            Date().time + Duration.of(
                    USER_EXPIRATION_TIME,
                    USER_EXPIRATION_UNIT
            ).toMillis()
}