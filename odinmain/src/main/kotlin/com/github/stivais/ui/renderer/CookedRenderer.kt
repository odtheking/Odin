package com.github.stivais.ui.renderer

import me.odinmain.OdinMain.mc
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.TextPos
import me.odinmain.utils.render.roundedRectangle
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import me.odinmain.utils.render.GradientDirection as GradientDirectionOld
import me.odinmain.utils.render.gradientRect as gradientRectOld

// op renderer odin client
// maybe isn't as fair comparsion as i cba to make it use new colors since im gonig to replace it soon
// note: text size isn't same as nvg
object CookedRenderer : Renderer {

    override fun beginFrame(width: Float, height: Float) {
        val scaleFactor = ScaledResolution(mc).scaleFactor.toFloat()
        GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1f)
        GlStateManager.translate(0f, 0f, 0f)
    }

    override fun endFrame() {
    }

    override fun push() {
        GlStateManager.pushMatrix()
    }

    override fun pop() {
        GlStateManager.popMatrix()
    }

    override fun translate(x: Float, y: Float) {
        GlStateManager.translate(x, y, 0f)
    }

    override fun scale(x: Float, y: Float) {
        GlStateManager.scale(x, y, 0f)
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        roundedRectangle(x, y, w, h, Color(color))
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, tl: Float, bl: Float, br: Float, tr: Float) {
        val stupidColor = Color(color)
        roundedRectangle(x, y, w, h, stupidColor, stupidColor, stupidColor, 1f, tl, tr, bl, br, 1f)
    }

    override fun hollowRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        thickness: Float,
        color: Int,
        tl: Float,
        bl: Float,
        br: Float,
        tr: Float
    ) {
        val stupidColor = Color(color)
        roundedRectangle(x, y, w, h, Color.TRANSPARENT, stupidColor, Color.TRANSPARENT, thickness + 1f, tl, tr, bl, br, 1f)
    }

    override fun gradientRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color1: Int,
        color2: Int,
        direction: GradientDirection
    ) = gradientRect(x, y, w, h, color1, color2, 0f, direction)

    override fun gradientRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color1: Int,
        color2: Int,
        radius: Float,
        direction: GradientDirection
    ) {
        val stupidColor1 = Color(color1)
        val stupidColor2 = Color(color2)
        gradientRectOld(x, y, w, h, stupidColor1, stupidColor2, radius, getGradientDirection(direction))
    }

    private fun getGradientDirection(direction: GradientDirection) = when (direction) {
        GradientDirection.LeftToRight -> GradientDirectionOld.Right
        GradientDirection.TopToBottom -> GradientDirectionOld.Down
    }

    override fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font) {
        OdinFont.text(text, x, y, Color(color), size * 0.75f, verticalAlign = TextPos.Top)
    }

    override fun textWidth(text: String, size: Float, font: Font): Float {
        return OdinFont.getTextWidth(text, size * 0.75f)
    }

    private val scissors: ArrayList<IntArray> = arrayListOf()

    override fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
        val width = w.toInt() + 1
        val height = h.toInt() + 1
        val array = intArrayOf(x.toInt(), Display.getHeight() - y.toInt() - height, width, height)
        scissors.add(array)
        glEnable(GL_SCISSOR_TEST)
        glScissor(array[0], array[1], array[2], array[3])
    }

    override fun popScissor() {
        if (scissors.size == 0) throw IllegalStateException("Can't pop scissor, if no scissor is active")
        scissors.removeAt(scissors.size - 1)
        scissors.lastOrNull()?.let { glScissor(it[0], it[1], it[2], it[3]) } ?: glDisable(GL_SCISSOR_TEST)
    }
}