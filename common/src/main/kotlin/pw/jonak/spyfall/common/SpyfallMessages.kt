package pw.jonak.spyfall.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.Optional

interface SpyfallMessage {
    val message_name: String
    val SENDER_SIDE: Side
}

enum class Side {
    SERVER,
    CLIENT,
    EITHER
}

const val SelfIsFirst = -1

@Serializable
class UserRequest(val user_name: String) : SpyfallMessage {
    override val message_name = "user_request"
    override val SENDER_SIDE = Side.CLIENT
}

@Serializable
class UserRegistrationInformation(val user_id: Int, val user_name: String) : SpyfallMessage {
    override val message_name = "user_requested_information"
    override val SENDER_SIDE = Side.SERVER
}

@Serializable
data class GameInformation(
        @Optional val user_id: Int? = null,
        val game_code: String,
        val user_names: List<String>,
        val game_has_started: Boolean,
        @Optional val start_time: Long? = null,
        @Optional val pause_time: Long? = null,
        @Optional val total_time: Long? = null,
        @Optional val is_spy: Boolean? = null,
        @Optional val first_player: Int? = null,
        @Optional val location: String? = null,
        @Optional val role: String? = null
) : SpyfallMessage {
    override val message_name = "game_information"
    override val SENDER_SIDE = Side.SERVER
}

@Serializable
class JoinGameRequest(val user_id: Int, val user_name: String, val game_code: String) :
        SpyfallMessage {
    override val message_name = "join_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

@Serializable
class LeaveGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = "leave_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

@Serializable
class StartGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = "start_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

@Serializable
class StopGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = "stop_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

@Serializable
open class MessageError(@Optional val request: SpyfallMessage? = null, val reason: String) :
        SpyfallMessage {
    override val message_name = "message_error"
    override val SENDER_SIDE = Side.EITHER
}

@Serializable
object InvalidParameters : MessageError(null, "invalid_parameters")

@Serializable
open class ActionFailure(val reason: String) : SpyfallMessage {
    override val message_name = "action_failure"
    override val SENDER_SIDE = Side.EITHER
}

@Serializable
object GameNotCreatedError : ActionFailure("game_not_created")

@Serializable
open class StatusMessage(val status: String) : SpyfallMessage {
    override val message_name = "status_message"
    override val SENDER_SIDE = Side.EITHER
}

@Serializable
class UserNotFound(val user_id: Int) : StatusMessage("user_not_found")

@Serializable
class GameNotFound(val game_id: String) : StatusMessage("game_not_found")

@Serializable
class PruneOK(val num_pruned: Int) : StatusMessage("prune_ok")

@Serializable
object ServerShutdownOK : StatusMessage("server_shutdown_ok")