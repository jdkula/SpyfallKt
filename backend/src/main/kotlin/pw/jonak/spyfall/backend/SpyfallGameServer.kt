package pw.jonak.spyfall.backend

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.netty.util.internal.ConcurrentSet
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import kotlinx.serialization.json.JSON
import pw.jonak.spyfall.backend.gameElements.AllLocations
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
    val connectedUsers = ConcurrentHashMap<User, ConcurrentSet<WebSocketSession>>()

    /** Stores all the users that have connected to this Game Server and haven't expired. */
    private val userStore = UserStore()

    /** Stores all the currently open games */
    private val gameStore = GameStore(userStore)

    /** Allows a user to join a game, adding them to the list of connected users that are notified of state changes. */
    suspend fun joinGame(request: JoinGameRequest, session: WebSocketSession): SpyfallMessage? {
        val userInfo = userStore.ensureRegistered(request.userId, request.userName, thisSession)

        connectedUsers.getOrPut(userInfo) { ConcurrentSet() }.add(session)
        println("${userInfo.userName}/${userInfo.id} connected: ${connectedUsers[userInfo]?.size} sessions.")

        try {
            val game = gameStore.joinGame(request.userId, request.gameCode.toLowerCase())
                    ?: throw GameNotFoundException(request.gameCode.toLowerCase())
            userInfo.currentGame = request.gameCode
            println("Member ${userInfo.id} joined ${request.gameCode}!")
            println("Current users: ${game.users}")
            game.notifyUsers()
            return game.getLobbyInfo(userInfo)
        } catch (gnfe: GameNotFoundException) {
            return GameNotFound(request.gameCode.toLowerCase())
        }
    }

    suspend fun leaveGame(request: LeaveGameRequest): Acknowledged {
        gameStore.leaveGame(request.userId, request.gameCode)?.notifyUsers()
        return Acknowledged(LeaveGameRequest.messageTypeName)
    }

    suspend fun userLeave(userId: Int, session: WebSocketSession) {
        userStore.getExistingUser(userId)?.let { user ->
            val sessions = connectedUsers[user]
            sessions?.remove(session)
            if (sessions != null && sessions.isEmpty()) {
                connectedUsers.remove(user)
                user.currentGame?.let {
                    leaveGame(LeaveGameRequest(userId, it))
                }
            }
            println("Member $userId left! \n ${connectedUsers.map { it.key to it.value.size }}")
        }
    }

    suspend fun startGame(request: StartGameRequest): SpyfallMessage? {
        return notifyWithGame(request.gameCode) {
            if (gameHasStarted) {
                ActionFailure("Game already started")
            } else {
                start()
                null
            }
        }
    }

    suspend fun pauseGame(request: PauseGameRequest): SpyfallMessage? {
        return notifyWithGame(request.gameCode) {
            if (isPaused) {
                ActionFailure("Game already paused")
            } else {
                pause()
                null
            }
        }
    }

    suspend fun unpauseGame(request: UnpauseGameRequest): SpyfallMessage? {
        return notifyWithGame(request.gameCode) {
            if (isPaused) {
                unpause()
                null
            } else {
                ActionFailure("Game not paused")
            }
        }
    }

    suspend fun stopGame(request: StopGameRequest): SpyfallMessage? {
        return notifyWithGame(request.gameCode) {
            if (gameHasStarted) {
                stop()
                Acknowledged(request.messageType)
            } else {
                ActionFailure("Game not started")
            }
        }
    }

    private suspend inline fun <T> notifyWithGame(code: String, doThis: Game.() -> T): T? =
            gameStore.games[code]?.run {
                val x = doThis()
                notifyUsers()
                x
            }

    private suspend fun Game.notifyUsers() {
        users.forEach { user ->
            print("Notifying user ${user.userName}")
            connectedUsers[user]?.forEach { session ->
                print("~")
                try {
                    session.send(Frame.Text(JSON.stringify(getLobbyInfo(user))))
                } catch (ex: ClosedSendChannelException) {
                    println("$user's session unexpectedly disconnected!")
                    userLeave(user.id, session)
                }
            }
            println()
        }
    }

    suspend fun receiveMessage(message: SpyfallMessage, session: WebSocketSession): SpyfallMessage? {
        val returnMessage: Any? = when (message) {
            is CreateGameRequest -> {
                val gameCode = gameStore.createGame()
                message.userId?.let { uid ->
                    userStore.users[uid]?.let { user ->
                        joinGame(JoinGameRequest(user.id, user.userName, gameCode), session)
                    }
                }
                gameStore.getLobbyInfo(gameCode, message.userId)
            }
            is AdminAction -> when (message.action) {
                AdminActionType.SHUTDOWN -> System.exit(0)
                AdminActionType.PRUNE_GAMES -> gameStore.pruneGames()
                AdminActionType.PRUNE_USERS -> userStore.pruneUsers()
            }
            is UserRegistrationRequest -> userStore.createUser(message.userName).toMessage()
            is EnsureUserRegistration -> userStore.ensureRegistered(message.userId, message.userName, message.sessionId).toMessage()
            is LobbyInformationRequest -> gameStore.getLobbyInfo(message.gameCode, message.userId)
            is JoinGameRequest -> joinGame(message, session)
            is LeaveGameRequest -> leaveGame(message)
            is StartGameRequest -> startGame(message)
            is PauseGameRequest -> pauseGame(message)
            is UnpauseGameRequest -> unpauseGame(message)
            is StopGameRequest -> stopGame(message)
            is LocationListRequest -> getAllLocations()
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

    private fun getAllLocations(): LocationListAnswer {
        return LocationListAnswer(AllLocations.map { it.key })
    }

}