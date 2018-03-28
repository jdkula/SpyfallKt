package pw.jonak.spyfall.frontend.state

import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.common.AdminAction
import pw.jonak.spyfall.common.AdminActionType
import pw.jonak.spyfall.common.serialize
import pw.jonak.spyfall.frontend.ApplicationState
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.socketClient
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div

class AdminMenu : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        div {
            +"Admin Menu"
        }
        button {
            +"Shutdown Server"
            attrs {
                onClickFunction = {
                    socketClient.sendMessage(AdminAction(AdminActionType.SHUTDOWN).serialize())
                }
            }
        }

        button {
            +"Prune Games"
            attrs {
                onClickFunction = {
                    socketClient.sendMessage(AdminAction(AdminActionType.PRUNE_GAMES).serialize())
                }
            }
        }

        button {
            +"Prune Users"
            attrs {
                onClickFunction = {
                    socketClient.sendMessage(AdminAction(AdminActionType.PRUNE_USERS).serialize())
                }
            }
        }

        button {
            +"Back"
            attrs {
                onClickFunction = {
                    toLoginState()
                }
            }
        }
    }

}

fun RBuilder.adminMenu() = child(AdminMenu::class) {

}

fun toAdminMenu() {
    appState = appState.changeState(ApplicationState.ADMINMENU)
}