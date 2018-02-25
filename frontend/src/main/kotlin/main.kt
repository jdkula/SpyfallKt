import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.p
import org.w3c.dom.*
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.dom.clear

fun main(args: Array<String>) {
    val div = document.getElementById("app") as HTMLDivElement
    //val ws = WebSocket("ws://localhost:8080")
    val submitButton = document.getElementById("submitbutton") as HTMLInputElement
    submitButton.onclick = {
        val nameForm = document.getElementById("nametext") as HTMLInputElement
        val userRequest = XMLHttpRequest()
        userRequest.open("POST", "http://localhost:8080/user/new?name=${nameForm.value}")
        userRequest.send()
        userRequest.onload = {
            div.clear()
            div.append {
                div {
                    +"${(userRequest.response as String).toInt()}"
                }
            }
        }
        false
    }
    div.append {
        p {
            +document.cookie
        }
    }
}