package pw.jonak.spyfall.frontend.state

import kotlinx.html.js.onClickFunction
import pw.jonak.spyfall.common.*
import pw.jonak.spyfall.frontend.appState
import pw.jonak.spyfall.frontend.elements.accessibleBullet
import pw.jonak.spyfall.frontend.elements.alert
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

    fun getTimeRemaining(inputTime: Double = Date().getTime()): Int = max(0, -(round(inputTime / 1000).toInt() - (props.game.startTime + props.game.gameLength)))

    override fun GameState.init(props: GameProps) {
        alertRendered = false
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
            } else if((tl + 3) % 60 > 6 && state.alertRendered) {
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
            +"Page Game Screen"
        }
        div(classes = "row") {
            span(classes = "col s12 center-align") {
                attrs["style"] = json("width" to "100%")
                +"Game Code: "
                span(classes = "teletype") { +props.info.gameCode }
            }
            if (props.game.isSpy) {
                h1(classes = "col s12 center-align") {
                    attrs["style"] = json("width" to "100%")
                    +"You're the spy!"
                }
            } else {
                h1(classes = "col s12 center-align") {
                    attrs["style"] = json("width" to "100%")
                    +"Location: "
                    b { +props.game.location }
                }
                h2(classes = "col s12 center-align") {
                    attrs["style"] = json("width" to "100%")
                    +"Role: "
                    b { +props.game.role }
                }
            }
        }
        p(classes = "accessibilityonly") {
            +"First player is ${props.info.userNameList[props.game.firstPlayer]}"
        }
        p {
            +"Player list:"
        }
        ul(classes = "collection") {
            props.info.userNameList.mapIndexed { index, value ->
                li(classes = "collection-item center-align") {
                    accessibleBullet()
                    +value
                    if (index == props.game.firstPlayer) {
                        sup(classes = "firstplayer") { i { +" 1st" } }
                    }
                }
            }
        }
        p {
            +"Possible locations:"
        }
        ul(classes = "collection") {
            props.possibleLocations.map { li(classes = "collection-item") {
                accessibleBullet()
                +it
            } }
        }
        p {
            +"Time Left: ${state.timeLeft}"
            attrs["aria-live"] = "off"
            attrs["role"] = "timer"
        }
        if (state.alertRendered) {
            val minsLeft = secondsToMinutes(getTimeRemaining()) + if (secondsToSecondsOfMinute(getTimeRemaining()) > 1) 1 else 0
            if (getTimeRemaining() == 0) {
                alert("Time is up!")
            } else {
                alert("$minsLeft minute${if (minsLeft != 1) "s" else ""} left!")
            }
        }

        div(classes = "row") {
            button(classes = "col s12 btn waves-effect waves-light") {
                +"Pause"
                attrs {
                    onClickFunction = {
                        pauseGame(props.info.gameCode)
                    }
                }
            }
            button(classes = "col s5 btn red waves-effect waves-light") {
                +"Stop"
                attrs {
                    onClickFunction = {
                        stopGame(props.info.gameCode)
                    }
                }
            }
            button(classes = "col s5 offset-s2 btn grey waves-effect waves-light") {
                +"Leave"
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

}

fun stopGame(gameCode: String) {
    socketClient.run {
        if (isConnected) {
            sendMessage(StopGameRequest(appState.userInfo.userId, gameCode).serialize())
        }
    }
}

fun leaveGame(gameCode: String) {
    socketClient.run {
        if (isConnected) {
            sendMessage(LeaveGameRequest(appState.userInfo.userId, gameCode).serialize())
        }
    }
    toMainMenu()
}