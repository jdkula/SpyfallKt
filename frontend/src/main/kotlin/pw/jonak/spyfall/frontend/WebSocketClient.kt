package pw.jonak.spyfall.frontend

import org.w3c.dom.CloseEvent
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import kotlin.browser.window

typealias MessageHandler = (MessageEvent) -> Unit
typealias EventHandler = (Event) -> Unit
typealias CloseHandler = (CloseEvent) -> Unit

private const val reconnectInterval = 1000

/**
 * Resilient client for [WebSocket]s that reconnects on disconnect, and
 * is more idiomatic.
 *
 * @property url Connect to this server.
 * @property onFirstOpenHandler Runs this code on the first opening of the server.
 */
@Suppress("RedundantVisibilityModifier")
public class WebSocketClient(val url: String, private val onFirstOpenHandler: EventHandler? = null) {

    /**
     * The current socket we're using.
     */
    private lateinit var socket: WebSocket

    /**
     * Tracks if this is the first connect; controls
     * whether [onFirstOpenHandler] is called.
     */
    private var isFirstConnect = true

    /** Handlers called when a message is received */
    private val onMessageHandlers: HashSet<MessageHandler> = HashSet()

    /** Handlers called when the connection is opened or re-opened */
    private val onOpenHandlers: HashSet<EventHandler> = HashSet()

    /** Handlers called when the connection is closed */
    private val onCloseHandlers: HashSet<CloseHandler> = HashSet()

    /** Handlers called when the connection is errored */
    private val onErrorHandlers: HashSet<EventHandler> = HashSet()

    /** Messages to be sent when reconnected. */
    private val messagesToSend = HashSet<String>()

    /** Gets if the socket is ready */
    public val isConnected: Boolean get() = socket.readyState == 1.toShort()

    init {
        open() // Open on creation.
    }

    /**
     * Closes and reopens the socket.
     */
    public fun open(): WebSocketClient {
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
            onMessageHandlers.forEach { handle -> handle(it) }
        }

        return this
    }

    /**
     * Adds an on-open event handler to this [WebSocketClient]
     */
    fun onOpen(function: EventHandler): WebSocketClient {
        onOpenHandlers.add(function)
        return this
    }

    /**
     * Adds an on-message event handler to this [WebSocketClient]
     */
    fun onMessage(function: MessageHandler): WebSocketClient {
        onMessageHandlers.add(function)
        return this
    }

    /**
     * Adds an on-close event handler to this [WebSocketClient]
     */
    fun onClose(function: CloseHandler): WebSocketClient {
        onCloseHandlers.add(function)
        return this
    }

    /**
     * Adds an on-error event handler to this [WebSocketClient]
     */
    fun onError(function: EventHandler): WebSocketClient {
        onErrorHandlers.add(function)
        return this
    }

    /**
     * Handles reconnection
     */
    private fun reconnect(): WebSocketClient {
        window.setTimeout({
            open()
        }, reconnectInterval)
        return this
    }

    /**
     * Sends a [message] down the pipe ASAP.
     */
    public fun sendMessage(message: String): WebSocketClient {
        if(isConnected) {
            socket.send(message)
        } else {
            messagesToSend += message
        }
        return this
    }
}