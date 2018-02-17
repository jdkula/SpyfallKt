import org.jetbrains.exposed.sql.Table

object Games: Table() {
    val id = text("id").primaryKey().uniqueIndex()
    val location = text("location")
    val spy_player = integer("spy_player") references Players.id
    val first_player = integer("first_player") references Players.id
    val total_time = integer("total_time")
    val time_start = integer("time_started")
    val pause_time = integer("pause_time").nullable()
}

object Players: Table() {
    val id = integer("id").primaryKey().uniqueIndex().autoIncrement()
    val last_game = text("last_game").nullable()
    val expires_at = integer("expires_at")
    val name = text("name")
}