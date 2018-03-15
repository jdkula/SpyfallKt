package pw.jonak.spyfall.backend

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.serialization.json.JSON
import pw.jonak.spyfall.backend.gameElements.Game
import pw.jonak.spyfall.backend.gameElements.User
import pw.jonak.spyfall.backend.storage.GameStore
import pw.jonak.spyfall.backend.storage.UserStore
import pw.jonak.spyfall.common.*
import java.util.concurrent.ConcurrentHashMap

/**
 * This represents the Spyfall Game Server, which works via WebSockets.
 * Messages are sent using a simple JSON format represented in [SpyfallMessage] (and subclasses).
 */
class SpyfallGameServer {
    /** Matches user ID to a list of [WebSocketSession]s. */
    val connectedUsers = ConcurrentHashMap<User, MutableList<WebSocketSession>>()

    /** Stores all the users that have connected to this Game Server and haven't expired. */
    private val userStore = UserStore()

    /** Stores all the currently open games */
    private val gameStore = GameStore(userStore)

    /** Allows a user to join a game, adding them to the list of connected users that are notified of state changes. */
    suspend fun joinGame(request: JoinGameRequest, session: WebSocketSession) {
        val userInfo = userStore.ensureRegistered(request.userId, request.userName)
        connectedUsers.getOrElse(userInfo) {
            val newList = ArrayList<WebSocketSession>()
            connectedUsers[userInfo] = newList
            newList
        } += session

        try {
            val game = gameStore.joinGame(request.userId, request.gameCode)
                    ?: throw GameNotFoundException(request.gameCode)
            game.notifyUsers()
            println("Member ${userInfo.id} joined! \n ${connectedUsers.map { it.key to it.value.size }}")
        } catch (gnfe: GameNotFoundException) {
            session.send(Frame.Text(JSON.stringify(GameNotFound(request.gameCode))))
        }
    }

    suspend fun leaveGame(request: LeaveGameRequest) {
        gameStore.leaveGame(request.userId, request.gameCode)
    }

    suspend fun userLeave(userId: Int, session: WebSocketSession) {
        userStore.getExistingUser(userId)?.let { user ->
            val sessions = connectedUsers[user]
            sessions?.remove(session)
            if (sessions != null && sessions.isEmpty()) {
                connectedUsers.remove(user)
            }
            println("Member $userId left! \n ${connectedUsers.map { it.key to it.value.size }}")
        }
    }

    suspend fun startGame(request: StartGameRequest) {
        notifyWithGame(request.gameCode) {
            start()
        }
    }

    suspend fun pauseGame(request: PauseGameRequest) {
        notifyWithGame(request.gameCode) {
            pause()
        }
    }

    suspend fun unpauseGame(request: UnpauseGameRequest) {
        notifyWithGame(request.gameCode) {
            unpause()
        }
    }

    suspend fun stopGame(request: StopGameRequest) {
        notifyWithGame(request.gameCode) {
            stop()
        }
    }

    private suspend fun notifyWithGame(code: String, doThis: Game.() -> Unit) {
        gameStore.games[code]?.run {
            doThis()
            notifyUsers()
        }
    }

    private suspend fun Game.notifyUsers() {
        users.forEach { user ->
            connectedUsers[user]?.forEach { session ->
                session.send(Frame.Text(JSON.stringify(getGameInfo(user))))
            }
        }
    }

    suspend fun recieveMessage(message: SpyfallMessage, session: WebSocketSession): SpyfallMessage? {
        val returnMessage: Any? = when (message) {
            is CreateGameRequest -> gameStore.getGameInfo(gameStore.createGame(), message.userId)
            is AdminAction -> when (message.action) {
                AdminActionType.SHUTDOWN -> System.exit(0)
                AdminActionType.PRUNE_GAMES -> gameStore.pruneGames()
                AdminActionType.PRUNE_USERS -> userStore.pruneUsers()
            }
            is UserRegistrationRequest -> userStore.createUser(message.userName).toMessage()
            is JoinGameRequest -> joinGame(message, session)
            is LeaveGameRequest -> leaveGame(message)
            is StartGameRequest -> startGame(message)
            is PauseGameRequest -> pauseGame(message)
            is UnpauseGameRequest -> unpauseGame(message)
            is StopGameRequest -> stopGame(message)
            is MessageError -> println("ERROR: Recent message sent ${message.badMessageType} failed for ${message.reason}!")
            is ActionFailure -> println("ERROR: Client failed to do something: ${message.reason}")
            is StatusMessage -> println("STATUS: Client sent status message ${message.status}")
            else -> null
        }

        return when (returnMessage) {
            is SpyfallMessage -> returnMessage
            else -> null
        }
    }

}