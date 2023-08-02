package me.odinclient.utils.render.gui

import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.renderer.scissor.Scissor
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.utils.Utils.minus
import me.odinclient.utils.Utils.plus
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import java.util.*


/**
 * Helps with making GUIs
 */
object GuiUtils {

    val scaledWidth get() =
        ScaledResolution(mc).scaledWidth

    val scaledHeight get() =
        ScaledResolution(mc).scaledHeight

    val scaleFactor: Float
        get() {
            val scale = (scaledWidth / 960f).coerceAtMost(scaledHeight / 540f)
            return (scale.coerceAtLeast(1f / 1280f).coerceAtLeast(1f / 800f)).coerceIn(0.05f, 1f)
        }

    fun VG.translateWithMouse(mouseHandler: MouseHandler, x: Float, y: Float) { // bad name refactor later same with scale func
        this.translate(x, y)
        mouseHandler.translate(x, y)
    }

    fun VG.scaleWithMouse(mouseHandler: MouseHandler, x: Float, y: Float) {
        this.scale(x, y)
        mouseHandler.scale(x, y)
    }

    inline fun VG.scissor(x: Float, y: Float, width: Float, height: Float, action: () -> Unit) {
        val scissor = ScissorHelper.INSTANCE.scissor(instance, x, y, width, height)
        action()
        ScissorHelper.INSTANCE.resetScissor(instance, scissor)
    }

    fun VG.scissor(x: Float, y: Float, width: Float, height: Float): Scissor {
        return ScissorHelper.INSTANCE.scissor(instance, x, y, width, height)
    }

    fun VG.resetScissor(scissor: Scissor) {
        ScissorHelper.INSTANCE.resetScissor(instance, scissor)
    }

    fun VG.nanoVG(block: VG.() -> Unit) = nanoVG(this.instance, block)

    fun VG.drawCustomCenteredText(string: String, x: Float, y: Float, size: Float, font: Font, color: Int = ColorUtil.textColor) {
        val textWidth = (x - getTextWidth(string, size, Fonts.MEDIUM) / 2f)
        drawText(string, textWidth, y, color, size, font)
    }

    fun String.capitalizeOnlyFirst(): String {
        return substring(0, 1).uppercase(Locale.getDefault()) + substring(1, length).lowercase()
    }

    fun VG.drawHSBBox(x: Number, y: Number, w: Number, h: Number, color: Int) {
        nanoVGHelper.drawHSBBox(this.instance, x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color)
        drawOutlineRoundedRect(x, y, w, h, 8f, Color(38, 38, 38).rgb, 1f)
    }

    /**
     * Fixed version of [drawHollowRoundedRect]
     * Original verison is offset for whatever reason.
     */
    fun VG.drawOutlineRoundedRect(x: Number, y: Number, w: Number, h: Number, radius: Number, color: Int, thickness: Number) {
        drawHollowRoundedRect(x - 1, y - 1, w + .5, h + .5, radius - 1, color, thickness)
    }
}