package com.odtheking.odin.utils.network

import com.odtheking.odin.OdinMod.logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicReference

fun webSocket(func: WebSocketConnection.() -> Unit) = WebSocketConnection().apply(func)

class WebSocketConnection {
    private val _webSocket = AtomicReference<WebSocket?>(null)
    private val httpClient = HttpClient.newHttpClient()
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

    fun connect(url: String) {
        _webSocket.getAndSet(null)?.sendClose(1000, "Reconnecting")

        val listener = object : WebSocket.Listener {
            private val messageBuilder = StringBuilder()

            override fun onOpen(webSocket: WebSocket) {
                logger.info("WebSocket connected to $url")
                _webSocket.set(webSocket)
                webSocket.request(1)
            }

            override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                messageBuilder.append(data)
                if (last) {
                    val message = messageBuilder.toString()
                    messageBuilder.clear()
                    if (_webSocket.get() === webSocket) onMessageFunc(message)
                }
                webSocket.request(1)
                return null
            }

            override fun onBinary(webSocket: WebSocket, data: ByteBuffer, last: Boolean): CompletionStage<*>? {
                val text = StandardCharsets.UTF_8.decode(data).toString()
                if (_webSocket.get() === webSocket) onMessageFunc(text)
                webSocket.request(1)
                return null
            }

            override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
                logger.info("WebSocket closed: $statusCode / $reason")
                _webSocket.compareAndSet(webSocket, null)
                return null
            }

            override fun onError(webSocket: WebSocket, error: Throwable) {
                logger.error("WebSocket error: ${error.message}", error)
                _webSocket.compareAndSet(webSocket, null)
            }
        }

        httpClient.newWebSocketBuilder()
            .buildAsync(URI.create(url), listener)
            .exceptionally { error ->
                logger.error("Failed to connect WebSocket: ${error.message}", error)
                null
            }
    }

    fun shutdown() {
        _webSocket.getAndSet(null)?.sendClose(1000, "Client shutdown")
    }
}