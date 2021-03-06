package pw.jonak.spyfall.backend.gameElements

import io.netty.util.internal.ConcurrentSet
import pw.jonak.spyfall.common.GameInformation
import pw.jonak.spyfall.common.LobbyInformation
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

const val spyRoleId = "spy"

class Game(val code: String, val gameLength: Duration = Duration.ofMinutes(8)) {
    private val _users = ConcurrentSet<User>()
    val users: Set<User> get() = _users

    private val _userRoleMap = ConcurrentHashMap<User, String>()
    val userRoleMap: Map<User, String> get() = _userRoleMap

    var latestLobbyInformationPacketId: Int = 0
        get() {
            synchronized(field) {
                field += 1
            }
            return field - 1
        }

    var location: Location? = null

    var startTime: Int? = null
        private set
    var pauseTime: Int? = null
        private set

    var firstPlayer: User? = null
        private set
    var spyPlayer: User? = null
        private set

    val gameHasStarted: Boolean get() = startTime != null
    val isPaused: Boolean get() = pauseTime != null

    override fun equals(other: Any?): Boolean =
            when (other) {
                is Game -> other.code == code
                else -> false
            }

    override fun hashCode(): Int = code.hashCode()

    fun start() {
        startTime = (System.currentTimeMillis() / 1000L).toInt()
        val theSpy = _users.randomValue()
        spyPlayer = theSpy
        _userRoleMap += theSpy to spyRoleId
        location = AllLocations.randomValue()
        val roles = location!!.roles.toMutableList()
        firstPlayer = _users.randomValue()
        _users.forEach {
            if (it != spyPlayer) {
                _userRoleMap += it to roles.removeAt(Random().nextInt(roles.size))
            }
            if (roles.isEmpty()) {
                roles.addAll(location!!.roles)
            }
        }
    }

    fun getLobbyInfo(user: User?): LobbyInformation {
        val usersList = users.map { it.userName }.toList()
        return LobbyInformation(code, usersList, latestLobbyInformationPacketId, getGameInfo(user, usersList))
    }

    fun getGameInfo(user: User? = null, usersList: List<String>): GameInformation? {
        return if (gameHasStarted) {
            val firstPlayerId = usersList.indexOf(firstPlayer?.userName)
            val isSpy = spyPlayer == user && user != null
            GameInformation(
                    startTime!!, // TODO: Worry about nullability
                    gameLength.seconds.toInt(),
                    isSpy,
                    firstPlayerId,
                    location!!.id,
                    userRoleMap[user]!!,
                    pauseTime
            )
        } else null
    }

    fun addUser(user: User) {
        _users += user
        if (gameHasStarted && user !in _userRoleMap.keys) {
            _userRoleMap += user to location!!.roles.randomValue()
        }
    }

    fun removeUser(user: User) {
        _users -= user
        user.currentGame = null
    }

    fun pause() {
        pauseTime = (Calendar.getInstance().timeInMillis / 1000L).toInt()
    }

    fun unpause() {
        val now = (Calendar.getInstance().timeInMillis / 1000L).toInt()
        pauseTime?.let {
            startTime = startTime?.plus(now - it)
        }
        pauseTime = null
    }

    fun stop() {
        _userRoleMap.clear()
        startTime = null
        pauseTime = null
        firstPlayer = null
        spyPlayer = null
        location = null
    }

    private fun <T> List<T>.randomValue(): T {
        val randomIndex = Random().nextInt(size)
        return this[randomIndex]
    }

    private fun <T> Set<T>.randomValue(): T {
        return this.toList().randomValue()
    }

    private fun <K, V> Map<K, V>.randomValue(): V {
        return this.values.toList().randomValue()
    }
}