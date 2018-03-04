package pw.jonak.spyfall.common

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON

interface SpyfallMessage {
    val message_name: String
    val SENDER_SIDE: Side
}

@Serializable
class SpyfallMessageImpl(
        override val message_name: String,
        override val SENDER_SIDE: Side
) : SpyfallMessage

fun deserializeMessage(message: String): SpyfallMessage {
    val stubMessage = JSON.parse<SpyfallMessageImpl>(message)

    return when (stubMessage.message_name) {
        UserRequest.message_name -> JSON.parse<UserRequest>(message)
        UserRegistrationInformation.message_name -> JSON.parse<UserRegistrationInformation>(message)
        GameInformation.message_name -> JSON.parse<GameInformation>(message)
        JoinGameRequest.message_name -> JSON.parse<JoinGameRequest>(message)
        LeaveGameRequest.message_name -> JSON.parse<LeaveGameRequest>(message)
        StartGameRequest.message_name -> JSON.parse<StartGameRequest>(message)
        StopGameRequest.message_name -> JSON.parse<StopGameRequest>(message)
        MessageError.message_name -> {
            val error = JSON.parse<MessageError>(message)
            when(error.reason) {
                InvalidParameters.reason -> JSON.parse<InvalidParameters>(message)
                else -> error
            }
        }
        ActionFailure.message_name -> {
            val failure = JSON.parse<ActionFailure>(message)
            when(failure.reason) {
                GameNotCreatedError.reason -> JSON.parse<GameNotCreatedError>(message)
                else -> failure
            }
        }
        StatusMessage.message_name -> {
            val status = JSON.parse<StatusMessage>(message)
            when(status.status) {
                UserNotFound.status -> JSON.parse<UserNotFound>(message)
                GameNotFound.status -> JSON.parse<GameNotFound>(message)
                PruneOK.status -> JSON.parse<PruneOK>(message)
                ServerShutdownOK.status -> JSON.parse<ServerShutdownOK>(message)
                else -> status
            }
        }
        else -> stubMessage
    }
}

enum class Side {
    SERVER,
    CLIENT,
    EITHER
}

@Serializable
class UserRequest(val user_name: String) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.CLIENT

    companion object {
        const val message_name = "user_request"
    }
}

@Serializable
class UserRegistrationInformation(val user_id: Int, val user_name: String) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.SERVER

    companion object {
        const val message_name = "user_registration_information"
    }
}

@Serializable
data class GameInformation(
        @Optional val user_id: Int? = null,
        val game_code: String,
        val user_names: List<String>,
        val game_has_started: Boolean,
        @Optional val start_time: Long? = null,
        @Optional val pause_time: Long? = null,
        @Optional val game_length: Long? = null,
        @Optional val is_spy: Boolean? = null,
        @Optional val first_player: Int? = null,
        @Optional val location: String? = null,
        @Optional val role: String? = null
) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.SERVER

    companion object {
        const val message_name = "game_information"
    }
}

@Serializable
class JoinGameRequest(val user_id: Int, val user_name: String, val game_code: String) :
        SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.CLIENT

    companion object {
        const val message_name = "join_game_request"
    }
}

@Serializable
class LeaveGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.CLIENT

    companion object {
        const val message_name = "leave_game_request"
    }
}

@Serializable
class StartGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.CLIENT

    companion object {
        const val message_name = "start_game_request"
    }
}

@Serializable
class StopGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.CLIENT

    companion object {
        const val message_name = "stop_game_request"
    }
}

@Serializable
open class MessageError(val reason: String, val bad_message_name: String? = null) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.EITHER

    companion object {
        const val message_name = "message_error"
    }
}

@Serializable
class InvalidParameters : MessageError(Companion.reason) {
    companion object {
        const val reason = "invalid_parameters"
    }
}

@Serializable
open class ActionFailure(val reason: String) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.EITHER

    companion object {
        const val message_name = "action_failure"
    }
}

@Serializable
class GameNotCreatedError : ActionFailure(Companion.reason) {
    companion object {
        const val reason = "game_not_created"
    }
}

@Serializable
open class StatusMessage(val status: String) : SpyfallMessage {
    override val message_name = Companion.message_name
    override val SENDER_SIDE = Side.EITHER

    companion object {
        const val message_name = "status_message"
    }
}

@Serializable
class UserNotFound(val user_id: Int) : StatusMessage(Companion.status) {
    companion object {
        const val status = "user_not_found"
    }
}

@Serializable
class GameNotFound(val game_id: String) : StatusMessage(Companion.status) {
    companion object {
        const val status = "game_not_found"
    }
}

@Serializable
class PruneOK(val num_pruned: Int) : StatusMessage(Companion.status) {
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