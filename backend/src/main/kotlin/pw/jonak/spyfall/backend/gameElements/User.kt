package pw.jonak.spyfall.backend.gameElements

import com.kizitonwose.time.days
import com.kizitonwose.time.plus
import pw.jonak.spyfall.common.UserRegistrationInformation
import java.util.*

class User(val id: Int, var userName: String) {

    var currentGame: String? = null

    val expiresAt = Calendar.getInstance() + 1.days

    override fun equals(other: Any?): Boolean =
            when(other) {
                is User -> other.id == id
                else -> false
            }

    override fun hashCode(): Int = id

    fun toMessage(): UserRegistrationInformation {
        return UserRegistrationInformation(id, userName)
    }

    override fun toString(): String {
        return "{$id: $userName}"
    }

    companion object {
        var totalUsersRegistered: Int = 0
            get() {
                field += 1
                return field
            }
            private set
    }
}