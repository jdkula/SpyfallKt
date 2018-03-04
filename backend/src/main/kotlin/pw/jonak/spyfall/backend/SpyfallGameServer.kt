package pw.jonak.spyfall.backend

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.serialization.json.JSON
import pw.jonak.spyfall.backend.gameElements.Game
import pw.jonak.spyfall.backend.gameElements.User
import pw.jonak.spyfall.backend.storage.GameStore
import pw.jonak.spyfall.backend.storage.UserStore.ensureRegistered
import pw.jonak.spyfall.backend.storage.UserStore.getExistingUser
import pw.jonak.spyfall.common.*
import java.util.concurrent.ConcurrentHashMap

/**
 * This represents the Spyfall Game Server, which works via WebSockets.
 * Messages are sent using a simple JSON format represented in [SpyfallMessage] (and subclasses).
 */
class SpyfallGameServer {
    /** Matches user ID to a list of [WebSocketSession]s. */
    val connectedUsers = ConcurrentHashMap<User, MutableList<WebSocketSession>>()

    /** Allows a user to join a game, adding them to the list of connected users that are notified of state changes. */
    suspend fun joinGame(request: JoinGameRequest, session: WebSocketSession) {
        val userInfo = ensureRegistered(request.user_id, request.user_name)
        connectedUsers.getOrElse(userInfo) {
            val newList = ArrayList<WebSocketSession>()
            connectedUsers[userInfo] = newList
            newList
        } += session

        try {
            val game = GameStore.joinGame(request.user_id, request.game_code)
                    ?: throw GameNotFoundException(request.game_code)
            game.notifyUsers()
            println("Member ${userInfo.id} joined! \n ${connectedUsers.map { it.key to it.value.size }}")
        } catch (gnfe: GameNotFoundException) {
            session.send(Frame.Text(JSON.stringify(GameNotFound(request.game_code))))
        }
    }

    suspend fun leaveGame(request: LeaveGameRequest) {
        GameStore.leaveGame(request.user_id, request.game_code)

    }

    suspend fun userLeave(userId: Int, session: WebSocketSession) {
        getExistingUser(userId)?.let { user ->
            val sessions = connectedUsers[user]
            sessions?.remove(session)
            if (sessions != null && sessions.isEmpty()) {
                connectedUsers.remove(user)
            }
            println("Member $userId left! \n ${connectedUsers.map { it.key to it.value.size }}")
        }
    }

    suspend fun startGame(request: StartGameRequest) {
        GameStore.games[request.game_code]?.let { game ->
            game.start()
            game.notifyUsers()
        }
    }

    private suspend fun Game.notifyUsers() {
        users.forEach { user ->
            connectedUsers[user]?.forEach { session ->
                session.send(Frame.Text(JSON.stringify(getGameInfo(user))))
            }
        }
    }

}