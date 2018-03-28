package pw.jonak.spyfall.common

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent by the server to the client in response to a myriad of different requests,
 * including:
 * [LobbyInformationRequest]
 * [CreateGameRequest]
 * It's also sent whenever a server-side change is made to the lobby, if the
 * client is joined to that game. The client will receive a [LobbyInformation]
 * as a side-effect of this as a response to the following requests:
 * [JoinGameRequest]
 * [StartGameRequest]
 * [PauseGameRequest]
 * [UnpauseGameRequest]
 * [StopGameRequest]
 */
@Serializable
data class LobbyInformation(
        @SerialName("game_code") val gameCode: String,
        @SerialName("user_name_list") val userNameList: List<String>,
        @SerialName("game_info") @Optional val gameInformation: GameInformation? = null
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.SERVER

    val gameHasStarted: Boolean get() = gameInformation != null

    companion object {
        const val messageTypeName = "lobby_information"
    }
}

/**
 * Requests a [LobbyInformation] from the server. Obviously,
 * the server will reply with a [LobbyInformation].
 */
@Serializable
data class LobbyInformationRequest(
        @SerialName("user_id") val userId: Int,
        @SerialName("game_code") val gameCode: String
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "lobby_information_request"
    }
}

/**
 * Rarely sent on its own, Rather, it's usually wrapped up
 * inside a [LobbyInformation].
 * GameInformation will be non-null in [LobbyInformation] if
 * the game has started or is currently running.
 * If the game has not started, [LobbyInformation.gameInformation]
 * will be null.
 */
@Serializable
data class GameInformation(
        @SerialName("start_time") val startTime: Int,
        @SerialName("game_length") val gameLength: Int,
        @SerialName("is_spy") val isSpy: Boolean,
        @SerialName("first_player") val firstPlayer: Int,
        val location: String,
        val role: String,
        @SerialName("pause_time") @Optional val pauseTime: Long? = null
) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.SERVER

    companion object {
        const val messageTypeName = "game_information"
    }
}

/**
 * Requests a [LocationListAnswer] from the server. Obviously,
 * the server responds with a [LocationListAnswer].
 */
@Serializable
class LocationListRequest : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = LocationListRequest.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT

    companion object {
        const val messageTypeName = "location_list_request"
    }
}

/**
 * Sent in response to a [LocationListRequest].
 */
@Serializable
class LocationListAnswer(@SerialName("location_list") val locationList: List<String>) : SpyfallMessage {
    @SerialName("message_type")
    override val messageType = LocationListAnswer.messageTypeName
    @SerialName("sender_side")
    override val senderSide = Side.SERVER

    companion object {
        const val messageTypeName = "location_list_answer"
    }
}

/**
 * Requests that the server create a game.
 * The server responds with [LobbyInformation].
 * Responds with [GameNotCreatedError] if the game
 * wasn't created for some reason.
 */
@Serializable
class CreateGameRequest(
        @SerialName("user_id") @Optional val userId: Int? = null
) : SpyfallMessage {
    @SerialName("sender_side")
    override val senderSide = Side.CLIENT
    @SerialName("message_type")
    override val messageType = Companion.messageTypeName

    companion object {
        const val messageTypeName = "create_game_request"
    }
}

/**
 * Requests that a given user join the given game code.
 * Responds with [LobbyInformation] if the game was joined.
 * Responds with [GameNotFound] if the game wasn't found.
 */
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

/**
 * Requests that a given user leave the game they were a part of.
 * Responds with [Acknowledged].
 */
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

/**
 * Requests that a given game is started.
 * Responds with [LobbyInformation]
 */
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

/**
 * Requests that a given game is paused.
 * Responds with [LobbyInformation]
 */
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

/**
 * Requests that a given game is unpaused.
 * Responds with [LobbyInformation]
 */
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

/**
 * Requests that a given game is stopped.
 * Responds with [LobbyInformation].
 */
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

/**
 * Sent if a game wasn't created for whatever reason.
 */
@Serializable
class GameNotCreatedError : ActionFailure(Companion.reason) {
    companion object {
        const val reason = "game_not_created"
    }
}