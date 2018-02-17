import io.ktor.application.Application
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

fun GameInformation.toJson(): String {
    TODO("Get Klaxon set up!")
}

class SpyfallEventServer {
    val connectedUsers = ConcurrentHashMap<Int, MutableList<WebSocketSession>>()

    suspend fun userJoin(id: Int, name: String, gameCode: String, session: WebSocketSession) {
        ensureRegistered(id, name)
        connectedUsers.getOrElse(id) {
            val new = ArrayList<WebSocketSession>()
            connectedUsers[id] = new
            new
        } += session

        session.send(Frame.Text(joinGame(id, gameCode).toJson()))
    }
}

/**
    User loads page:
        ** REST **
        Has ID cookie -> Ensures ID available
        Else -> Gets new ID
    User joins game
        ** WS **
        Gets list, etc.
 **/