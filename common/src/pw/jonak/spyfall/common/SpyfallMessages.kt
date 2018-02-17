package pw.jonak.spyfall.common

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

class UserRequest(val user_name: String) : SpyfallMessage {
    override val message_name = "user_request"
    override val SENDER_SIDE = Side.CLIENT
}

class UserRegistrationInformation(val user_id: Int, val user_name: String) : SpyfallMessage {
    override val message_name = "user_requested_information"
    override val SENDER_SIDE = Side.SERVER
}

class GameInformation(
    val user_id: Int,
    val game_code: String,
    val user_names: List<String>,
    val game_has_started: Boolean,
    val start_time: Long?,
    val pause_time: Long?,
    val total_time: Long?,
    val is_spy: Boolean?,
    val first_player: Int?,
    val location: String?,
    val role: String?
) : SpyfallMessage {
    override val message_name = "game_information"
    override val SENDER_SIDE = Side.SERVER
}

class JoinGameRequest(val user_id: Int, val user_name: String, val game_code: String) :
    SpyfallMessage {
    override val message_name = "join_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class LeaveGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = "leave_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class StartGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = "start_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class StopGameRequest(val user_id: Int, val game_code: String) : SpyfallMessage {
    override val message_name = "stop_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class MessageError(val request: SpyfallMessage, val reason: String) :
    SpyfallMessage {
    override val message_name = "message_error"
    override val SENDER_SIDE = Side.EITHER
}