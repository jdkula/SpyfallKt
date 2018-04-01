package pw.jonak.spyfall.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserRegistrationRequest(@SerialName("user_name") val userName: String) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "user_request"
    }
}

@Serializable
class EnsureUserRegistration(
        @SerialName("user_id") val userId: Int,
        @SerialName("user_name") val userName: String,
        @SerialName("session_id") val sessionId: Int
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "ensure_user_registration"
    }
}

@Serializable
class UserRegistrationInformation(
        @SerialName("user_id") val userId: Int,
        @SerialName("user_name") val userName: String,
        @SerialName("session_id") val sessionId: Int
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.SERVER

    companion object {
        const val messageTypeName = "user_registration_information"
    }
}