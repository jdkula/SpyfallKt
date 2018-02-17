import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.security.InvalidParameterException

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        routing {
            route("/execute") {
                get("shutdown") {
                    call.respondText("Shutting down...", ContentType.Text.Html)
                    System.exit(0)
                }
            }
            route("/user") {
                get("new") {
                    try {
                        val userId: Int = createUser(context.parameters["name"] ?: throw InvalidParameterException())

                        call.respondText("${context.parameters} -> User created: $userId", ContentType.Text.Html)
                    } catch (ex: AbstractMethodError) {
                        call.respond(HttpStatusCode.InternalServerError, "DB Failure...")
                    } catch (ex: InvalidParameterException) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid parameters...")
                    }
                }
                get("info") {

                }
            }
            route("/test") {
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