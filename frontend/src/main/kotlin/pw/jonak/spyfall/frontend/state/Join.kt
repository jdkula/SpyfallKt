package pw.jonak.spyfall.frontend.state

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyUpFunction
import org.w3c.dom.HTMLInputElement
import pw.jonak.spyfall.common.JoinGameRequest
import pw.jonak.spyfall.common.LocationListRequest
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*
import kotlin.browser.document

class Join : RComponent<RProps, RState>() {

    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +getLocalization("ui", "page join screen")
        }
        div(classes = "row") {
            div(classes = "s12 input-field") {
                label {
                    +getLocalization("ui", "code")
                    attrs["htmlFor"] = "gamecode"
                    attrs {
                        id = "gamecodelabel"
                    }
                }
                input(type = InputType.text, classes = "teletype alllower") {
                    attrs["autocapitalize"] = "none"
                    attrs["autocorrect"] = "off"
                    attrs["autocomplete"] = "nope"
                    attrs {
                        id = "gamecode"
                        autoComplete = false
                        autoFocus = true
                        onKeyUpFunction = {
                            val keyevent = it.asDynamic()
                            val keycode = keyevent.which
                            if(keycode == 13) {
                                joinEvent()
                            }
                        }
                    }
                    attrs["aria-labelledby"] = "gamecodelabel"
                }
            }

            button(classes = "waves-effect waves-light btn col s12 m8 l10") {
                +getLocalization("ui", "join")
                attrs {
                    id = "joinButton"
                    onClickFunction = {
                        joinEvent()
                    }
                }
            }
            button(classes = "waves-effect waves-light btn grey col s12 offset-m1 m3 offset-l1 l1") {
                +getLocalization("ui", "back")
                attrs {
                    onClickFunction = {
                        toMainMenu()
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

fun joinEvent() {
    val gameCode = (document.getElementById("gamecode") as? HTMLInputElement)?.value?.trim()
    println("Found gamecode ${gameCode}")
    if (gameCode != null && gameCode.isNotEmpty()) {
        joinGame(gameCode)
    }
}

fun joinGame(gameCode: String) {
    leftGameCode = null
    socketClient.run {
        sendMessage(LocationListRequest().serialize())
        sendMessage(JoinGameRequest(appState.userInfo.userId, appState.userInfo.userName, gameCode).serialize())
    }
}