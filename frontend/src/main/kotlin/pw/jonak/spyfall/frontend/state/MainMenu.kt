@file:Suppress("RedundantVisibilityModifier")

package pw.jonak.spyfall.frontend.state

import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.common.CreateGameRequest
import pw.jonak.spyfall.common.LocationListRequest
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.ApplicationState
import pw.jonak.spyfall.frontend.CookieManager
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalization
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.elements.introHeader
import pw.jonak.spyfall.frontend.socketClient
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.span

/**
 * Defines the properties of the [MainMenu]
 * @property name The name of the logged-in person
 */
public interface MainMenuProps : RProps {
    var name: String
}

/**
 * This class represents the main menu (with create and join buttons),
 * and appears with [ApplicationState.MAINMENU].
 */
internal class MainMenu(props: MainMenuProps) : RComponent<MainMenuProps, RState>(props) {

    public override fun RBuilder.render() {
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

/**
 * Returns a [ReactElement] that's a Main Menu with the given [name].
 */
internal fun RBuilder.mainMenu(name: String) = child(MainMenu::class) {
    attrs.name = name
}

/**
 * Goes to the main menu, deleting the currentLobby cookie and setting [appState]'s currentLobby to null.
 */
internal fun toMainMenu() {
    CookieManager.delete("currentLobby")
    appState = appState.changeLobby(null)
    appState = appState.changeState(ApplicationState.MAINMENU)
}

/**
 * Creates a game by sending a [CreateGameRequest] and [LocationListRequest] to the server.
 */
private fun createGame() {
    socketClient.run {
        sendMessage(LocationListRequest().serialize())
        sendMessage(CreateGameRequest(appState.userInfo.userId).serialize())
    }
}