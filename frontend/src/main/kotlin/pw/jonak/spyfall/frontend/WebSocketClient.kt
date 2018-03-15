package pw.jonak.spyfall.frontend

import org.w3c.dom.CloseEvent
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event

class WebSocketClient(val url: String) {
    private lateinit var socket: WebSocket

    private val nextMessageHandlers: ArrayList<((MessageEvent) -> Unit)> = ArrayList()

    init {
        open()
    }

    fun open() {
        socket = WebSocket(url)
    }

    fun onOpen(function: (Event) -> Unit) {
        socket.onopen = {
            function(it)
        }
    }

    fun onMessage(function: (MessageEvent) -> Unit) {
        socket.onmessage = {
            it as MessageEvent
            function(it)
            nextMessageHandlers.forEach { handler -> handler(it) }
        }
    }

    fun onClose(function: (CloseEvent) -> Unit) {
        socket.onclose = {
            function(it as CloseEvent)
            reconnect()
        }
    }

    fun onError(function: (Event) -> Unit) {
        socket.onerror = {
            function(it)
            reconnect()
        }
    }

    private fun reconnect() {
        socket.close()
        socket = WebSocket(url)
    }

    fun sendMessage(message: String, onReply: ((MessageEvent) -> Unit)? = null) {
        socket.send(message)
        if (onReply != null) {
            nextMessageHandlers.add(onReply)
        }
    }
}