package pw.jonak.spyfall.frontend.state

import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.common.LobbyInformation
import pw.jonak.spyfall.common.StartGameRequest
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalization
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.elements.accessibleBullet
import pw.jonak.spyfall.frontend.elements.gameCode
import pw.jonak.spyfall.frontend.socketClient
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

interface LobbyProps : RProps {
    var lobbyInfo: LobbyInformation
}

class Lobby(props: LobbyProps) : RComponent<LobbyProps, RState>(props) {
    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +getLocalization("ui", "page lobby")
        }
        div(classes = "row") {
            gameCode(props.lobbyInfo.gameCode)
            h4(classes = "col s12 maxwidth center-align") {
                +getLocalization("ui", "awaiting")
            }
        }
        p {
            +getLocalization("ui", "player list")
        }
        ul(classes = "collection") {
            props.lobbyInfo.userNameList.map {
                li(classes = "collection-item center-align") {
                    accessibleBullet()
                    +it
                }
            }
        }
        div(classes = "row") {
            val disabled = props.lobbyInfo.userNameList.size < 3
            button(classes = "col s12 m8 l10 btn waves-effect waves-light ${if(disabled) "disabled" else ""}") {
                +if(disabled) getLocalization("ui", "need 3 players") else getLocalization("ui", "start game")
                attrs {
                    onClickFunction = {
                        if(!disabled) {
                            startGame(props.lobbyInfo.gameCode)
                        }
                    }
                }
            }
            button(classes = "col s12 offset-m1 m3 l1 offset-l1 grey btn waves-effect waves-light") {
                +getLocalization("ui", "leave")
                attrs {
                    onClickFunction = {
                        leaveGame(props.lobbyInfo.gameCode)
                        toMainMenu()
                    }
                }
            }
        }
    }

    private fun startGame(gameCode: String) {
        socketClient.sendMessage(StartGameRequest(appState.userInfo.userId, gameCode).serialize())
    }
}

fun RBuilder.lobby(lobbyInfo: LobbyInformation) = child(Lobby::class) {
    attrs.lobbyInfo = lobbyInfo
}