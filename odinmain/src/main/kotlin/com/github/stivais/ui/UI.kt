package com.github.stivais.ui

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.Group
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.events.EventManager
import com.github.stivais.ui.events.Lifetime
import com.github.stivais.ui.operation.UIOperation
import com.github.stivais.ui.renderer.Font
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.renderer.impl.NVGRenderer
import com.github.stivais.ui.utils.loop
import me.odinmain.utils.round
import java.util.logging.Logger

class UI(val renderer: Renderer = NVGRenderer) {

    /**
     * Used to reference the handler for this UI
     */
    lateinit var window: Window

    /**
     * The master element, acts as the border
     */
    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px))

    /**
     * Handles events, like mouse clicks or keyboard presses
     */
    var eventManager: EventManager = EventManager(this)

    constructor(renderer: Renderer = NVGRenderer, dsl: ElementScope<Group>.() -> Unit) : this(renderer) {
        main.initialize(this)
        ElementScope(main).dsl()
    }

    inline val mx get() = eventManager.mouseX

    inline val my get() = eventManager.mouseY

    var operations: ArrayList<UIOperation>? = null

    fun initialize(width: Int, height: Int, window: Window? = null) {
        window?.let { this.window = it }
        main.constraints.width = width.px
        main.constraints.height = height.px

        main.initialize(this)
        main.size()
        main.positionChildren()
        main.clip()

        eventManager.dispatchToAll(Lifetime.AfterInitialized, main)
    }

    // frame metrics
    var performance: String? = null
    var lastUpdate = System.nanoTime()
    var frames: Int = 0
    var frameTime: Long = 0

    // rework fbo
    fun render() {
        renderer.beginFrame(main.width, main.height)
        renderer.push()
        operations?.removeIf {
            it.run()
        }
        main.render()
        performance?.let {
            renderer.text(it, main.width - renderer.textWidth(it, 12f), main.height - 12f, 12f)
        }
        renderer.pop()
        renderer.endFrame()
    }

    internal inline fun measureFrametime(block: () -> Unit) {
//        if (!debug) return
        val start = System.nanoTime()
        block()
        frameTime += System.nanoTime() - start
        frames++
        if (System.nanoTime() - lastUpdate >= 1_000_000_000) {
            lastUpdate = System.nanoTime()
            val sb = StringBuilder()
            sb.append("elements: ${getStats(main, false)}, elements rendering: ${getStats(main, true)},")
            sb.append("frame-time avg: ${((frameTime / frames) / 1_000_000.0).round(4)}s")
            performance = sb.toString()
            frames = 0
            frameTime = 0
        }
    }

    private fun getStats(element: Element, onlyRender: Boolean): Int {
        var amount = 0
        if (!(onlyRender && !element.renders)) {
            amount++
            element.elements?.loop { amount += getStats(it, onlyRender) }
        }
        return amount
    }

    fun resize(width: Int, height: Int) {
        main.constraints.width = width.px
        main.constraints.height = height.px
        main.redraw = true
    }

    fun cleanup() {
        unfocus()
        eventManager.dispatchToAll(Lifetime.Uninitialized, main)
    }

    fun focus(element: Element) {
        eventManager.focus(element)
    }

    fun unfocus() {
        eventManager.unfocus()
    }

    fun isFocused(element: Element): Boolean {
        return eventManager.focused == element
    }

    companion object {
        // temp name
        // future: maybe make a log handling class, so you can get an element's "errors" and details
        val logger: Logger = Logger.getLogger("Odin/UI")

        @JvmField
        val defaultFont = Font("Regular", "/assets/odinmain/fonts/Regular.otf")
    }
}