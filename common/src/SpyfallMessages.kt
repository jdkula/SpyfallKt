interface SpyfallMessage {
    val JSON_IDENTIFIER: String
    val SENDER_SIDE: Side
}

enum class Side {
    SERVER,
    CLIENT,
    EITHER
}

const val SelfIsFirst = -1

class UserRequest(val userName: String) : SpyfallMessage {
    override val JSON_IDENTIFIER = "user_request"
    override val SENDER_SIDE = Side.CLIENT
}

class UserRegistrationInformation(val userId: Int, val userName: String) : SpyfallMessage {
    override val JSON_IDENTIFIER = "user_requested_information"
    override val SENDER_SIDE = Side.SERVER
}

class GameInformation(
    val userId: Int,
    val gameCode: String,
    val userNames: List<String>,
    val gameHasStarted: Boolean,
    val startTime: Long?,
    val pauseTime: Long?,
    val totalTime: Long?,
    val isSpy: Boolean?,
    val firstPlayer: Int?,
    val location: String?,
    val role: String?
) : SpyfallMessage {
    override val JSON_IDENTIFIER = "game_information"
    override val SENDER_SIDE = Side.SERVER
}

class JoinGameRequest(val userId: Int, val gameCode: String) : SpyfallMessage {
    override val JSON_IDENTIFIER = "join_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class LeaveGameRequest(val userId: Int, val gameCode: String) : SpyfallMessage {
    override val JSON_IDENTIFIER = "leave_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class StartGameRequest(val userId: Int, val gameCode: String) : SpyfallMessage {
    override val JSON_IDENTIFIER = "start_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class StopGameRequest(val userId: Int, val gameCode: String) : SpyfallMessage {
    override val JSON_IDENTIFIER = "stop_game_request"
    override val SENDER_SIDE = Side.CLIENT
}

class MessageError(val request: SpyfallMessage, val reason: String) : SpyfallMessage {
    override val JSON_IDENTIFIER = "message_error"
    override val SENDER_SIDE = Side.EITHER
}