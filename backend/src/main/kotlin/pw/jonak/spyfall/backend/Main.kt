package pw.jonak.spyfall.backend

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.default
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.content.staticRootFolder
import io.ktor.features.CORS
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSockets
import io.ktor.websocket.readText
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.serialization.json.JSON
import pw.jonak.spyfall.common.ServerShutdownOK
import pw.jonak.spyfall.common.UserRegistrationInformation
import pw.jonak.spyfall.common.deserialize
import pw.jonak.spyfall.common.serialize
import java.io.File
import java.io.IOException
import java.time.Duration

val server = SpyfallGameServer()

lateinit var httpServer: ApplicationEngine

/**
 * Sets up the server, and contains routing information for REST.
 */
fun main(args: Array<String>) {
    httpServer = embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(5L)
        }
        routing {

            /**
             * WEBSOCKET /ws
             * Opens a websocket connection.
             */
            webSocket("/ws") {
                var userId: Int? = null
                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val frameText = frame.readText()

                            println("${userId ?: "Someone"} -> $frameText")
                            val des = frameText.deserialize()
                            val response = server.receiveMessage(des, this)
                            if(response is UserRegistrationInformation) {
                                userId = response.userId
                            }

                            if (response != null) {
                                val ser = response.serialize()
                                println("<- $ser")
                                outgoing.send(Frame.Text(ser))
                            }
                        }
                    }
                } catch (ex: IOException) {
                    println("${userId ?: "Someone"}'s connection closed unexpectedly.")
                } catch (ex: ClosedSendChannelException) {
                    println("${userId ?: "Someone"}'s connection closed unexpectedly.")
                } finally {
                    userId?.let { server.userLeave(it, this) }
                }
            }

            /**
             * /execute
             * Contains admin commands the server can execute.
             */
            route("/execute") {
                /**
                 * POST /execute/shutdown
                 * Shuts down the server.
                 */
                post("shutdown") {
                    call.respondText(JSON.stringify(ServerShutdownOK()), ContentType.Application.Json)
                    System.exit(0)
                }
            }
            static {
                staticRootFolder = File("../frontend/build/WEB")
                files("js")
                files("css")
                files(".")
                default("index.html")
                static("localization") {
                    static("english") {
                        files(".")
                    }
                }
            }
        }
    }

    /*Runtime.getRuntime().addShutdownHook(Thread({
        println("Shutdown hook ran!")
        httpServer.stop(3, 5, TimeUnit.SECONDS)
    }))*/

    httpServer.start(wait = true)
}