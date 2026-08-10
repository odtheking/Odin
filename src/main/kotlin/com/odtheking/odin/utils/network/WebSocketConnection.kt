package com.odtheking.odin.utils.network

import com.odtheking.odin.OdinMod.logger
import java.net.http.WebSocket
import java.util.concurrent.atomic.AtomicReference

fun webSocket(func: WebSocketConnection.() -> Unit) = WebSocketConnection().apply(func)

class WebSocketConnection {
    private val _webSocket = AtomicReference<WebSocket?>(null)
    private var onMessageFunc: (String) -> Unit = { }

    fun onMessage(func: (String) -> Unit) {
        onMessageFunc = func
    }

    val connected get() = _webSocket.get() != null

    fun send(message: String): Boolean {
        val ws = _webSocket.get()
        if (ws == null) {
            logger.warn("Cannot send message: WebSocket not connected")
            return false
        }
        ws.sendText(message, true)
        return true
    }

    fun connect(url: String) {}

    fun shutdown() {
        _webSocket.getAndSet(null)?.sendClose(1000, "Client shutdown")
    }
}
