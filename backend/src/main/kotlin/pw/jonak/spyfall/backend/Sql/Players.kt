package pw.jonak.spyfall.backend.Sql

import org.jetbrains.exposed.sql.Table

object Players: Table() {
    val id = integer("id").primaryKey().uniqueIndex().autoIncrement()
    val game = (text("game") references Games.code).nullable()
    val role = text("role").nullable()
    val expires_at = long("expires_at")
    val name = text("name")
}