package pw.jonak.spyfall.frontend

import org.w3c.dom.HTMLDivElement
import pw.jonak.spyfall.common.*
import pw.jonak.spyfall.frontend.ApplicationState.*
import pw.jonak.spyfall.frontend.elements.alert
import pw.jonak.spyfall.frontend.elements.slider
import pw.jonak.spyfall.frontend.state.*
import react.dom.div
import react.dom.render
import react.dom.unmountComponentAtNode
import kotlin.browser.document
import kotlin.properties.Delegates

const val host = "localhost:8080"
lateinit var socketClient: WebSocketClient
lateinit var appDiv: HTMLDivElement
lateinit var statusDiv: HTMLDivElement
val dummyUser = UserRegistrationInformation(-1, "")
var appState: GameState by Delegates.observable(GameState(dummyUser, LOGIN)) { _, oldValue, newValue ->
    println("Observed change! $oldValue ==> $newValue")
    updatePage()
}

fun main(args: Array<String>) {
    document.addEventListener("DOMContentLoaded", {
        val barDiv = document.getElementById("connectionstatus")
        render(barDiv) { slider() }
        statusDiv = document.getElementById("statusbar") as HTMLDivElement
        appDiv = document.getElementById("app") as HTMLDivElement

        socketClient = WebSocketClient("ws://$host/ws") {
            println("First connect!")
        }

        socketClient.onOpen {
            println("Reconnected!")
            unmountComponentAtNode(barDiv)
        }

        socketClient.onClose {
            render(barDiv) {
                alert("Connecting to Server!")
                slider("#ff3333")
            }
        }

        var counter = -1

        socketClient.onMessage {
            counter += 1
            println("RECEIVING $counter: ${it.data?.toString()}")
            val msg = it.data?.toString()?.deserialize()
            if(msg is UserRegistrationInformation) {
                appState = appState.changeUserInfo(msg)
                println("Found user ID ${appState.userInfo.userId}")
                if(appState.state == LOGIN) {
                    // We've now logged in!
                    println("Logged In!")
                    appState = appState.changeState(MAINMENU)
                }
            }
            if(msg is LobbyInformation) {
                appState = appState.changeLobby(msg)
                if(msg.gameHasStarted) {
                    appState = appState.changeState(GAME)
                    socketClient.sendMessage(LocationListRequest().serialize())
                } else {
                    appState = appState.changeState(LOBBY)
                }
            }
            if(msg is LocationListAnswer) {
                appState = appState.changeLocations(msg.locationList)
            }
            println("FINISHED WITH M#$counter")
        }
        updatePage()
    })
}

fun updatePage() {
    when (appState.state) {
        LOGIN -> {
            render(appDiv) {
                div {
                    login(if (appState.userInfo != dummyUser) appState.userInfo.userName else null)
                }
            }
        }
        MAINMENU -> {
            println("Rendering Main Menu...")
            render(appDiv) {
                div {
                    mainMenu(appState.userInfo.userName)
                }
            }
        }
        JOIN -> {
            render(appDiv) {
                div {
                    join()
                }
            }
        }
        LOBBY -> render(appDiv) {
            div {
                appState.currentLobby?.let { lobby(it) }
            }
        }
        GAME -> render(appDiv) {
            div {
                appState.currentLobby?.let { lobby ->
                    lobby.gameInformation?.let { game ->
                        appState.locationList?.let { locations ->
                            game(lobby, game, locations)
                        }
                    }
                }
            }
        }
        ADMINMENU -> render(appDiv) {
            div {
                adminMenu()
            }
        }
    }

    if(appDiv.childNodes.length == 0) {
        render(statusDiv) {
            slider()
        }
    }
}

data class GameState(
        val userInfo: UserRegistrationInformation,
        val state: ApplicationState,
        val currentLobby: LobbyInformation? = null,
        val locationList: List<String>? = null
) {
    fun changeState(newState: ApplicationState): GameState {
        return GameState(userInfo, newState, currentLobby, locationList)
    }

    fun changeUserInfo(newUserInfo: UserRegistrationInformation): GameState {
        return GameState(newUserInfo, state, currentLobby, locationList)
    }

    fun changeLobby(newLobby: LobbyInformation?): GameState {
        return GameState(userInfo, state, newLobby, locationList)
    }

    fun changeLocations(newLocations: List<String>?): GameState {
        return GameState(userInfo, state, currentLobby, newLocations)
    }
}