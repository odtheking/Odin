package me.odinclient.ui.hud

import me.odinclient.features.Module
import me.odinclient.features.ModuleManager.hud
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.ui.hud.ExampleHudGui.dragging
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.*

/**
 * Inspired by [FloppaClient](https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/kotlin/floppaclient/ui/hud/HudElement.kt)
 */
open class HudElement(
    x: Float = 0f,
    y: Float = 0f,
    defaultScale: Float = 1.4f,
    inline val render: Render = { 0f to 0f }
) {

    private var parentModule: Module? = null

    var enabled = false

    private val isEnabled: Boolean
        get() = parentModule?.enabled ?: false && enabled

    internal val xSetting: NumberSetting<Float>
    internal val ySetting: NumberSetting<Float>
    internal val scaleSetting: NumberSetting<Float>

    val hoverHandler = HoverHandler(200)

    fun init(module: Module) {
        parentModule = module

        module.register(
            xSetting,
            ySetting,
            scaleSetting
        )

        hud.add(this)
    }

    internal var x: Float
        inline get() = xSetting.value
        set(value) {
            xSetting.value = value
        }

    internal var y: Float
        inline get() = ySetting.value
        set(value) {
            ySetting.value = value
        }

    internal var scale: Float
        inline get() = scaleSetting.value
        set(value) {
            if (value > .8f) scaleSetting.value = value
        }

    /**
     * Good practice to keep the example as similar to the actual hud.
     */
    open fun render(nvg: NVG, example: Boolean): Pair<Float, Float> {
        return nvg.render(example)
    }

    /**
     * Renders and positions the element and if it's rendering the example then draw a rect behind it.
     */
    fun draw(vg: NVG, example: Boolean) {
        if (!isEnabled) return

        vg.translate(x, y)
        vg.scale(scale, scale)
        val (width, height) = render(vg, example)
        if (example) {
            hoverHandler.handle(x, y, width * scale, height * scale)

            var thickness = anim.get(.25f, .75f, !hasStarted)
            if (anim2.isAnimating() || dragging != null) {
                thickness += anim2.get(0f, .5f, dragging == null)
            }

            vg.rectOutline(
                -1.5f,
                -1.5f,
                3f + width,
                3f + height,
                Color.WHITE.withAlpha(percent / 100f),
                5f,
                thickness
            )
        }
        vg.resetTransform()

        this.width = width
        this.height = height
    }

    fun accept(): Boolean {
        return isAreaHovered(x, y, width * scale, height * scale)
    }

    /**
     * Needs to be set for preview boxes to be displayed correctly
     */
    var width: Float = 10f

    /**
     * Needs to be set for preview boxes to be displayed correctly
     */
    var height: Float = 10f

    /**
     * Animation for clicking on it
     */
    val anim2 = EaseInOut(200)

    /**
     * Wrapper
     */
    inline val anim
        get() = hoverHandler.anim

    /**
     * Wrapper
     */
    inline val percent: Int
        get() = hoverHandler.percent()

    /**
     * Wrapper
     */
    inline val hasStarted: Boolean
        get() = hoverHandler.hasStarted

    init {
        val xHud = NumberSetting("xHud", default = x, hidden = true, min = 0f, max = 1920f)
        val yHud = NumberSetting("yHud", default = y, hidden = true, min = 0f, max = 1080f)
        val scaleHud = NumberSetting("scaleHud", defaultScale, 0.8f, 6.0f, 0.01f, hidden = true)

        this.xSetting = xHud
        this.ySetting = yHud
        this.scaleSetting = scaleHud
    }
}

typealias Render = NVG.(Boolean) -> Pair<Float, Float>