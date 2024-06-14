package com.github.stivais.ui

import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.Group
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.events.EventManager
import com.github.stivais.ui.renderer.Font
import com.github.stivais.ui.renderer.Framebuffer
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.renderer.impl.NVGRenderer
import com.github.stivais.ui.utils.loop
import me.odinmain.utils.round
import java.util.logging.Logger

class UI(
    val renderer: Renderer = NVGRenderer,
    settings: UISettings? = null
) {
    lateinit var window: Window

    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px))

    val settings: UISettings = settings ?: UISettings()

    // temporary
    var keepOpen = false

    constructor(renderer: Renderer = NVGRenderer, dsl: ElementScope<Group>.() -> Unit) : this(renderer) {
        ElementScope(main).dsl()
    }

    var eventManager: EventManager = EventManager(this)

    inline val mx get() = eventManager.mouseX

    inline val my get() = eventManager.mouseY

    var onOpen: ArrayList<UI.() -> Unit>? = null

    var onClose: ArrayList<UI.() -> Unit>? = null


    var animations: ArrayList<Pair<Animation, UI.(Float) -> Unit>>? = null

    var alpha = 1f

    var scale = 1f

    fun initialize(window: Window, width: Int, height: Int) {
        this.window = window
        main.constraints.width = width.px
        main.constraints.height = height.px

        main.initialize(this)
        main.position()

        onOpen?.loop { this.it() }
        if (settings.cleanupOnOpenClose) {
            onOpen = null
        }
        if (settings.cacheFrames && renderer.supportsFramebuffers()) {
            framebuffer = renderer.createFramebuffer(main.width, main.height)
        }
    }

    // frame metrics
    var performance: String? = null
    var lastUpdate = System.nanoTime()
    var frames: Int = 0
    var frameTime: Long = 0

    var needsRedraw = true

    var framebuffer: Framebuffer? = null

    // rework fbo
    fun render() {
        val fbo = framebuffer
        if (fbo == null) {
            renderer.beginFrame(main.width, main.height)
            renderer.push()
            animations?.removeIf { (anim, block) ->
                this.block(anim.get())
                anim.finished
            }

            if (alpha != 1f) {
                renderer.globalAlpha(alpha)
            }
            if (scale != 1f) {
                renderer.translate(main.width / 2f, main.height / 2f)
                renderer.scale(scale, scale)
                renderer.translate(-main.width / 2f, -main.height / 2f)
            }

            if (needsRedraw) {
                needsRedraw = false
                main.position()
                main.clip()
            }
            main.render()
            performance?.let {
                renderer.text(it, main.width - renderer.textWidth(it, 12f), main.height - 12f, 12f)
            }
            renderer.pop()
            renderer.endFrame()
        } else {
            if (needsRedraw) {
                needsRedraw = false
                renderer.bindFramebuffer(fbo) // thanks ilmars for helping me fix
                renderer.beginFrame(fbo.width, fbo.height)
                main.position()
                main.clip()
                main.render()
                renderer.endFrame()
                renderer.unbindFramebuffer()
            }
            renderer.beginFrame(fbo.width, fbo.height)
            renderer.drawFramebuffer(fbo, 0f, 0f)
            performance?.let {
                renderer.text(it, main.width - renderer.textWidth(it, 12f), main.height - 12f, 12f)
            }
            renderer.endFrame()
        }
    }

    // idk about name
    // kinda verbose
    internal inline fun measureMetrics(block: () -> Unit) {
        val start = System.nanoTime()
        block()
        frameTime += System.nanoTime() - start
        frames++
        if (System.nanoTime() - lastUpdate >= 1_000_000_000) {
            lastUpdate = System.nanoTime()
            val sb = StringBuilder()
            if (settings.elementMetrics) {
                sb.append("elements: ${getStats(main, false)}, elements rendering: ${getStats(main, true)},")
            }
            if (settings.frameMetrics) {
                sb.append("frame-time avg: ${((frameTime / frames) / 1_000_000.0).round(4)}s")
            }
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
        needsRedraw = true
        if (framebuffer != null) {
            renderer.destroyFramebuffer(framebuffer!!)
            framebuffer = renderer.createFramebuffer(main.width, main.height)
        }
    }

    fun cleanup() {
        eventManager.elementHovered = null
        unfocus()
        framebuffer?.let { fbo -> renderer.destroyFramebuffer(fbo) }
        onClose?.loop { this.it() }
        if (settings.cleanupOnOpenClose) onClose = null
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

    inline fun settings(block: UISettings.() -> Unit): UI {
        settings.apply(block)
        return this
    }

    fun animate(
        duration: Float,
        animation: Animations,
        block: UI.(percent: Float) -> Unit
    ): Animation {
        val anim = Animation(duration, animation)
        if (animations == null) animations = arrayListOf()
        animations!!.add(Pair(anim, block))
        return anim
    }

    companion object {
        // temp name
        // future: maybe make a log handling class, so you can get an element's "errors" and details
        val logger: Logger = Logger.getLogger("Odin/UI")

        @JvmField
        val defaultFont = Font("Regular", "/assets/odinmain/fonts/Regular.otf")
    }
}