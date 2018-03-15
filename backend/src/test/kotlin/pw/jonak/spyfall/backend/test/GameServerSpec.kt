package pw.jonak.spyfall.backend.test

import com.nhaarman.mockito_kotlin.*
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import pw.jonak.spyfall.backend.SpyfallGameServer
import pw.jonak.spyfall.common.JoinGameRequest

object GameServerSpec : Spek({
    describe("Spyfall's Game Server") {
        val session: WebSocketSession = mock()

        describe("with no games added") {
            val server = SpyfallGameServer()

            it("should store users that join") {
                val request = JoinGameRequest(-1, "Hi", "abcd")
                runBlocking {
                    server.joinGame(request, session)
                }
                assert(server.connectedUsers.contains(request.userId))
                runBlocking {
                    verify(session).send(any())
                }
            }
        }

        describe("with a game added") {
            val server = SpyfallGameServer()

            it("should store created games") {

            }
        }
    }
})