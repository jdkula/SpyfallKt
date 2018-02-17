package pw.jonak.spyfall.backend.Sql

import org.jetbrains.exposed.sql.Table

object Games: Table() {
    val code = text("code").primaryKey().uniqueIndex()
    val location = text("location").nullable()
    val spy_player = (integer("spy_player") references Players.id).nullable()
    val first_player = (integer("first_player") references Players.id).nullable()
    val total_time = long("total_time").nullable()
    val time_start = long("time_started").nullable()
    val pause_time = long("pause_time").nullable()
}