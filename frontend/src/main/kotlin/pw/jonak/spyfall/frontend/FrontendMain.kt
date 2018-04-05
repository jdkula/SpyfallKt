package pw.jonak.spyfall.frontend

import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.url.URL
import pw.jonak.spyfall.common.*
import pw.jonak.spyfall.frontend.ApplicationState.*
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalization
import pw.jonak.spyfall.frontend.LocalizationInformation.getLocalizationList
import pw.jonak.spyfall.frontend.elements.alert
import pw.jonak.spyfall.frontend.elements.footer
import pw.jonak.spyfall.frontend.elements.slider
import pw.jonak.spyfall.frontend.state.*
import react.dom.div
import react.dom.render
import react.dom.unmountComponentAtNode
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date
import kotlin.properties.Delegates

/**
 * Specifies the host the websocket should connect to. Defaults
 * to the same location as the server they're connecting to,
 * but can be overridden by adding a ?host=HOST_STR to the get parameters.
 */
private var host = window.location.host

/**
 * The [HTMLDivElement] that should render this app.
 */
private lateinit var appDiv: HTMLDivElement

/**
 * The [HTMLDivElement] that should render the loading bar.
 */
private lateinit var statusDiv: HTMLDivElement

/**
 * The last gameCode the user was a part of (and left) --
 * prevents a [LobbyInformation] sent before the server
 * recieves the [LeaveGameRequest] from erroneously
 * pulling the client back to the lobby/game view.
 */
internal var leftGameCode: String? = null

/**
 * A user whose information is always invalid --
 * used before a [UserRegistrationRequest] or
 * [EnsureUserRegistration] can be sent.
 */
internal val dummyUser = UserRegistrationInformation(-1, "", -1)

/**
 * The [WebSocketClient] responsible for communicating
 * with the HTTP server at [host].
 */
internal lateinit var socketClient: WebSocketClient

/**
 * Captures all the information about the current state
 * of the app. Writing a new value to it will automatically
 * re-render the page.
 */
internal var appState: GameState by Delegates.observable(GameState(dummyUser, LOGIN)) { _, oldValue, newValue ->
    println("Observed change! $oldValue ==> $newValue")
    updatePage()
}

/**
 * Used by [Game] to determine if the user should be told
 * they entered a non-existent game code. This is here
 * because all the processing of incoming packets
 * is done in this file.
 */
internal var lastGameCodeWasWrong: Boolean by Delegates.observable(false) { _, _, isWrong ->
    if (isWrong) {
        updatePage()
    }
}

/**
 * Sets up everything the [WebSocketClient] using the [host] GET parameter if necessary, then
 * sets up [onDomLoaded] to load when the DOM is ready.
 */
fun main(args: Array<String>) {
    val windowUrl = window.location.href
    val url = URL(windowUrl)
    if (url.searchParams != undefined) { // for MS Edge...
        val newHost = url.searchParams.get("host")
        if (newHost != null) {
            host = newHost
        }
    }

    socketClient = WebSocketClient("ws://$host/ws")

    document.addEventListener("DOMContentLoaded", onDomLoaded(url))
}

/**
 * Returns the handler that runs when the [socketClient] opens.
 * Responsible for ensuring cached UserInfo
 * is valid.
 */
private fun onSocketOpen(barDiv: Element): EventHandler = {
    if ("userInfo" in CookieManager) {
        val userInfo = CookieManager["userInfo"]?.deserialize()
        if (userInfo is UserRegistrationInformation) {
            socketClient.sendMessage(
                EnsureUserRegistration(
                    userInfo.userId,
                    userInfo.userName,
                    userInfo.sessionId
                ).serialize()
            )
        }
    }
    unmountComponentAtNode(barDiv)
}

/**
 * Returns the handler that runs when the [socketClient] disconnects.
 * Responsible for placing the alert slider at the top of the screen.
 */
private fun onSocketDisconnect(barDiv: Element): EventHandler = {
    render(barDiv) {
        alert("Connecting to Server!")
        slider("#ff3333")
    }
}

/**
 * Returns the handler responsible for processing incoming messages
 * through the [socketClient]. All core application logic is here.
 */
private fun onSocketMessage(url: URL): MessageHandler = {
    // Deserialize the message
    val msg = it.data?.toString()?.deserialize()

    // Process incoming [UserRegistrationInformation]
    if (msg is UserRegistrationInformation) {
        // Register new info
        appState = appState.changeUserInfo(msg)

        // If we were in the LOGIN state, we've now logged in!
        if (appState.state == LOGIN) {
            appState = appState.changeState(MAINMENU)
            CookieManager.add("userInfo" to appState.userInfo.serialize(), getDateXFromNow(days = 1))
        }

        // Process re-joining a game after a disconnect/closed window
        if (appState.userInfo != dummyUser  // Ensure we've logged in. Any of...
            && ("currentLobby" in CookieManager  // A currentLobby in CookieManager
                    || (url.searchParams != undefined && url.searchParams.get("gamecode") != null)  // Or it's in a search parameter
                    || appState.currentLobby?.gameHasStarted == true)  // Or it's cached from a "back" button or something
        ) {
            val gameCode = appState.currentLobby?.gameCode  // Get the game code with this precedence:
                    ?: url.searchParams.get("gamecode")  // Cached, Params, Cookie
                    ?: CookieManager["currentLobby"]
            if (gameCode != null) {  // If we found a game to join, join!
                joinGame(gameCode)
            }
        }
    }

    // Process incoming [GameNotFound]
    if (msg is GameNotFound) {
        lastGameCodeWasWrong = true  // Shows a validation error on the JOIN state.
        if (appState.state == GAME || appState.state == LOBBY) {  // Fixes a bug if the user comes back to a game that...
            toMainMenu()  // ...doesn't exist.
        }
    }

    // Process incoming [LobbyInformation]
    if (msg is LobbyInformation
        && msg.gameCode != leftGameCode  // Makes sure we don't re-join a game we just left
        && msg.packetId > (appState.currentLobby?.packetId ?: -1)  // Makes sure we're getting the latest info only.
    ) {
        // Sends a LocationListRequest if we're just entering a lobby or game
        if (appState.state != LOBBY && appState.state != GAME) {
            socketClient.sendMessage(LocationListRequest().serialize())
        }

        // Save the fact we're in a lobby just in case the connection is lost or the tab is closed.
        CookieManager.add("currentLobby" to msg.gameCode, getDateXFromNow(days = 1))

        // Cache the lobby info
        appState = appState.changeLobby(msg)

        // Switch to the appropriate state:
        appState = if (msg.gameHasStarted) {  // If the game has started...
            if (appState.state != GAME) {  // And we're just starting to get into a game...
                socketClient.sendMessage(LocationListRequest().serialize())  // Then get the location list again.
            }
            appState.changeState(GAME)  // In any case, change the state to GAME
        } else {
            appState.changeState(LOBBY)  // Otherwise, we're joining the lobby!
        }
    }

    // Process incoming [LocationListAnswer]s.
    if (msg is LocationListAnswer) {
        appState = appState.changeLocations(msg.locationList)  // Cache the location list.
    }
}

/**
 * Returns the callback that runs when the DOM loads.
 * Responsible for setting up localizations and all
 * message handlers for the [WebSocketClient].
 */
private fun onDomLoaded(url: URL): EventHandler = {
    val barDiv = document.getElementById("connectionstatus") as Element
    render(barDiv) { slider() }

    statusDiv = document.getElementById("statusbar") as HTMLDivElement
    appDiv = document.getElementById("app") as HTMLDivElement

    getLocalizationList()
    getLocalization("ui", "")
    getLocalization("locations", "")

    socketClient.onOpen(onSocketOpen(barDiv))
    socketClient.onClose(onSocketDisconnect(barDiv))
    socketClient.onMessage(onSocketMessage(url))

    updatePage()
}

/**
 * Updates the page in [appDiv] given the [appState]'s state.
 */
internal fun updatePage() {
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
        }

        // If we haven't rendered anything, then display the slider.
        if (appDiv.childNodes.length == 0) {
            slider()
        }

        // Add the footer.
        footer()
    }
}

/**
 * Gets a date some duration later from now.
 */
private fun getDateXFromNow(
    years: Int = 0,
    months: Int = 0,
    days: Int = 0,
    hours: Int = 0,
    minutes: Int = 0,
    seconds: Int = 0
): Date {
    val now = Date()
    return Date(
        now.getFullYear() + years,
        now.getMonth() + months,
        now.getDate() + days,
        now.getHours() + hours,
        now.getMinutes() + minutes,
        now.getSeconds() + seconds
    )
}