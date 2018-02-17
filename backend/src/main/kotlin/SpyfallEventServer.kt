import io.ktor.application.Application
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap


class SpyfallEventServer {
    val connectedUsers = ConcurrentHashMap<Int, MutableList<WebSocketSession>>()

    suspend fun createUser(id: Int, session: WebSocketSession) {
        connectedUsers.getOrElse(id) {
            val new = ArrayList<WebSocketSession>()
            connectedUsers[id] = new
            new
        } += session

        session.send(Frame.Text("CREATE USER OK"))
    }

    suspend fun joinGame(userId: Int, gameCode: String, session: WebSocketSession) {

    }
}