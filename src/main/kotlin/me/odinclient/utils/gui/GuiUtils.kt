package me.odinclient.utils.gui

import cc.polyfrost.oneconfig.renderer.scissor.Scissor
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.dsl.VG
import cc.polyfrost.oneconfig.utils.dsl.nanoVG
import cc.polyfrost.oneconfig.utils.dsl.scale
import cc.polyfrost.oneconfig.utils.dsl.translate
import me.odinclient.ui.waypoint.WaypointGUI
import net.minecraft.client.gui.ScaledResolution

/**
 * Helps with making GUIs
 */
object GuiUtils {

    val scaledWidth get() =
        ScaledResolution(WaypointGUI.mc).scaledWidth

    val scaledHeight get() =
        ScaledResolution(WaypointGUI.mc).scaledHeight

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

    fun VG.scissor(x: Float, y: Float, width: Float, height: Float) =
        ScissorHelper.INSTANCE.scissor(this.instance, x, y, width, height)

    fun VG.resetScissor(scissor: Scissor) =
        ScissorHelper.INSTANCE.resetScissor(this.instance, scissor)

    fun VG.nanoVG(block: VG.() -> Unit) = nanoVG(this.instance, block)
}