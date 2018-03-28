package pw.jonak.spyfall.frontend

import org.w3c.dom.CloseEvent
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import kotlin.browser.window

typealias MessageHandler = (MessageEvent) -> Unit
typealias EventHandler = (Event) -> Unit
typealias CloseHandler = (CloseEvent) -> Unit

const val reconnectInterval = 1000

class WebSocketClient(val url: String, private val onFirstOpenHandler: EventHandler? = null) {

    private lateinit var socket: WebSocket
    var isFirstConnect = true

    private val nextMessageHandlers: HashSet<MessageHandler> = HashSet()
    private val onMessageHandlers: HashSet<MessageHandler> = HashSet()
    private val onOpenHandlers: HashSet<EventHandler> = HashSet()
    private val onCloseHandlers: HashSet<CloseHandler> = HashSet()
    private val onErrorHandlers: HashSet<EventHandler> = HashSet()

    private val messagesToSend = HashSet<String>()

    val isConnected: Boolean get() = socket.readyState == 1.toShort()

    init {
        open()
    }

    fun open(): WebSocketClient {
        if (this::socket.isInitialized) {
            if(socket.readyState == 1.toShort()) {
                socket.close()
            }
        }

        socket = WebSocket(url)

        socket.onclose = {
            it as CloseEvent
            onCloseHandlers.forEach { handle -> handle(it) }
            reconnect()
        }
        socket.onerror = {
            onErrorHandlers.forEach { handle -> handle(it) }
        }
        socket.onopen = {
            if(isFirstConnect) {
                isFirstConnect = false
                onFirstOpenHandler?.invoke(it)
            }
            messagesToSend.forEach { sendMessage(it) }
            messagesToSend.clear()
            onOpenHandlers.forEach { handle -> handle(it) }
        }
        socket.onmessage = {
            it as MessageEvent
            val nextMessageHandlerCache = HashSet(nextMessageHandlers)
            onMessageHandlers.forEach { handle -> handle(it) }
            nextMessageHandlerCache.forEach { handle -> handle(it) }
            nextMessageHandlers.removeAll(nextMessageHandlerCache)
        }

        return this
    }

    fun onOpen(function: EventHandler): WebSocketClient {
        onOpenHandlers.add(function)
        return this
    }

    fun onMessage(function: MessageHandler): WebSocketClient {
        onMessageHandlers.add(function)
        return this
    }

    fun onClose(function: CloseHandler): WebSocketClient {
        onCloseHandlers.add(function)
        return this
    }

    fun onError(function: EventHandler): WebSocketClient {
        onErrorHandlers.add(function)
        return this
    }

    private fun reconnect(): WebSocketClient {
        println("Reconnecting in $reconnectInterval ms...")
        window.setTimeout({
            println("Reconnecting...")
            open()
        }, reconnectInterval)
        return this
    }

    fun sendMessage(message: String): WebSocketClient {
        println("SENDING: $message")
        if(isConnected) {
            socket.send(message)
        } else {
            messagesToSend += message
        }
        return this
    }
}