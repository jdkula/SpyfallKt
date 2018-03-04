package pw.jonak.spyfall.backend.storage

import pw.jonak.spyfall.backend.gameElements.Location
import java.io.File

fun Location.Companion.getAllLocations(): Map<String, Location> {
    return File("src/pw.jonak.spyfall.backend.main/resources/locations")
            .walkTopDown()
            .filter { it.isFile }
            .map { it.readText() }
            .map { Location.fromJson(it) }
            .filterNotNull()
            .map { it.id to it }
            .toMap()
}

val LocationStore = Location.getAllLocations()