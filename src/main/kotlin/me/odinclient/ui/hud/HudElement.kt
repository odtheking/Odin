package me.odinclient.ui.hud

import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.nvg.*

/**
 * Inspired by [FloppaClient](https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/kotlin/floppaclient/ui/hud/HudElement.kt)
 */
abstract class HudElement {

    constructor(x: Float = 0f, y: Float = 0f, defaultScale: Float = 1.4f) {

        val xHud = NumberSetting("xHud", default = x, hidden = true, min = 0f, max = 1920f)
        val yHud = NumberSetting("yHud", default = y, hidden = true, min = 0f, max = 1080f)
        val scaleHud = NumberSetting("scaleHud", defaultScale, 0.8f, 6.0f, 0.01f, hidden = true)

        this.xSetting = xHud
        this.ySetting = yHud
        this.scaleSetting = scaleHud
    }

    private var parentModule: Module? = null

    var isEnabled: Boolean = true
        get() = parentModule?.enabled ?: false && field

    internal val xSetting: NumberSetting<Float>
    internal val ySetting: NumberSetting<Float>
    internal val scaleSetting: NumberSetting<Float>


    fun init(module: Module) {
        parentModule = module

        module.register(
            xSetting,
            ySetting,
            scaleSetting
        )
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
    abstract fun render(vg: NVG, example: Boolean): Pair<Float, Float>

    /**
     * Renders and positions the element and if its rendering the example then draw a rect behind it.
     */
    fun draw(vg: NVG, example: Boolean) {
        if (!isEnabled) return

        vg.translate(x, y)
        vg.scale(scale, scale)

        if (example) vg.rect(0f, 0f, width, height, Color(0, 0, 0, 1.5f))
        val (width, height) = render(vg, example)
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
}