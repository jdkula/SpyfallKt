package pw.jonak.spyfall.frontend.state

import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.common.CreateGameRequest
import pw.jonak.spyfall.common.LocationListRequest
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.*
import pw.jonak.spyfall.frontend.elements.introHeader
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.span


interface MainMenuProps : RProps {
    var name: String
}

class MainMenu(props: MainMenuProps) : RComponent<MainMenuProps, RState>(props) {

    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +getLocalization("ui", "page main screen")
        }
        button(classes = "linkbutton") {
            +getLocalization("ui", "logout")
            attrs {
                id = "logoutButton"
                onClickFunction = {
                    toLoginState()
                }
            }
        }
        introHeader({
            +"${getLocalization("ui", "hello name")} ${props.name}!"
        }, {})
        div(classes = "row") {
            button(classes = "btn col s12 l5 waves-effect waves-light") {
                +getLocalization("ui", "join game")
                attrs {
                    id = "joinButton"
                    onClickFunction = {
                        toJoinState()
                    }
                }
            }
            button(classes = "btn col s12 l5 offset-l2 waves-effect waves-light") {
                +getLocalization("ui", "create game")
                attrs {
                    id = "createButton"
                    onClickFunction = {
                        createGame()
                    }
                }
            }
        }
    }
}


fun RBuilder.mainMenu(name: String) = child(MainMenu::class) {
    attrs.name = name
}

fun toMainMenu() {
    CookieManager.delete("currentLobby")
    appState = appState.changeLobby(null)
    appState = appState.changeState(ApplicationState.MAINMENU)
}

fun createGame() {
    socketClient.run {
        sendMessage(LocationListRequest().serialize())
        sendMessage(CreateGameRequest(appState.userInfo.userId).serialize())
    }
}