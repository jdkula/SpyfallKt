package pw.jonak.spyfall.frontend.state

import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.common.*
import pw.jonak.spyfall.frontend.MessageHandler
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.socketClient
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*
import kotlin.js.json

interface LobbyProps : RProps {
    var lobbyInfo: LobbyInformation
}

class Lobby(props: LobbyProps) : RComponent<LobbyProps, RState>(props) {
    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +"Pre game Lobby"
        }
        div(classes = "row") {
            span(classes = "col s12 center-align") {
                attrs["style"] = json("width" to "100%")
                +"Game Code: "
                span(classes = "teletype") { +props.lobbyInfo.gameCode }
            }
            h4(classes = "col s12 center-align") {
                attrs["style"] = json("width" to "100%")
                +"Awaiting Players..."
            }
        }
        p {
            +"Players:"
        }
        ul(classes = "collection") {
            props.lobbyInfo.userNameList.map { li(classes = "collection-item center-align") { +it } }
        }
        div(classes = "row") {
            button(classes = "col s12 m8 l10 btn waves-effect waves-light") {
                +"Start"
                attrs {
                    onClickFunction = {
                        startGame(props.lobbyInfo.gameCode)
                    }
                }
            }
            button(classes = "col s12 offset-m1 m3 l1 offset-l1 grey btn waves-effect waves-light") {
                +"Leave"
                attrs {
                    onClickFunction = {
                        socketClient.sendMessage(LeaveGameRequest(appState.userInfo.userId, props.lobbyInfo.gameCode).serialize())
                        toMainMenu()

                    }
                }
            }
        }
    }
}

fun RBuilder.lobby(lobbyInfo: LobbyInformation) = child(Lobby::class) {
    attrs.lobbyInfo = lobbyInfo
}

fun toLobbyState(gameCode: String, then: MessageHandler? = null) {
    socketClient.run {
        if (isConnected) {
            sendMessage(LobbyInformationRequest(appState.userInfo.userId, gameCode).serialize())
        }
    }
}

fun startGame(gameCode: String) {
    socketClient.sendMessage(StartGameRequest(appState.userInfo.userId, gameCode).serialize())
}