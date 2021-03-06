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
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalization
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*
import kotlin.browser.document

/**
 * The screen for [ApplicationState.JOIN].
 * Allows users to join games.
 */
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
                input(type = InputType.text, classes = "teletype alllower ${if(lastGameCodeWasWrong) "invalid" else ""}") {
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
                span(classes = "helper-text") {
                    attrs["data-error"] = getLocalization("ui", "game code not found")
                    attrs["data-success"] = ""
                }
            }

            button(classes = "waves-effect waves-light btn col s12 m8 l10") {
                +getLocalization("ui", "join")
                attrs {
                    id = "joinButton"
                    onClickFunction = {
                        lastGameCodeWasWrong = false
                        joinEvent()
                    }
                }
            }
            button(classes = "waves-effect waves-light btn grey col s12 offset-m1 m3 offset-l1 l1") {
                +getLocalization("ui", "back")
                attrs {
                    onClickFunction = {
                        lastGameCodeWasWrong = false
                        toMainMenu()
                    }
                }
            }
        }
    }

    private fun joinEvent() {
        val gameCode = (document.getElementById("gamecode") as? HTMLInputElement)?.value?.trim()
        println("Found gamecode ${gameCode}")
        if (gameCode != null && gameCode.isNotEmpty()) {
            joinGame(gameCode)
        }
    }
}

/** Allows the use of [Join] in [RBuilder] contexts */
internal fun RBuilder.join() = child(Join::class) {}

/** Shows the [Join] page, resetting [lastGameCodeWasWrong] */
internal fun toJoinState() {
    lastGameCodeWasWrong = false
    appState = appState.changeState(ApplicationState.JOIN)
}

/**
 * Joins a new game, setting [leftGameCode] to null
 * and sending a [LocationListRequest] and [JoinGameRequest] to the server.
 */
internal fun joinGame(gameCode: String) {
    leftGameCode = null
    socketClient.run {
        sendMessage(LocationListRequest().serialize())
        sendMessage(JoinGameRequest(appState.userInfo.userId, appState.userInfo.userName, gameCode).serialize())
    }
}