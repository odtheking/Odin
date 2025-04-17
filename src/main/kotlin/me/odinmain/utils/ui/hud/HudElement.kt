package me.odinmain.utils.ui.hud

import me.odinmain.OdinMain.mc
import me.odinmain.features.Module
import me.odinmain.features.ModuleManager.huds
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.endProfile
import me.odinmain.utils.render.rectangleOutline
import me.odinmain.utils.startProfile
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.ui.clickgui.util.HoverHandler
import me.odinmain.utils.ui.hud.EditHUDGui.dragging
import me.odinmain.utils.ui.util.MouseUtils.isAreaHovered
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.Display
import kotlin.math.max

/**
 * Class to render elements on hud
 *
 * Inspired by [FloppaClient](https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/kotlin/floppaclient/ui/hud/HudElement.kt)
 * @author Stivais, Aton
 */
open class HudElement(
    x: Float = 0f,
    y: Float = 0f,
    private val displayToggle: Boolean,
    defaultScale: Float = 2f,
    val render: Render = { 0f to 0f },
    settingName: String
) {

    private var parentModule: Module? = null

    var enabled = false

    private val isEnabled: Boolean
        get() = parentModule?.enabled == true && enabled

    internal val xSetting: NumberSetting<Float>
    internal val ySetting: NumberSetting<Float>
    internal val scaleSetting: NumberSetting<Float>
    val enabledSetting: BooleanSetting = BooleanSetting("$settingName enabled", default = enabled, "", hidden = true)

    val hoverHandler = HoverHandler(200)

    fun init(module: Module) {
        parentModule = module

        module.register(
            xSetting,
            ySetting,
            scaleSetting,
            enabledSetting,
        )

        huds.add(this)
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
     * Renders and positions the element and if it's rendering the example then draw a rect behind it.
     */
    fun draw(example: Boolean) {
        if (displayToggle) enabled = enabledSetting.value
        if (!isEnabled) return

        startProfile(this.parentModule?.name + " Hud")

        xSetting.max = Display.getWidth().toDouble()
        ySetting.max = Display.getHeight().toDouble()

        GlStateManager.pushMatrix()
        val sr = ScaledResolution(mc)
        GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
        GlStateManager.translate(x, y, 0f)
        GlStateManager.scale(scale, scale, 1f)

        val (width, height) = render(example)

        if (example) {
            hoverHandler.handle(x, y, width * scale, height * scale)
            var thickness = anim.get(.25f, 1f, !hasStarted)
            if (anim2.isAnimating() || dragging != null) {
                thickness += anim2.get(0f, 1f, dragging == null)
            }

            rectangleOutline(
                -1.5f,
                -1.5f,
                3f + width,
                3f + height,
                Colors.WHITE.withAlpha(percent / 100f),
                5f,
                max(thickness * (scale / 2.5f), 2f)
            )
        }
        GlStateManager.popMatrix()

        this.width = width
        this.height = height

        endProfile()
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

    /** Wrapper */
    private inline val anim
        get() = hoverHandler.anim

    /** Wrapper */
    private inline val percent: Int
        get() = hoverHandler.percent()

    /** Wrapper */
    private inline val hasStarted: Boolean
        get() = hoverHandler.hasStarted

    /** Used for smooth resetting animations */
    internal var resetX: Float = 0f

    /** Used for smooth resetting animations */
    internal var resetY: Float = 0f

    /** Used for smooth resetting animations */
    internal var resetScale: Float = 0f

    private val xHud = NumberSetting("$settingName x", x, min = 0f, max = Float.MAX_VALUE, desc = "", hidden = true)
    private val yHud = NumberSetting("$settingName y", y, min = 0f, max = Float.MAX_VALUE, desc = "", hidden = true)

    init {
        val scaleHud = NumberSetting("$settingName scale", defaultScale, 1f, 8.0f, 0.01f, desc = "", hidden = true)

        this.xSetting = xHud
        this.ySetting = yHud
        this.scaleSetting = scaleHud
        this.enabled = enabledSetting.value
    }
}

typealias Render = (Boolean) -> Pair<Float, Float>