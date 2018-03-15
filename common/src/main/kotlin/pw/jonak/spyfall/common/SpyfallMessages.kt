package pw.jonak.spyfall.common

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON

interface SpyfallMessage {
    val messageType: String
    val senderSide: Side
}

@Serializable
class SpyfallMessageImpl(
    @SerialName("message_type") override val messageType: String,
    @SerialName("sender_side") override val senderSide: Side
) : SpyfallMessage

fun String.deserialize(): SpyfallMessage {
    val stubMessage = JSON.nonstrict.parse<SpyfallMessageImpl>(this)

    return when (stubMessage.messageType) {
        UserRegistrationRequest.messageTypeName -> JSON.parse<UserRegistrationRequest>(this)
        UserRegistrationInformation.messageTypeName -> JSON.parse<UserRegistrationInformation>(this)
        GameInformation.messageTypeName -> JSON.parse<GameInformation>(this)
        CreateGameRequest.messageTypeName -> JSON.parse<CreateGameRequest>(this)
        JoinGameRequest.messageTypeName -> JSON.parse<JoinGameRequest>(this)
        LeaveGameRequest.messageTypeName -> JSON.parse<LeaveGameRequest>(this)
        StartGameRequest.messageTypeName -> JSON.parse<StartGameRequest>(this)
        StopGameRequest.messageTypeName -> JSON.parse<StopGameRequest>(this)
        PauseGameRequest.messageTypeName -> JSON.parse<PauseGameRequest>(this)
        UnpauseGameRequest.messageTypeName -> JSON.parse<UnpauseGameRequest>(this)
        MessageError.messageTypeName -> {
            val error = JSON.parse<MessageError>(this)
            when (error.reason) {
                InvalidParameters.reason -> JSON.parse<InvalidParameters>(this)
                else -> error
            }
        }
        ActionFailure.messageTypeName -> {
            val failure = JSON.parse<ActionFailure>(this)
            when (failure.reason) {
                GameNotCreatedError.reason -> JSON.parse<GameNotCreatedError>(this)
                else -> failure
            }
        }
        StatusMessage.messageTypeName -> {
            val status = JSON.parse<StatusMessage>(this)
            when (status.status) {
                UserNotFound.status -> JSON.parse<UserNotFound>(this)
                GameNotFound.status -> JSON.parse<GameNotFound>(this)
                PruneOK.status -> JSON.parse<PruneOK>(this)
                ServerShutdownOK.status -> JSON.parse<ServerShutdownOK>(this)
                else -> status
            }
        }
        AdminAction.messageTypeName -> JSON.parse<AdminAction>(this)
        else -> stubMessage
    }
}

fun SpyfallMessage.serialize(): String {
    return when (this.messageType) {
        UserRegistrationRequest.messageTypeName -> JSON.stringify(this as UserRegistrationRequest)
        UserRegistrationInformation.messageTypeName -> JSON.stringify(this as UserRegistrationInformation)
        GameInformation.messageTypeName -> JSON.stringify(this as GameInformation)
        CreateGameRequest.messageTypeName -> JSON.stringify(this as CreateGameRequest)
        JoinGameRequest.messageTypeName -> JSON.stringify(this as JoinGameRequest)
        LeaveGameRequest.messageTypeName -> JSON.stringify(this as LeaveGameRequest)
        StartGameRequest.messageTypeName -> JSON.stringify(this as StartGameRequest)
        StopGameRequest.messageTypeName -> JSON.stringify(this as StopGameRequest)
        PauseGameRequest.messageTypeName -> JSON.stringify(this as PauseGameRequest)
        UnpauseGameRequest.messageTypeName -> JSON.stringify(this as UnpauseGameRequest)
        MessageError.messageTypeName -> {
            when ((this as MessageError).reason) {
                InvalidParameters.reason -> JSON.stringify(this as InvalidParameters)
                else -> JSON.stringify(this)
            }
        }
        ActionFailure.messageTypeName -> {
            when ((this as ActionFailure).reason) {
                GameNotCreatedError.reason -> JSON.stringify(this as GameNotCreatedError)
                else -> JSON.stringify(this)
            }
        }
        StatusMessage.messageTypeName -> {
            when ((this as StatusMessage).status) {
                UserNotFound.status -> JSON.stringify(this as UserNotFound)
                GameNotFound.status -> JSON.stringify(this as GameNotFound)
                PruneOK.status -> JSON.stringify(this as PruneOK)
                ServerShutdownOK.status -> JSON.stringify(this as ServerShutdownOK)
                else -> JSON.stringify(this)
            }
        }
        AdminAction.messageTypeName -> JSON.stringify(this as AdminAction)
        else -> JSON.stringify(SpyfallMessageImpl(this.messageType, this.senderSide))
    }
}

enum class Side {
    SERVER,
    CLIENT,
    EITHER
}

enum class AdminActionType {
    SHUTDOWN,
    PRUNE_GAMES,
    PRUNE_USERS
}

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
class UserRegistrationInformation(
    @SerialName("user_id") val userId: Int,
    @SerialName("user_name") val userName: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.SERVER

    companion object {
        const val messageTypeName = "user_registration_information"
    }
}

@Serializable
data class GameInformation(
    @SerialName("user_id") @Optional val userId: Int? = null,
    @SerialName("game_code") val gameCode: String,
    @SerialName("user_name_list") val userNameList: List<String>,
    @SerialName("game_has_started") val gameHasStarted: Boolean,
    @SerialName("start_time") @Optional val startTime: Long? = null,
    @SerialName("pause_time") @Optional val pauseTime: Long? = null,
    @SerialName("game_length") @Optional val gameLength: Long? = null,
    @SerialName("is_spy") @Optional val isSpy: Boolean? = null,
    @SerialName("first_player") @Optional val firstPlayer: Int? = null,
    @Optional val location: String? = null,
    @Optional val role: String? = null
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.SERVER

    companion object {
        const val messageTypeName = "game_information"
    }
}

@Serializable
class CreateGameRequest(
    @SerialName("user_id") @Optional val userId: Int? = null
) : SpyfallMessage {
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName

    companion object {
        const val messageTypeName = "join_game_request"
    }
}

@Serializable
class JoinGameRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("user_name") val userName: String,
    @SerialName("game_code") val gameCode: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "join_game_request"
    }
}

@Serializable
class LeaveGameRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("game_code") val gameCode: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "leave_game_request"
    }
}

@Serializable
class StartGameRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("game_code") val gameCode: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "start_game_request"
    }
}

@Serializable
class PauseGameRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("game_code") val gameCode: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "pause_game_request"
    }
}

@Serializable
class UnpauseGameRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("game_code") val gameCode: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "unpause_game_request"
    }
}

@Serializable
class StopGameRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("game_code") val gameCode: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "stop_game_request"
    }
}

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
class GameNotCreatedError : ActionFailure(Companion.reason) {
    companion object {
        const val reason = "game_not_created"
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

@Serializable
class PruneOK(
    @SerialName("num_pruned") val numPruned: Int
) : StatusMessage(Companion.status) {
    companion object {
        const val status = "prune_ok"
    }
}

@Serializable
class ServerShutdownOK : StatusMessage(Companion.status) {
    companion object {
        const val status = "server_shutdown_ok"
    }
}

@Serializable
class AdminAction(
    val action: AdminActionType
) : SpyfallMessage {
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName

    companion object {
        const val messageTypeName = "admin_action"
    }
}