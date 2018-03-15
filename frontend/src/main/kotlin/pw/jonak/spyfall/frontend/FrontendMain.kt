package pw.jonak.spyfall.frontend

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.MessageEvent
import pw.jonak.spyfall.common.deserialize
import pw.jonak.spyfall.frontend.ApplicationState.*
import pw.jonak.spyfall.frontend.FrontendMain.socketClient
import pw.jonak.spyfall.frontend.state.join
import pw.jonak.spyfall.frontend.state.start
import react.dom.div
import react.dom.render
import kotlin.browser.document
import kotlin.properties.Delegates

const val host = "localhost"

fun main(args: Array<String>) {
    socketClient = WebSocketClient("ws://$host:8080/ws")
    socketClient.onOpen {
        println("Connected!")
    }
    socketClient.onMessage {
        val me = it as? MessageEvent
        if(me != null) {
            println(me.data?.toString())
            println(me.data!!.toString().deserialize())
        }
    }

    document.addEventListener("DOMContentLoaded", {
        updatePage()
    })
}

fun updatePage() {
    when(FrontendMain.state) {
        START -> {
            render(document.getElementById("app")) {
                div {
                    start()
                }
            }
        }
        JOIN -> {
            render(document.getElementById("app")) {
                div {
                    join(FrontendMain.userId?.toString() ?: "")
                }
            }
        }
        LOBBY -> TODO()
        GAME -> TODO()
    }
}

object FrontendMain {
    var appDiv: HTMLDivElement? = null
    var gameCode: String? = null
    var userId: Int? = null
    lateinit var socketClient: WebSocketClient
    var state: ApplicationState by Delegates.observable(START) { property, oldValue, newValue ->
        updatePage()
    }
    var userName: String? = null
}