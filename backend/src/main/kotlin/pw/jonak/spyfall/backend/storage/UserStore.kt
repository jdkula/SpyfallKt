package pw.jonak.spyfall.backend.storage

import pw.jonak.spyfall.backend.gameElements.User
import pw.jonak.spyfall.backend.thisSession
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class UserStore {
    private val _users = ConcurrentHashMap<Int, User>()
    val users: Map<Int, User> get() = _users

    fun createUser(userName: String): User {
        val newUser = User(User.totalUsersRegistered, userName)
        _users += newUser.id to newUser
        return newUser
    }

    fun getExistingUser(userId: Int): User? = _users[userId]

    fun ensureRegistered(userId: Int, userName: String, sessionId: Int): User =
            if (!_users.containsKey(userId) || sessionId != thisSession) {
                val newUser = createUser(userName)
                newUser
            } else {
                val user = _users[userId]!!
                user.userName = userName
                user
            }

    fun userExists(userId: Int) = userId in users

    fun pruneUsers(): Int {
        var usersPruned = 0
        users.filter { it.value.expiresAt < Calendar.getInstance() }.forEach {
            usersPruned += 1
            _users.remove(it.key)
        }
        return usersPruned
    }
}