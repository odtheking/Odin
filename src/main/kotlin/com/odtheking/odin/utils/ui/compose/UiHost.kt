package com.odtheking.odin.utils.ui.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import java.lang.Runnable
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

class UiHost(content: @Composable () -> Unit) {
    private val root = UiNode()

    private val tasks = ConcurrentLinkedQueue<Runnable>()
    private val clock = BroadcastFrameClock()
    private val scope = CoroutineScope(RenderThread() + clock)
    private val recomposer = Recomposer(scope.coroutineContext)
    private val composition = Composition(UiApplier(root, ::onRemoved), recomposer)

    private var focused: UiNode? = null
    private var pressed: UiNode? = null
    private var settling = true
    val dismissListeners = ArrayList<() -> Unit>()

    var scale = 1f

    var width = 0
        private set
    var height = 0
        private set

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) { recomposer.runRecomposeAndApplyChanges() }
        composition.setContent {
            CompositionLocalProvider(LocalHost provides this, content = content)
        }
    }

    fun render(graphics: GuiGraphicsExtractor, width: Int, height: Int, mouseX: Int, mouseY: Int) {
        Motion.frameNanos = System.nanoTime()
        Motion.settling = settling

        Snapshot.sendApplyNotifications()
        drain()
        if (clock.hasAwaiters) {
            clock.sendFrame(Motion.frameNanos)
            drain()
        }

        this.width = (width / scale).toInt()
        this.height = (height / scale).toInt()
        Pointer.x = (mouseX / scale).toInt()
        Pointer.y = (mouseY / scale).toInt()
        root.measure()
        root.width = this.width
        root.height = this.height
        root.place(0, 0)

        graphics.pose().pushMatrix()
        graphics.pose().scale(scale, scale)
        root.render(graphics, true)
        Overlay.flush(graphics)
        graphics.pose().popMatrix()

        Motion.settling = false
        settling = false
    }

    fun dispose() {
        composition.dispose()
        recomposer.cancel()
        scope.cancel()
    }

    fun settle() {
        settling = true
    }

    fun click(screenEvent: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val event = screenEvent.scaled()
        val target = root.pick(event.x(), event.y())
        ancestry(target) { it.onPress?.invoke(); false }

        Pointer.doubleClick = doubleClick
        val handler = ancestry(target) { it.pointer?.onPress?.invoke(it, event) == true } ?: return false
        pressed = handler
        setFocused(handler)
        return true
    }

    fun drag(event: MouseButtonEvent): Boolean {
        val node = pressed ?: return false
        node.pointer?.onDrag?.invoke(node, event.scaled())
        return true
    }

    fun release(event: MouseButtonEvent): Boolean {
        val node = pressed ?: return false
        pressed = null
        node.pointer?.onRelease?.invoke(node, event.scaled())
        return true
    }

    fun scroll(mouseX: Double, mouseY: Double, amount: Double): Boolean =
        ancestry(root.pick(mouseX / scale, mouseY / scale)) { it.onScroll?.invoke(it, amount) == true } != null

    fun hitTest(x: Int, y: Int): Any? = hitTest(x.toDouble(), y.toDouble())

    fun hitTest(x: Double, y: Double): Any? {
        var node = root.pick(x / scale, y / scale)
        while (node != null) {
            node.tag?.let { return it }
            node = node.parent
        }
        return null
    }

    fun key(event: KeyEvent): Boolean = ancestry(focused) { it.onKey?.invoke(it, event) == true } != null

    fun char(event: CharacterEvent): Boolean = ancestry(focused) { it.onChar?.invoke(it, event) == true } != null

    fun focus(requester: FocusRequester) {
        setFocused(requester.node)
        requester.onRequest?.invoke()
    }

    fun dismiss() {
        pressed = null
        setFocused(null)
        for (listener in dismissListeners.toList()) listener()
    }

    private fun onRemoved(node: UiNode) {
        if (focused?.isWithin(node) == true) setFocused(null)
        if (pressed?.isWithin(node) == true) pressed = null
    }

    private fun MouseButtonEvent.scaled(): MouseButtonEvent =
        if (scale == 1f) this else MouseButtonEvent(x() / scale, y() / scale, buttonInfo())

    private fun UiNode.isWithin(ancestor: UiNode) = ancestry(this) { it === ancestor } != null

    private fun setFocused(node: UiNode?) {
        if (focused === node) return
        focused?.onFocusChanged?.invoke(false)
        focused = node
        node?.onFocusChanged?.invoke(true)
    }

    private inline fun ancestry(from: UiNode?, predicate: (UiNode) -> Boolean): UiNode? {
        var node = from
        while (node != null) {
            if (predicate(node)) return node
            node = node.parent
        }
        return null
    }

    private fun drain() {
        while (true) (tasks.poll() ?: return).run()
    }

    private inner class RenderThread : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            tasks += block
        }
    }
}

val LocalHost = staticCompositionLocalOf<UiHost> { error("No UiHost") }

@Composable
fun OnDismiss(action: () -> Unit) {
    val host = LocalHost.current
    val current by rememberUpdatedState(action)
    DisposableEffect(host) {
        val listener = { current() }
        host.dismissListeners += listener
        onDispose { host.dismissListeners -= listener }
    }
}

object Overlay {
    private val queue = ArrayList<(GuiGraphicsExtractor) -> Unit>()

    fun defer(draw: (GuiGraphicsExtractor) -> Unit) {
        queue += draw
    }

    fun flush(graphics: GuiGraphicsExtractor) {
        try {
            for (i in queue.indices) queue[i](graphics)
        } finally {
            queue.clear()
        }
    }
}

class InteractionSource {
    var hovered by mutableStateOf(false)
        internal set
}

class FocusRequester {
    var node: UiNode? = null
    var onRequest: (() -> Unit)? = null
}

private class UiApplier(root: UiNode, private val onRemoved: (UiNode) -> Unit) : AbstractApplier<UiNode>(root) {
    override fun insertTopDown(index: Int, instance: UiNode) = Unit

    override fun insertBottomUp(index: Int, instance: UiNode) {
        instance.parent = current
        current.children.add(index, instance)
    }

    override fun remove(index: Int, count: Int) {
        for (i in index until index + count) onRemoved(current.children[i])
        current.children.subList(index, index + count).clear()
    }

    override fun move(from: Int, to: Int, count: Int) {
        val children = current.children
        val moved = children.subList(from, from + count).toList()
        children.subList(from, from + count).clear()
        children.addAll(if (from > to) to else to - count, moved)
    }

    override fun onClear() = root.children.clear()
}