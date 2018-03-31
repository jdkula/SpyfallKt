package pw.jonak.spyfall.frontend.state

import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.common.*
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.elements.accessibleBullet
import pw.jonak.spyfall.frontend.elements.alert
import pw.jonak.spyfall.frontend.elements.listEntry
import pw.jonak.spyfall.frontend.getLocalization
import pw.jonak.spyfall.frontend.leftGameCode
import pw.jonak.spyfall.frontend.socketClient
import react.*
import react.dom.*
import kotlin.browser.window
import kotlin.js.Date
import kotlin.js.json
import kotlin.math.max
import kotlin.math.round

interface GameProps : RProps {
    var info: LobbyInformation
    var game: GameInformation
    var possibleLocations: List<String>
}

interface GameState : RState {
    var timeLeft: String
    var intervalId: Int
    var alertRendered: Boolean
    var gameWasUnpaused: Boolean
}

class Game(props: GameProps) : RComponent<GameProps, GameState>(props) {
    fun secondsToMinutes(duration: Int): Int = (duration / 60) % 60
    fun secondsToSecondsOfMinute(duration: Int): Int = duration % 60

    fun sToTime(duration: Int): String {
        val s = secondsToSecondsOfMinute(duration)
        val m = secondsToMinutes(duration)

        val mstr = if (m < 10) "0$m" else m.toString()
        val sstr = if (s < 10) "0$s" else s.toString()

        return "$mstr:$sstr"
    }

    fun getTimeRemaining(inputTime: Double = props.game.pauseTime?.toDouble()
            ?: (Date().getTime() / 1000)): Int = max(0, -(round(inputTime).toInt() - (props.game.startTime + props.game.gameLength)))

    override fun GameState.init(props: GameProps) {
        alertRendered = false
        gameWasUnpaused = false
        timeLeft = sToTime(getTimeRemaining())
        intervalId = window.setInterval({
            setState {
                timeLeft = sToTime(getTimeRemaining())
            }
            val tl = secondsToSecondsOfMinute(getTimeRemaining())
            if ((tl + 3) % 60 <= 6
                    && !state.alertRendered
                    && (secondsToMinutes(getTimeRemaining()) + if (secondsToSecondsOfMinute(getTimeRemaining()) > 1) 1 else 0) != secondsToMinutes(props.game.gameLength)) {
                setState {
                    alertRendered = true
                }
            } else if ((tl + 3) % 60 > 6 && state.alertRendered) {
                setState {
                    alertRendered = false
                }
            }
        }, 1000)
    }

    override fun componentWillUnmount() {
        window.clearInterval(state.intervalId)
    }

    override fun RBuilder.render() {
        span(classes = "accessibilityonly") {
            +getLocalization("ui", "page game")
        }
        div(classes = "row") {
            span(classes = "col s12 center-align") {
                attrs["style"] = json("width" to "100%")
                +"${getLocalization("ui", "code")}: "
                span(classes = "teletype") { +props.info.gameCode }
            }
            if (props.game.isSpy) {
                h1(classes = "col s12 center-align") {
                    attrs["style"] = json("width" to "100%")
                    +getLocalization("ui", "you are the spy")
                }
            } else {
                h1(classes = "col s12 center-align") {
                    attrs["style"] = json("width" to "100%")
                    +"${getLocalization("ui", "location")} "
                    b { +getLocalization("locations", props.game.location) }
                }
                h2(classes = "col s12 center-align") {
                    attrs["style"] = json("width" to "100%")
                    +"${getLocalization("ui", "role")} "
                    b { +getLocalization(props.game.location, props.game.role) }
                }
                p(classes = "col s12 center-align") {
                    attrs["style"] = json("width" to "100%")
                    +getLocalization("ui", "you are not the spy")
                }
            }
        }
        val firstInGame = props.game.firstPlayer >= 0 && props.game.firstPlayer < props.info.userNameList.size
        if (firstInGame) {
            p(classes = "accessibilityonly") {
                +"${getLocalization("ui", "first accessible")} ${props.info.userNameList[props.game.firstPlayer]}"
            }
        }
        p {
            +getLocalization("ui", "player list")
        }
        ul(classes = "collection") {
            props.info.userNameList.mapIndexed { index, value ->
                listEntry("collection-item center-align") {
                    accessibleBullet()
                    +value
                    if (index == props.game.firstPlayer) {
                        sup(classes = "firstplayer") { i { +" 1st" } }
                    }
                }
            }
        }
        p {
            +getLocalization("ui", "location list")
        }
        ul(classes = "collection row") {
            props.possibleLocations.map { location ->
                listEntry("col s6 collection-item") {
                    accessibleBullet()
                    +getLocalization("locations", location)
                }
            }
        }
        p {
            +"${if (props.game.isPaused) "${getLocalization("ui", "paused")}: " else "${getLocalization("ui", "Time Left")}: "} ${state.timeLeft}"
            attrs["aria-live"] = "off"
            attrs["role"] = "timer"
        }
        if (state.alertRendered) {
            val minsLeft = secondsToMinutes(getTimeRemaining()) + if (secondsToSecondsOfMinute(getTimeRemaining()) > 1) 1 else 0
            if (getTimeRemaining() == 0) {
                alert("Time is up!")
            } else {
                alert("$minsLeft ${getLocalization("ui", "minute")}${if (minsLeft != 1) "s" else ""} ${getLocalization("ui", "left")}")
            }
        }
        if (props.game.isPaused) {
            alert(getLocalization("ui", "game was paused"))
        } else if (state.gameWasUnpaused) {
            alert(getLocalization("ui", "game was unpaused"))
        }

        div(classes = "row") {
            attrs["aria-live"] = "polite"
            button(classes = "col s12 btn waves-effect waves-light") {
                +if (props.game.isPaused) getLocalization("ui", "unpause") else getLocalization("ui", "pause")
                attrs {
                    onClickFunction = {
                        if (props.game.isPaused) {
                            unpauseGame(props.info.gameCode)
                            state.gameWasUnpaused = true
                        } else {
                            pauseGame(props.info.gameCode)
                        }
                    }
                }
            }
            button(classes = "col s5 btn red waves-effect waves-light") {
                +getLocalization("ui", "stop game")
                attrs {
                    onClickFunction = {
                        stopGame(props.info.gameCode)
                    }
                }
            }
            button(classes = "col s5 offset-s2 btn grey waves-effect waves-light") {
                +getLocalization("ui", "leave game")
                attrs {
                    onClickFunction = {
                        leaveGame(props.info.gameCode)
                    }
                }
            }
        }
    }
}


fun RBuilder.game(info: LobbyInformation, game: GameInformation, possibleLocations: List<String>) = child(Game::class) {
    attrs.info = info
    attrs.game = game
    attrs.possibleLocations = possibleLocations.sorted()
}

fun toGameState() {
    socketClient.run {
        if (isConnected) {
            appState.currentLobby?.let { lobby ->
                sendMessage(LocationListRequest().serialize())
                sendMessage(LobbyInformationRequest(appState.userInfo.userId, lobby.gameCode).serialize())
            }
        }
    }
}

fun pauseGame(gameCode: String) {
    socketClient.sendMessage(PauseGameRequest(appState.userInfo.userId, gameCode).serialize())
}

fun unpauseGame(gameCode: String) {
    socketClient.sendMessage(UnpauseGameRequest(appState.userInfo.userId, gameCode).serialize())
}

fun stopGame(gameCode: String) {
    socketClient.run {
        if (isConnected) {
            sendMessage(StopGameRequest(appState.userInfo.userId, gameCode).serialize())
        }
    }
}

fun leaveGame(gameCode: String) {
    socketClient.sendMessage(LeaveGameRequest(appState.userInfo.userId, gameCode).serialize())
    leftGameCode = gameCode
    toMainMenu()
}