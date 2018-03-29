package pw.jonak.spyfall.backend.gameElements

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import java.io.File

@Serializable
class Location(val id: String, val roles: List<String>) {

    override fun toString(): String {
        return "Location $id: $roles"
    }

    companion object {
        fun fromJson(json: String): Location? {
            return JSON.parse(json)
        }

        fun getAllLocations(): Map<String, Location> {
            return File("src/main/resources/locations")
                    .walkTopDown()
                    .filter { it.isFile }.map { fromJson(it.readText()) }
                    .filterNotNull()
                    .map { it.id to it }
                    .toMap()
        }
    }
}

val AllLocations = Location.getAllLocations()