package pw.jonak.spyfall.backend

import kotlinx.serialization.json.JSON
import java.io.File

class Location(val id: String, val roles: List<String>) {

    override fun toString(): String {
        return "pw.jonak.spyfall.backend.Location $id: $roles"
    }

    companion object {
        fun fromJson(file: File): Location? {
            return JSON.parse(file.readText())
        }

        fun getAllLocations(): Map<String, Location> {
            return File("src/pw.jonak.spyfall.backend.main/resources/locations").walkTopDown().filter { it.isFile }.map {
                fromJson(it)
            }.filterNotNull().map { it.id to it }.toMap()
        }
    }
}

val AllLocations = Location.getAllLocations()