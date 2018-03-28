package pw.jonak.spyfall.frontend.state

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import pw.jonak.spyfall.common.JoinGameRequest
import pw.jonak.spyfall.common.LocationListRequest
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.ApplicationState
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.socketClient
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*
import kotlin.browser.document

class Join : RComponent<RProps, RState>() {

    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +"Page Join Screen"
        }
        form {
            attrs {
                autoComplete = false
                onSubmitFunction = {
                    it.preventDefault()
                    val gameCode = (document.getElementById("gamecode") as? HTMLInputElement)?.value
                    println("Found gamecode ${gameCode}")
                    if (gameCode != null) {
                        joinGame(gameCode)
                    }
                }
            }
            div(classes = "row") {
                div(classes = "s12 input-field") {
                    label {
                        +"Game Code"
                        attrs["htmlFor"] = "gamecode"
                        attrs {
                            id = "gamecodelabel"
                        }
                    }
                    input(type = InputType.text, classes = "login") {
                        attrs {
                            id = "gamecode"
                        }
                        attrs["aria-labelledby"] = "gamecodelabel"
                    }
                }

                input(type = InputType.submit, classes = "waves-effect waves-light btn col s12 m8 l10") {
                    attrs {
                        value = "Join"
                        id = "joinButton"
                    }
                }
                button(classes = "waves-effect waves-light btn grey col s12 offset-m1 m3 offset-l1 l1") {
                    +"Back"
                    attrs {
                        onClickFunction = {
                            toMainMenu()
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.join() = child(Join::class) {}
fun toJoinState() {
    appState = appState.changeState(ApplicationState.JOIN)
}

fun joinGame(gameCode: String) {
    socketClient.run {
        sendMessage(LocationListRequest().serialize())
        sendMessage(JoinGameRequest(appState.userInfo.userId, appState.userInfo.userName, gameCode).serialize())
    }
}