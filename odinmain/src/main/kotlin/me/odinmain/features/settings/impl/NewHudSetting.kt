package me.odinmain.features.settings.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.*
import com.github.stivais.ui.impl.ClickGUITheme
import com.github.stivais.ui.impl.`gray 26`
import com.github.stivais.ui.impl.`gray 38`
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.sign

class NewHudSetting(name: String, hud: Hud) : Setting<Hud>(name, false, ""), Saving {
    override val default: Hud = hud
    override var value: Hud = hud

    override fun write(): JsonElement {
        return JsonObject().apply {
            add("x", JsonPrimitive(value._x.current))
            add("y", JsonPrimitive(value._y.current))
            add("scale", JsonPrimitive(value.scale))
            add("enabled", JsonPrimitive(value.enabled))
        }
    }

    override fun read(element: JsonElement?) {
        element?.asJsonObject?.apply {
            value._x.current = get("x").asFloat
            value._y.current = get("y").asFloat
            value.scale = get("scale").asFloat
            value.enabled = get("enabled").asBoolean
        }
    }

    override fun ElementScope<*>.createElement() {
        setting(40.px) {
            text(text = name)
            block(
                constrain(x = -6.px, w = 20.px, h = 20.px),
                color = Color.RED
            ).onClick {
                popupMenu(constraints = size(Bounding + 6.px, Bounding + 6.px)) {
                    val menu = this.element
                    // bg
                    block(constraints = size(Copying, Copying), `gray 26`, radii(5)).outline(ClickGUITheme)

                    row(constraints = constrain(6.px, 6.px, Bounding, Bounding)) {
                        block(
                            constraints = size(Bounding, Bounding),
                            color = Color.TRANSPARENT,
                            radius = 5.radii()
                        ) {
//                            outline(ClickGUITheme)
                            value.builder(Hud.Scope(element))
                        }
                        divider(3.px)
                        // x to close preview thingy
                        block(constraints = size(10.px, 10.px), Color.RED).onClick {
                            ui.main.removeElement(menu)
                            true
                        }
                    }
                }
                true
            }

        }
    }
}

class Hud(
    x: Float,
    y: Float,
    on: Boolean,
    val builder: Scope.() -> Unit
) : Element(Constraints(Animatable.Raw(x), Animatable.Raw(y), Bounding, Bounding)) {

    private val defaultX = x
    private val defaultY = y
    private val defaultScale = 1f

    inline val _x get() = constraints.x as Animatable.Raw
    inline val _y get() = constraints.y as Animatable.Raw

    private val outline1 = Animatable(from = 0.px, to = 1.px)
    private val outline2 = Animatable(from = 0.px, to = 1.px)

    private var dragging = false
        set(value) {
            outline1.animate(0.5.seconds, Animations.EaseOutQuint)
            field = value
        }

    init {
        scaleCenter = false
        enabled = on
        UI.main.addElement(this)
        huds.add(this)
    }

    val scope = Scope(this).apply {

        var dx = 0f
        var dy = 0f

        onClick {
            dragging = true
            dx = ui.mx - element.internalX
            dy = ui.my - element.internalY
            true
        }
        onRelease {
            if (dragging) dragging = false
        }
        onMouseMove {
            if (dragging) {
                _x.to(ui.mx - dx)
                _y.to(ui.my - dy)
                redraw()
            }
            dragging
        }
        onMouseEnterExit {
            outline2.animate(0.5.seconds, Animations.EaseOutQuint)
            true
        }
        onScroll { (amount) ->
            scale = (scale + (amount.sign * 0.1f)).coerceIn(0.8f, 5f)
            true
        }
        builder()
    }

    fun refresh() { // im not sure the impact of this
        removeAll()
        scope.builder()
        ui.needsRedraw = true
    }

    fun resetPositions() {
        _x.animate(to = defaultX, 0.35.seconds, Animations.EaseOutQuint)
        _y.animate(to = defaultY, 0.35.seconds, Animations.EaseOutQuint)
        scale = defaultScale
    }

    fun onEditHudClose() {
        if (dragging) dragging = false
    }

    override fun draw() {
        val outline = outline1.get(this, Type.W) + outline2.get(this, Type.W)
        if (outline != 0f) {
            renderer.hollowRect(x - 2f, y - 2f, width + 4f, height + 4f, outline, Color.WHITE.rgba, 5f)
        }
    }

    override fun onElementAdded(element: Element) {
        val c = element.constraints
        if (c.x is Undefined) c.x = 0.px
        if (c.y is Undefined) c.y = 0.px
    }

    class Scope(hud: Element) : ElementScope<Element>(hud) {
        val shouldPreview
            get() = !render
    }

    companion object {

        private var render = true

        private val huds = arrayListOf<Hud>()

        val UI = UI {
            val button = block(
                constraints = constrain(y = 80.percent, w = 10.percent, h = 5.percent),
                color = `gray 38`,
                radius = 5.radii()
            ) {
                onClick {
                    huds.forEach {
                        it.resetPositions()
                    }
                    true
                }
                text(text = "Reset")
                hoverEffect()
                outline(ClickGUITheme)
                enabled = false
            }.element
            onUIOpen {
                render = false
                button.enabled = true

                huds.forEach {
                    it.refresh()
                }
                animate(0.5.seconds, Animations.EaseOutQuint) {
                    button.alpha = it
                    button.scale = it
                }
            }
            onUIClose {
                render = true
                huds.forEach {
                    it.refresh()
                    it.onEditHudClose()
                }
                animate(0.5.seconds, Animations.EaseInBack) {
                    val p = 1f - it
                    button.alpha = p
                    button.scale = p
                }.onFinish {
                    button.enabled = false
                }
            }

        }.settings {
            frameMetrics = true
            cleanupOnOpenClose = false
        }.also { it.main.initialize(it) }

        @SubscribeEvent
        fun onRender(event: RenderWorldLastEvent) {
//            if (!render) return
//            val s = mc.currentScreen
//            if (s is UIScreen && s.ui == UI) return
//            UI.measureMetrics {
//                UI.render()
//            }
        }
    }
}

