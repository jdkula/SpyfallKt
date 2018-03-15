package pw.jonak.spyfall.backend.storage

import pw.jonak.spyfall.backend.gameElements.AllLocations
import pw.jonak.spyfall.backend.gameElements.Game
import pw.jonak.spyfall.common.GameInformation
import java.util.*

class GameStore(private val userStore: UserStore) {
    private val _games = HashMap<String, Game>()
    val games: Map<String, Game> get() = _games

    fun joinGame(userId: Int, gameCode: String): Game? {
        if (gameCode !in _games)
            return null

        return _games[gameCode]?.let { game ->
            userStore.users[userId]?.let { user ->
                game.addUser(user)
                game
            }
        }
    }

    fun leaveGame(userId: Int, gameCode: String) {
        _games[gameCode]?.let { game ->
            userStore.users[userId]?.let { user ->
                game.removeUser(user)
            }
        }
    }

    fun getGameInfo(gameCode: String, userId: Int? = null): GameInformation? {
        return _games[gameCode]?.let { game ->
            userStore.users[userId].let { user ->
                game.getGameInfo(user)
            }
        }
    }

    fun pruneGames(): Int {
        var gamesPruned = 0
        games.filter { it.value.users.isEmpty() }.forEach {
            gamesPruned += 1
            _games.remove(it.key)
        }
        return gamesPruned
    }

    fun createGame(): String {
        var rc: String
        do {
            rc = randomCode()
            val hasCode = rc in games
        } while (hasCode)

        _games += rc to Game(rc, AllLocations.randomValue())
        return rc
    }

    private fun <K, V> Map<K, V>.randomValue(): V {
        val random = Random().nextInt(this.values.size)
        return this.values.toList()[random]
    }

    private fun randomCode(): String {
        // Thanks Paul Hicks! https://stackoverflow.com/questions/46943860/idiomatic-way-to-generate-a-random-alphanumeric-string-in-kotlin
        val source = "abcdefghijklmnopqrstuvwxyz"
        return Random().ints(GAME_CODE_LENGTH, 0, source.length).toArray()
                .map(source::get)
                .joinToString("")
    }

    companion object {
        const val GAME_CODE_LENGTH: Long = 4
    }

}