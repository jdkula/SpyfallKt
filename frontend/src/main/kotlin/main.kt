import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.p
import kotlinx.serialization.json.JSON
import org.w3c.dom.*
import org.w3c.xhr.XMLHttpRequest
import pw.jonak.spyfall.common.GameInformation
import pw.jonak.spyfall.common.UserRegistrationInformation
import kotlin.browser.document
import kotlin.dom.clear

var userId: Int? = null
var gameCode: String? = null
var ws: WebSocket? = null
const val host = "jonak-latitude.stanford.edu"

fun main(args: Array<String>) {
    val div = document.getElementById("app") as HTMLDivElement
    val nameSubmitButton = document.getElementById("namesubmitbutton") as HTMLInputElement
    nameSubmitButton.onclick = {
        val nameForm = document.getElementById("nametext") as HTMLInputElement
        val userRequest = XMLHttpRequest()
        userRequest.open("POST", "http://$host:8080/user/new?name=${nameForm.value}")
        userRequest.send()
        userRequest.onload = {
            div.append {
                div {
                    +userRequest.responseText
                }
            }
            userId = JSON.parse<UserRegistrationInformation>(userRequest.responseText).user_id
            false
        }
        false
    }
    div.append {
        p {
            +document.cookie
        }
    }

    val gameSubmitButton = document.getElementById("gamesubmitbutton") as HTMLInputElement
    gameSubmitButton.onclick = {
        val gameForm = document.getElementById("gametext") as HTMLInputElement
        if(gameForm.value == "") {
            val gameRequest = XMLHttpRequest()
            gameRequest.open("POST", "http://$host:8080/game/create")
            gameRequest.send()
            gameRequest.onload = {
                div.append {
                    p {
                        +(gameRequest.response?.toString() ?: "")
                    }
                }
                gameCode = JSON.parse<GameInformation>(gameRequest.responseText).game_code
                false
            }
        } else {
            gameCode = gameForm.value
        }
        false
    }

    val connectButton = document.getElementById("connectbutton") as HTMLInputElement
    connectButton.onclick = {
        if(userId != null && gameCode != null && ws == null) {
            ws = WebSocket("ws://$host:8080/ws?user_id=$userId&game_id=$gameCode")
            ws?.onopen = {
                ws?.send("Hello!")
            }

            ws?.onmessage = {
                it as MessageEvent
                div.append {
                    p {
                        +(it.data?.toString() ?: "")
                    }
                }
            }

            ws?.onclose = {
                div.append {
                    p {
                        +"WS Closed."
                    }
                }
            }
        }
        false
    }
}