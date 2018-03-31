package pw.jonak.spyfall.frontend

import kotlinext.js.getOwnPropertyNames
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.url.URL
import org.w3c.xhr.JSON
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import pw.jonak.spyfall.common.*
import pw.jonak.spyfall.frontend.ApplicationState.*
import pw.jonak.spyfall.frontend.elements.alert
import pw.jonak.spyfall.frontend.elements.footer
import pw.jonak.spyfall.frontend.elements.slider
import pw.jonak.spyfall.frontend.state.*
import react.dom.div
import react.dom.render
import react.dom.unmountComponentAtNode
import kotlin.browser.document
import kotlin.browser.window
import kotlin.collections.set
import kotlin.js.Date
import kotlin.js.Json
import kotlin.properties.Delegates

var localizationName: String by Delegates.observable("en_US") { _, _, _ ->
    localizations = HashMap()
}
var localizations: HashMap<String, Json> by Delegates.observable(HashMap()) { _, _, _ ->
    updatePage()
}
var localizationOptions: Map<String, String> = mapOf()
var host = window.location.host
lateinit var socketClient: WebSocketClient
lateinit var appDiv: HTMLDivElement
lateinit var statusDiv: HTMLDivElement
var leftGameCode: String? = null
val dummyUser = UserRegistrationInformation(-1, "")
var appState: GameState by Delegates.observable(GameState(dummyUser, LOGIN)) { _, oldValue, newValue ->
    println("Observed change! $oldValue ==> $newValue")
    updatePage()
}

fun main(args: Array<String>) {
    val windowUrl = window.location.href
    val url = URL(windowUrl)
    if (url.searchParams != undefined) { // for MS Edge...
        val newHost = url.searchParams.get("host")
        if (newHost != null) {
            println("USING HOST $newHost!")
            host = newHost
        }
    }
    document.addEventListener("DOMContentLoaded", {
        val barDiv = document.getElementById("connectionstatus")
        render(barDiv) { slider() }
        statusDiv = document.getElementById("statusbar") as HTMLDivElement
        appDiv = document.getElementById("app") as HTMLDivElement

        getLocalizationList()
        getLocalization("ui", "")
        getLocalization("locations", "")

        socketClient = WebSocketClient("ws://$host/ws") {
            println("First connect!")
        }

        socketClient.onOpen {
            println("Reconnected!")
            if ("userInfo" in CookieManager) {
                println(CookieManager["userInfo"])
                val userInfo = CookieManager["userInfo"]?.deserialize()
                if (userInfo is UserRegistrationInformation) {
                    socketClient.sendMessage(EnsureUserRegistration(userInfo.userId, userInfo.userName).serialize())
                }
            }
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
            if (msg is UserRegistrationInformation) {
                appState = appState.changeUserInfo(msg)
                println("Found user ID ${appState.userInfo.userId}")
                if (appState.state == LOGIN) {
                    // We've now logged in!
                    println("Logged In!")
                    appState = appState.changeState(MAINMENU)
                    val now = Date()
                    val expiry = Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, now.getHours(), now.getMinutes(), now.getSeconds())
                    CookieManager.add("userInfo" to appState.userInfo.serialize(), expiry)
                    if ("currentLobby" in CookieManager) {
                        println(CookieManager["currentLobby"])
                        val lobbyInfo = CookieManager["currentLobby"]?.deserialize()
                        if (lobbyInfo is LobbyInformation && appState.userInfo != dummyUser) {
                            joinGame(lobbyInfo.gameCode)
                        }
                    }
                }
            }
            if (msg is LobbyInformation && msg.gameCode != leftGameCode && msg.packetId > (appState.currentLobby?.packetId
                            ?: -1)) {
                if (appState.state != LOBBY && appState.state != GAME) {
                    socketClient.sendMessage(LocationListRequest().serialize())
                }
                val now = Date()
                val expiry = Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, now.getHours(), now.getMinutes(), now.getSeconds())
                CookieManager.add("currentLobby" to msg.serialize(), expiry)
                appState = appState.changeLobby(msg)
                appState = if (msg.gameHasStarted) {
                    if (appState.state != GAME) {
                        socketClient.sendMessage(LocationListRequest().serialize())
                    }
                    appState.changeState(GAME)
                } else {
                    appState.changeState(LOBBY)
                }
            }
            if (msg is LocationListAnswer) {
                appState = appState.changeLocations(msg.locationList)
            }
            println("FINISHED WITH M#$counter")
        }
        updatePage()
    })
}

fun updatePage() {
    render(appDiv) {
        when (appState.state) {
            LOGIN -> {
                div {
                    login(if (appState.userInfo != dummyUser) appState.userInfo.userName else null)
                }

            }
            MAINMENU -> {
                println("Rendering Main Menu...")
                div {
                    mainMenu(appState.userInfo.userName)
                }

            }
            JOIN -> {
                div {
                    join()
                }
            }
            LOBBY -> {
                div {
                    appState.currentLobby?.let { lobby(it) }
                }
            }
            GAME -> {
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
            ADMINMENU -> {
                div {
                    adminMenu()
                }
            }
        }

        if (appDiv.childNodes.length == 0) {
            slider()
        }
        footer()
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


fun getLocalizationList() {
    val xhr = XMLHttpRequest()
    xhr.onload = {
        val status = xhr.status
        if (status == 200.toShort()) {
            println("Got localization list!")
            val mapJson = xhr.response.unsafeCast<Json>()
            val props = mapJson.getOwnPropertyNames()
            println("PROPS = $props")
            localizationOptions = props.map { it to mapJson[it].unsafeCast<String>() }.toMap()
            updatePage()
        } else {
            println("ERROR!! Got status ${xhr.status}")
        }
    }
    xhr.open("GET", "${window.location.protocol}//${window.location.host}${window.location.pathname}/localization/localizations.json")
    xhr.responseType = XMLHttpRequestResponseType.JSON
    println("Sending request!")
    xhr.send()
}

val xhrsOut = HashSet<String>()

fun getLocalization(localizationGroup: String, localizationElement: String): String {
    if (localizationGroup !in localizations && localizationGroup !in xhrsOut) {
        xhrsOut += localizationGroup
        val xhr = XMLHttpRequest()
        xhr.onload = {
            val status = xhr.status
            xhrsOut -= localizationGroup
            if (status == 200.toShort() && localizationGroup !in localizations) {
                println("Got localization!")
                localizations[localizationGroup] = xhr.response.unsafeCast<Json>()
                updatePage()
            } else {
                println("ERROR!! Got status ${xhr.status}")
            }
        }
        xhr.open("GET", "${window.location.protocol}//${window.location.host}${window.location.pathname}/localization/$localizationName/$localizationGroup.json")
        xhr.responseType = XMLHttpRequestResponseType.JSON
        println("Sending request!")
        xhr.send()
    }
    val x = localizations[localizationGroup]
    val y = x?.get(localizationElement)
    val z = y as? String
    return z ?: localizationElement
}