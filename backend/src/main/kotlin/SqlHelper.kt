import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

fun createTables() {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")

    transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        create(Games, Players)
    }
}

fun createUser(userName: String): Int {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")

    return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        return@transaction Players.insert {
            it[name] = userName
            it[expires_at] = 10
        } get Players.id
    }
}

fun getUserName(userId: Int): String? {
    Database.connect("jdbc:sqlite:test.sqlite3", "org.sqlite.JDBC")

    return transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
        Players.select {
            Players.id eq userId
        }.limit(1).elementAt(0)[Players.name]
    }
}