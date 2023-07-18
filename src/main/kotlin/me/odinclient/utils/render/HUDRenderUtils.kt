package me.odinclient.utils.render

import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.dsl.*

object HUDRenderUtils {

    // this dont use
    class DrawScope(private val dX: Number, private val dY: Number, private val dWidth: Number, private val dHeight: Number, val vg: VG) {
        /**
         * Rounded Rects
         */
        fun roundedRect(nvg: VG = vg, x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, color: Int, top: Float, bottom: Float) =
            nvg.drawRoundedRectVaried(x.toFloat() * 2f, y.toFloat() * 2f, width.toFloat() * 2f, height.toFloat() * 2f, color, top, top, bottom, bottom)

        fun roundedRect(x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, color: Int, top: Float, bottom: Float) =
            roundedRect(vg, x, y, width, height, color, top, bottom)

        fun roundedRect(color: Int, top: Float = 5f, bottom: Float = 5f) =
            roundedRect(vg, dX, dY, dWidth, dHeight, color, top, bottom)

        /**
         * Shadows
         */
        fun drawShadow(nvg: VG = vg, x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, blur: Float, spread: Float, radius: Float) =
            nvg.drawDropShadow(x.toFloat() * 2f, y.toFloat() * 2f, width.toFloat() * 2f, height.toFloat() * 2f, blur, spread, radius)

        fun drawShadow(x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, blur: Float, spread: Float, radius: Float) =
            drawShadow(vg, x, y, width, height, blur, spread, radius)

        fun drawShadow(blur: Float = 10f, spread: Float = 0.75f, radius: Float = 5f) =
            drawShadow(vg, dX, dY, dWidth, dHeight, blur, spread, radius)

        /**
         * Rounded Rect Outlines
         */
        fun roundRectOutline(nvg: VG = vg, x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, color: Int, radius: Float, thickness: Float) =
            nvg.drawHollowRoundedRect(x.toFloat() * 2f - 1, y.toFloat() * 2f - 1, width.toFloat() * 2f + 1, height.toFloat() * 2f + 1, radius, color, thickness)

        fun roundRectOutline(x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, color: Int, radius: Float, thickness: Float) =
            roundRectOutline(vg, x, y, width, height, color, radius, thickness)

        fun roundRectOutline(color: Int, radius: Float, thickness: Float) =
            roundRectOutline(vg, dX, dY, dWidth, dHeight, color, radius, thickness)

        /**
         * Rects
         */
        fun drawRect(nvg: VG = vg, x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, color: Int) =
            nvg.drawRect(x.toFloat() * 2f, y.toFloat() * 2f, width.toFloat() * 2f, height.toFloat() * 2f, color)

        fun drawRect(x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight, color: Int) =
            drawRect(vg, x, y, width, height, color)

        /**
         * Scissors
         */
        fun setUpScissor(nvg: VG = vg, x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight) =
            ScissorHelper.INSTANCE.scissor(nvg.instance, x.toFloat() * 2f, y.toFloat() * 2f, width.toFloat() * 2f, height.toFloat() * 2f)

        fun setUpScissor(x: Number = dX, y: Number = dY, width: Number = dWidth, height: Number = dHeight) =
            setUpScissor(vg, x, y, width, height)

        fun endScissor(nvg: VG = vg) =
            ScissorHelper.INSTANCE.clearScissors(nvg.instance)

        fun endScissor() = endScissor(vg)
    }

    inline fun VG.startDraw(x: Number = 0, y: Number = 0, width: Number = 0, height: Number = 0, block: DrawScope.() -> Unit) {
        DrawScope(x, y, width, height, this).run(block)
    }
}