package pw.jonak.spyfall.backend.gameElements

import pw.jonak.spyfall.backend.storage.UserStore
import pw.jonak.spyfall.common.GameInformation
import java.time.Duration
import java.util.*

class Game(val code: String, val location: Location, val gameLength: Duration = Duration.ofMinutes(8)) {
    private val _users = HashSet<User>()
    val users: Set<User> get() = _users

    private val _userRoleMap = HashMap<User, String>()
    val userRoleMap: Map<User, String> get() = _userRoleMap

    var startTime: Long? = null
        private set
    var pauseTime: Long? = null
        private set

    var firstPlayer: User? = null
        private set
    var spyPlayer: User? = null
        private set

    val gameHasStarted: Boolean get() = startTime != null

    override fun equals(other: Any?): Boolean =
            when (other) {
                is Game -> other.code == code
                else -> false
            }

    override fun hashCode(): Int = code.hashCode()

    fun addUser(user: User) {
        if (user !in UserStore.users.values)
            throw IllegalStateException("Nonexistent user tried to join a game!")

        _users += user
    }

    fun start() {
        startTime = Calendar.getInstance().timeInMillis
    }

    fun getGameInfo(user: User? = null): GameInformation {
        val usersList = users.map { it.userName }.toList()
        val firstPlayerId = usersList.indexOf(firstPlayer?.userName)
        val isSpy = spyPlayer == user && user != null
        return GameInformation(
                user?.id,
                code,
                usersList,
                gameHasStarted,
                startTime,
                pauseTime,
                gameLength.toMillis(),
                isSpy,
                if (firstPlayerId == -1) null else firstPlayerId,
                if (isSpy || user == null || !gameHasStarted) null else location.id,
                if (isSpy || user == null) null else userRoleMap[user]
        )
    }

    fun removeUser(user: User) {
        _users.remove(user)
    }
}