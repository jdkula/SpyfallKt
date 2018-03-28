package pw.jonak.spyfall.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class MessageError(
    val reason: String,
    @SerialName("bad_message_type") val badMessageType: String? = null
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.EITHER

    companion object {
        const val messageTypeName = "message_error"
    }
}

@Serializable
class InvalidParameters : MessageError(Companion.reason) {
    companion object {
        const val reason = "invalid_parameters"
    }
}

@Serializable
open class ActionFailure(
    val reason: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.EITHER

    companion object {
        const val messageTypeName = "action_failure"
    }
}

@Serializable
open class StatusMessage(
    val status: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.EITHER

    companion object {
        const val messageTypeName = "status_message"
    }
}

/**
 * Acknowledges an action as complete.
 * Sent in response to:
 * [LeaveGameRequest]
 * [StartGameRequest]
 */
@Serializable
class Acknowledged(@SerialName("acknowledged_message_type") val acknowledgedMessageType: String) : StatusMessage(Companion.status) {
    companion object {
        const val status = "acknowledged_message"
    }
}

@Serializable
class UserNotFound(
    @SerialName("missing_user_id") val missingUserId: Int
) : StatusMessage(Companion.status) {
    companion object {
        const val status = "user_not_found"
    }
}

@Serializable
class GameNotFound(
    @SerialName("missing_game_code") val gameCode: String
) : StatusMessage(Companion.status) {
    companion object {
        const val status = "game_not_found"
    }
}

