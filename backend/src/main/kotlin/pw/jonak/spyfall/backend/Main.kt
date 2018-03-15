package pw.jonak.spyfall.backend

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
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
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.serialization.json.JSON
import pw.jonak.spyfall.common.ServerShutdownOK
import pw.jonak.spyfall.common.deserialize
import pw.jonak.spyfall.common.serialize
import java.io.IOException
import java.time.Duration

val server = SpyfallGameServer()

var httpServer: ApplicationEngine? = null

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
                            val response = server.recieveMessage(des, this)

                            if (response != null) {
                                val ser = response.serialize()
                                println("<- $ser")
                                outgoing.send(Frame.Text(ser))
                            }
                        }
                    }
                } catch (ex: IOException) {
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
//  -- looking at using websockets only -- this seems like a better strategy, tbh --
//            /**
//             * /user
//             * Contains requests for dealing with users
//             */
//            route("/user") {
//                /**
//                 * POST /user/new?name=STRING
//                 * Creates a new user with a user [name], returning the resultant [UserRegistrationInformation].
//                 */
//                post("new") {
//                    try {
//                        val userName = context.parameters["name"]
//                        if (userName.isNullOrEmpty()) throw InvalidParameterException()
//                        val userInfo: UserRegistrationInformation = createUser(userName!!).toMessage()
//                        call.respondText(JSON.stringify(userInfo), ContentType.Application.Json, HttpStatusCode.Created)
//                    } catch (ex: AbstractMethodError) {
//                        call.respondText(JSON.stringify(ActionFailure("DB Error")), ContentType.Application.Json, HttpStatusCode.InternalServerError)
//                    } catch (ex: InvalidParameterException) {
//                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
//                    }
//                }
//
//                /**
//                 * POST /user/ensure?userId=INT&name=STRING
//                 * Ensures that the user [userId] is registered; if not,
//                 * registers it. Returns [UserRegistrationInformation].
//                 */
//                post("ensure") {
//                    try {
//                        val userId = context.parameters["userId"]?.toIntOrNull()
//                        val userName = context.parameters["name"]
//                        if (userId == null || userName.isNullOrEmpty()) throw InvalidParameterException()
//                        val userInfo = ensureRegistered(userId, userName!!)
//                        val statusCode = if (userInfo.id == userId) HttpStatusCode.OK else HttpStatusCode.Created
//                        call.respondText(JSON.stringify(userInfo), ContentType.Application.Json, statusCode)
//                    } catch (ex: InvalidParameterException) {
//                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
//                    }
//                }
//
//                /**
//                 * GET /user/verify?userId=STRING
//                 * Determines if a user exists.
//                 * Returns HTTP 200 if found, HTTP 404 if not found.
//                 */
//                get("verify") {
//                    try {
//                        val userId = context.parameters["userId"]?.toIntOrNull() ?: throw InvalidParameterException()
//                        if (userExists(userId)) {
//                            call.respondText(JSON.stringify(StatusMessage("OK")), ContentType.Application.Json)
//                        } else {
//                            call.respondText(JSON.stringify(UserNotFound(userId)), ContentType.Application.Json, HttpStatusCode.NotFound)
//                        }
//                    } catch (ex: InvalidParameterException) {
//                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
//                    }
//                }
//
//                /**
//                 * DELETE /user/prune
//                 * Forces a prune of all expired users.
//                 */
//                delete("prune") {
//                    val numPruned = pruneUsers()
//                    call.respondText(JSON.stringify(PruneOK(numPruned)), ContentType.Application.Json)
//                }
//            }
//
//            /**
//             * /game
//             * Contains REST commands that have to do with game modification.
//             */
//            route("/game") {
//                /**
//                 * GET /game/info?game_id=STRING<&userId=INT>
//                 * Gets game info for [game_id]. Returns [GameInformation].
//                 * If [userId] is passed in, fills in additional information
//                 * for that user.
//                 * If the game doesn't exist, returns HTTP 404.
//                 */
//                get("info") {
//                    try {
//                        val gameId = context.parameters["game_id"] ?: throw InvalidParameterException()
//                        val userId = context.parameters["userId"]?.toIntOrNull()
//                        val gameInfo = getGameInfo(gameId, userId)
//                        if (gameInfo == null) {
//                            call.respondText(JSON.stringify(GameNotFound(gameId)), ContentType.Application.Json, HttpStatusCode.NotFound)
//                        } else {
//                            call.respondText(JSON.stringify(gameInfo), ContentType.Application.Json)
//                        }
//                    } catch (ex: InvalidParameterException) {
//                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
//                    }
//                }
//
//                /**
//                 * POST /game/create
//                 * Creates a new game and returns information about it.
//                 * Gives error 504 if the game wasn't properly created.
//                 */
//                post("create") {
//                    try {
//                        val gameCode = createGame()
//                        val gameInformation = getGameInfo(gameCode)
//                                ?: throw InvalidStateException("Created game doesn't exist!")
//                        call.respondText(JSON.stringify(gameInformation), ContentType.Application.Json, HttpStatusCode.Created)
//                    } catch (ex: InvalidStateException) {
//                        call.respondText(JSON.stringify(GameNotCreatedError()), ContentType.Application.Json, HttpStatusCode.InternalServerError)
//                    }
//                }
//
//                /**
//                 * PATCH /game/join?game_id=STRING&userId=INT
//                 * Joins a user to a game, returning 404 if
//                 * the game or user isn't found, or 504 if joining
//                 * failed for another user.
//                 */
//                patch("join") {
//                    try {
//                        val gameCode = context.parameters["game_id"]
//                        val userId = context.parameters["userId"]?.toIntOrNull()
//                        if (gameCode == null || userId == null) throw InvalidParameterException()
//
//                        val gameInfo = server.getGameInfo(gameCode, userId)
//                        if (gameInfo == null) {
//                            call.respondText(JSON.stringify(GameNotFound(gameCode)), ContentType.Application.Json, HttpStatusCode.NotFound)
//                        } else {
//                            if (server.userExists(userId)) {
//                                val joinRequest = JoinGameRequest(userId, gameCode)
//                                server.joinGame(joinRequest)
//                                val newGameInfo = getGameInfo(gameCode, userId) ?: throw InvalidStateException("")
//                                call.respondText(JSON.stringify(newGameInfo), ContentType.Application.Json)
//                            } else {
//                                call.respondText(JSON.stringify(UserNotFound(userId)), ContentType.Application.Json, HttpStatusCode.NotFound)
//                            }
//                        }
//                    } catch (ex: InvalidParameterException) {
//                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
//                    } catch (ex: InvalidStateException) {
//                        call.respond(HttpStatusCode.InternalServerError, ActionFailure("Couldn't Join Player to Game"))
//                    }
//                }
//
//                /**
//                 * DELETE /game/prune
//                 * Forces a prune of all empty games.
//                 */
//                delete("prune") {
//                    val numPruned = server.pruneGames()
//                    call.respondText(JSON.stringify(PruneOK(numPruned)), ContentType.Application.Json)
//                }
//
//            }
//
//            /**
//             * /location
//             * Contains REST commands having to do with
//             * locations and location sets.
//             */
//            route("/location") {
//
//                /**
//                 * GET /location
//                 * Gets the standard set of locations.
//                 */
//                get {
//                    call.respondText(JSON.stringify(Location::class.serializer().list, AllLocations.values.toList()), ContentType.Application.Json)
//                }
//            }
//
//            /**
//             * /test
//             * Contains test REST commands.
//             */
//            route("/test") {
//                /**
//                 * GET /test/params/{CMD}
//                 * Shows all the parameters passed into the method, including the path {CMD}.
//                 */
//                get("/params/{cmd}") {
//                    call.respondText("${context.parameters}")
//                }
//
//                get("/locations") {
//                    call.respondText("$AllLocations")
//                }
//            }
            get("/") {
                call.respondText("Hello, World!", ContentType.Text.Html)
            }

        }
    }

    httpServer?.start(wait = true)
}