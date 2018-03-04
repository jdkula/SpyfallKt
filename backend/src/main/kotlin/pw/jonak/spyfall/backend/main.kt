package pw.jonak.spyfall.backend

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSockets
import io.ktor.websocket.readText
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import pw.jonak.spyfall.backend.gameElements.AllLocations
import pw.jonak.spyfall.backend.gameElements.Location
import pw.jonak.spyfall.backend.storage.GameStore.createGame
import pw.jonak.spyfall.backend.storage.GameStore.getGameInfo
import pw.jonak.spyfall.backend.storage.GameStore.joinGame
import pw.jonak.spyfall.backend.storage.GameStore.pruneGames
import pw.jonak.spyfall.backend.storage.UserStore
import pw.jonak.spyfall.backend.storage.UserStore.createUser
import pw.jonak.spyfall.backend.storage.UserStore.ensureRegistered
import pw.jonak.spyfall.backend.storage.UserStore.pruneUsers
import pw.jonak.spyfall.backend.storage.UserStore.userExists
import pw.jonak.spyfall.common.*
import java.io.IOException
import java.security.InvalidParameterException
import java.time.Duration

val server = SpyfallGameServer()

/**
 * Sets up the server, and contains routing information for REST.
 */
fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(5L)
        }
        routing {

            /**
             * WEBSOCKET /ws?user_id=USER_ID&game_id=GAME_ID
             * Opens a websocket connection for userId [user_id], for game negotiation.
             */
            webSocket("/ws") {
                try {
                    val userId: Int? = call.parameters["user_id"]?.toIntOrNull()
                    val gameId: String? = call.parameters["game_id"]
                    if (userId == null || gameId == null) throw InvalidParameterException()
                    println("Join request found for $userId -> $gameId!")

                    UserStore.getExistingUser(userId)?.let { user ->
                        server.joinGame(JoinGameRequest(user.id, user.userName, gameId), this)
                        println("$userId joined!")
                        try {
                            incoming.consumeEach { frame ->
                                if (frame is Frame.Text) {
                                    println("$userId -> ${frame.readText()}")
                                }
                            }
                        } catch (ex: IOException) {
                            println("$userId's connection closed unexpectedly.")
                        } finally {
                            server.userLeave(userId, this)
                        }
                    }
                } catch (ex: InvalidParameterException) {
                    call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
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

            /**
             * /user
             * Contains requests for dealing with users
             */
            route("/user") {
                /**
                 * POST /user/new?name=STRING
                 * Creates a new user with a user [name], returning the resultant [UserRegistrationInformation].
                 */
                post("new") {
                    try {
                        val userName = context.parameters["name"]
                        if (userName.isNullOrEmpty()) throw InvalidParameterException()
                        val userInfo: UserRegistrationInformation = createUser(userName!!).toMessage()
                        call.respondText(JSON.stringify(userInfo), ContentType.Application.Json, HttpStatusCode.Created)
                    } catch (ex: AbstractMethodError) {
                        call.respondText(JSON.stringify(ActionFailure("DB Error")), ContentType.Application.Json, HttpStatusCode.InternalServerError)
                    } catch (ex: InvalidParameterException) {
                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
                    }
                }

                /**
                 * POST /user/ensure?user_id=INT&name=STRING
                 * Ensures that the user [user_id] is registered; if not,
                 * registers it. Returns [UserRegistrationInformation].
                 */
                post("ensure") {
                    try {
                        val userId = context.parameters["user_id"]?.toIntOrNull()
                        val userName = context.parameters["name"]
                        if (userId == null || userName.isNullOrEmpty()) throw InvalidParameterException()
                        val userInfo = ensureRegistered(userId, userName!!)
                        val statusCode = if (userInfo.id == userId) HttpStatusCode.OK else HttpStatusCode.Created
                        call.respondText(JSON.stringify(userInfo), ContentType.Application.Json, statusCode)
                    } catch (ex: InvalidParameterException) {
                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
                    }
                }

                /**
                 * GET /user/verify?user_id=STRING
                 * Determines if a user exists.
                 * Returns HTTP 200 if found, HTTP 404 if not found.
                 */
                get("verify") {
                    try {
                        val userId = context.parameters["user_id"]?.toIntOrNull() ?: throw InvalidParameterException()
                        if (userExists(userId)) {
                            call.respondText(JSON.stringify(StatusMessage("OK")), ContentType.Application.Json)
                        } else {
                            call.respondText(JSON.stringify(UserNotFound(userId)), ContentType.Application.Json, HttpStatusCode.NotFound)
                        }
                    } catch (ex: InvalidParameterException) {
                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
                    }
                }

                /**
                 * DELETE /user/prune
                 * Forces a prune of all expired users.
                 */
                delete("prune") {
                    val numPruned = pruneUsers()
                    call.respondText(JSON.stringify(PruneOK(numPruned)), ContentType.Application.Json)
                }
            }

            /**
             * /game
             * Contains REST commands that have to do with game modification.
             */
            route("/game") {
                /**
                 * GET /game/info?game_id=STRING<&user_id=INT>
                 * Gets game info for [game_id]. Returns [GameInformation].
                 * If [user_id] is passed in, fills in additional information
                 * for that user.
                 * If the game doesn't exist, returns HTTP 404.
                 */
                get("info") {
                    try {
                        val gameId = context.parameters["game_id"] ?: throw InvalidParameterException()
                        val userId = context.parameters["user_id"]?.toIntOrNull()
                        val gameInfo = getGameInfo(gameId, userId)
                        if (gameInfo == null) {
                            call.respondText(JSON.stringify(GameNotFound(gameId)), ContentType.Application.Json, HttpStatusCode.NotFound)
                        } else {
                            call.respondText(JSON.stringify(gameInfo), ContentType.Application.Json)
                        }
                    } catch (ex: InvalidParameterException) {
                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
                    }
                }

                /**
                 * POST /game/create
                 * Creates a new game and returns information about it.
                 * Gives error 504 if the game wasn't properly created.
                 */
                post("create") {
                    try {
                        val gameCode = createGame()
                        val gameInformation = getGameInfo(gameCode)
                                ?: throw InvalidStateException("Created game doesn't exist!")
                        call.respondText(JSON.stringify(gameInformation), ContentType.Application.Json, HttpStatusCode.Created)
                    } catch (ex: InvalidStateException) {
                        call.respondText(JSON.stringify(GameNotCreatedError()), ContentType.Application.Json, HttpStatusCode.InternalServerError)
                    }
                }

                /**
                 * PATCH /game/join?game_id=STRING&user_id=INT
                 * Joins a user to a game, returning 404 if
                 * the game or user isn't found, or 504 if joining
                 * failed for another user.
                 */
                patch("join") {
                    try {
                        val gameCode = context.parameters["game_id"]
                        val userId = context.parameters["user_id"]?.toIntOrNull()
                        if (gameCode == null || userId == null) throw InvalidParameterException()

                        val gameInfo = getGameInfo(gameCode, userId)
                        if (gameInfo == null) {
                            call.respondText(JSON.stringify(GameNotFound(gameCode)), ContentType.Application.Json, HttpStatusCode.NotFound)
                        } else {
                            if (userExists(userId)) {
                                joinGame(userId, gameCode)
                                val newGameInfo = getGameInfo(gameCode, userId) ?: throw InvalidStateException("")
                                call.respondText(JSON.stringify(newGameInfo), ContentType.Application.Json)
                            } else {
                                call.respondText(JSON.stringify(UserNotFound(userId)), ContentType.Application.Json, HttpStatusCode.NotFound)
                            }
                        }
                    } catch (ex: InvalidParameterException) {
                        call.respondText(JSON.stringify(InvalidParameters()), ContentType.Application.Json, HttpStatusCode.BadRequest)
                    } catch (ex: InvalidStateException) {
                        call.respond(HttpStatusCode.InternalServerError, ActionFailure("Couldn't Join Player to Game"))
                    }
                }

                /**
                 * DELETE /game/prune
                 * Forces a prune of all empty games.
                 */
                delete("prune") {
                    val numPruned = pruneGames()
                    call.respondText(JSON.stringify(PruneOK(numPruned)), ContentType.Application.Json)
                }

            }

            /**
             * /location
             * Contains REST commands having to do with
             * locations and location sets.
             */
            route("/location") {

                /**
                 * GET /location
                 * Gets the standard set of locations.
                 */
                get {
                    call.respondText(JSON.stringify(Location::class.serializer().list, AllLocations.values.toList()), ContentType.Application.Json)
                }
            }

            /**
             * /test
             * Contains test REST commands.
             */
            route("/test") {
                /**
                 * GET /test/params/{CMD}
                 * Shows all the parameters passed into the method, including the path {CMD}.
                 */
                get("/params/{cmd}") {
                    call.respondText("${context.parameters}")
                }

                get("/locations") {
                    call.respondText("$AllLocations")
                }
            }
            get("/") {
                call.respondText("Hello, World!", ContentType.Text.Html)
            }

        }
    }
    server.start(wait = true)
}