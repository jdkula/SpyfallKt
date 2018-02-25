package pw.jonak.spyfall.backend

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.serialization.json.JSON
import pw.jonak.spyfall.backend.SqlHelper.ensureRegistered
import pw.jonak.spyfall.backend.SqlHelper.joinGame
import pw.jonak.spyfall.common.JoinGameRequest
import pw.jonak.spyfall.common.MessageError
import pw.jonak.spyfall.common.SpyfallMessage
import java.util.concurrent.ConcurrentHashMap

/**
 * This represents the Spyfall Game Server, which works via WebSockets.
 * Messages are sent using a simple JSON format represented in [SpyfallMessage] (and subclasses).
 */
class SpyfallGameServer {
    /** Matches user ID to a list of [WebSocketSession]s. */
    val connectedUsers = ConcurrentHashMap<Int, MutableList<WebSocketSession>>()

    /** Allows a user to join a game, adding them to the list of connected users that are notified of state changes. */
    suspend fun userJoin(request: JoinGameRequest, session: WebSocketSession) {
        val registrationInfo = ensureRegistered(request.user_id, request.user_name)
        connectedUsers.getOrElse(request.user_id) {
            val newList = ArrayList<WebSocketSession>()
            connectedUsers[request.user_id] = newList
            newList
        } += session

        try {
            session.send(Frame.Text(JSON.stringify(joinGame(request.user_id, request.game_code) ?: "")))
        } catch (gnfe: GameNotFoundException) {
            session.send(Frame.Text(JSON.stringify(MessageError(request, "Game code not found!"))))
        }
    }
}