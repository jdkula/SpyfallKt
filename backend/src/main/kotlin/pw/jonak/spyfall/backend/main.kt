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
import io.ktor.websocket.WebSockets
import kotlinx.serialization.json.JSON
import pw.jonak.spyfall.backend.SqlHelper.createGame
import pw.jonak.spyfall.backend.SqlHelper.createTables
import pw.jonak.spyfall.backend.SqlHelper.createUser
import pw.jonak.spyfall.backend.SqlHelper.ensureRegistered
import pw.jonak.spyfall.backend.SqlHelper.getGameInfo
import pw.jonak.spyfall.backend.SqlHelper.joinGame
import pw.jonak.spyfall.backend.SqlHelper.pruneGames
import pw.jonak.spyfall.backend.SqlHelper.prunePlayers
import pw.jonak.spyfall.backend.SqlHelper.userExists
import pw.jonak.spyfall.common.*
import java.security.InvalidParameterException
import java.time.Duration

val server = SpyfallGameServer()

/**
 * Sets up the server, and contains routing information for REST.
 */
fun main(args: Array<String>) {
    createTables()
    val server = embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(5L)
        }
        routing {
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
                    call.respondText(JSON.stringify(ServerShutdownOK), ContentType.Application.Json)
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
                        val userInfo: UserRegistrationInformation = createUser(userName!!)
                        call.respondText(JSON.stringify(userInfo), ContentType.Application.Json, HttpStatusCode.Created)
                    } catch (ex: AbstractMethodError) {
                        call.respondText(JSON.stringify(ActionFailure("DB Error")), ContentType.Application.Json, HttpStatusCode.InternalServerError)
                    } catch (ex: InvalidParameterException) {
                        call.respondText(JSON.stringify(InvalidParameters), ContentType.Application.Json, HttpStatusCode.BadRequest)
                    }
                }

                /**
                 * POST /user/ensure?id=INT&name=STRING
                 * Ensures that the user [id] is registered; if not,
                 * registers it. Returns [UserRegistrationInformation].
                 */
                post("ensure") {
                    try {
                        val userId = context.parameters["id"]?.toIntOrNull()
                        val userName = context.parameters["name"]
                        if (userId == null || userName.isNullOrEmpty()) throw InvalidParameterException()
                        val userInfo = ensureRegistered(userId, userName!!)
                        val statusCode = if (userInfo.user_id == userId) HttpStatusCode.OK else HttpStatusCode.Created
                        call.respondText(JSON.stringify(userInfo), ContentType.Application.Json, statusCode)
                    } catch (ex: InvalidParameterException) {
                        call.respondText(JSON.stringify(InvalidParameters), ContentType.Application.Json, HttpStatusCode.BadRequest)
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
                        call.respondText(JSON.stringify(InvalidParameters), ContentType.Application.Json, HttpStatusCode.BadRequest)
                    }
                }

                /**
                 * DELETE /user/prune
                 * Forces a prune of all expired users.
                 */
                delete("prune") {
                    val numPruned = prunePlayers()
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
                        call.respondText(JSON.stringify(InvalidParameters), ContentType.Application.Json, HttpStatusCode.BadRequest)
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
                        val gameInformation = getGameInfo(gameCode) ?: throw InvalidStateException("")
                        call.respondText(JSON.stringify(gameInformation), ContentType.Application.Json, HttpStatusCode.Created)
                    } catch (ex: InvalidStateException) {
                        call.respondText(JSON.stringify(GameNotCreatedError), ContentType.Application.Json, HttpStatusCode.InternalServerError)
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
                        call.respondText(JSON.stringify(InvalidParameters), ContentType.Application.Json, HttpStatusCode.BadRequest)
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
             * /test
             * Contains test REST commands.
             */
            route("/test") {
                /**
                 * GET /test/{CMD}
                 * Shows all the parameters passed into the method, including the path {CMD}.
                 */
                get("{cmd}") {
                    call.respondText("${context.parameters}")
                }
            }
            get("/") {
                call.respondText("Hello, World!", ContentType.Text.Html)
            }

        }
    }
    server.start(wait = true)
}