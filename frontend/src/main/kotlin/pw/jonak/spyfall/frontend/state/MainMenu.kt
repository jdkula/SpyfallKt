package pw.jonak.spyfall.frontend.state

import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import pw.jonak.spyfall.common.CreateGameRequest
import pw.jonak.spyfall.common.LocationListRequest
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.ApplicationState
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.elements.introHeader
import pw.jonak.spyfall.frontend.socketClient
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.form
import react.dom.span


interface MainMenuProps : RProps {
    var name: String
}

class MainMenu(props: MainMenuProps) : RComponent<MainMenuProps, RState>(props) {

    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +"Page Main Screen"
        }
        button(classes = "linkbutton") {
            +"Logout"
            attrs {
                id = "logoutButton"
                onClickFunction = {
                    toLoginState()
                }
            }
        }
        introHeader({
            +"Hello, ${props.name}!"
        }, {})
        form {
            attrs {
                onSubmitFunction = {
                    it.preventDefault()
                }
            }
            div(classes = "row") {
                button(classes = "btn col s12 l5 waves-effect waves-light") {
                    +"Join a Game"
                    attrs {
                        id = "joinButton"
                        onClickFunction = {
                            toJoinState()
                        }
                    }
                }
                button(classes = "btn col s12 l5 offset-l2 waves-effect waves-light") {
                    +"Create a game"
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
}


fun RBuilder.mainMenu(name: String) = child(MainMenu::class) {
    attrs.name = name
}

fun toMainMenu() {
    appState = appState.changeState(ApplicationState.MAINMENU)
}

fun createGame() {
    socketClient.run {
        sendMessage(LocationListRequest().serialize())
        sendMessage(CreateGameRequest(appState.userInfo.userId).serialize())
    }
}