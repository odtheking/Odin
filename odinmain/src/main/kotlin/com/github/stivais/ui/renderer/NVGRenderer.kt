package com.github.stivais.ui.renderer

import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.color.blue
import com.github.stivais.ui.color.green
import com.github.stivais.ui.color.red
import me.odinmain.OdinMain.mc
import net.minecraft.client.renderer.GlStateManager
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL2.nvgCreate
import org.lwjgl.opengl.GL11
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.round


object NVGRenderer : Renderer {

    private val nvgPaint: NVGPaint = NVGPaint.malloc()
    private val nvgColor: NVGColor = NVGColor.malloc()
    private val nvgColor2: NVGColor = NVGColor.malloc()

    private var vg: Long = -1

    // used in getTextWidth to avoid reallocating
    private val fontBounds = FloatArray(4)

    // this will be used later for fixing bugs caused by nvg i think
    var drawing: Boolean = false

    private var tempFont: NVGFont

    init {
        vg = nvgCreate(NVG_ANTIALIAS)
        require(vg != -1L) { "Failed to initialize NanoVG" }
        tempFont = NVGFont("Regular", "/assets/odinmain/fonts/Regular.otf")
    }

    override fun beginFrame(width: Float, height: Float) {
        if (drawing) throw IllegalStateException("[NVGRenderer] Already drawing, but called beginFrame")
        drawing = true
        if (!mc.framebuffer.isStencilEnabled) mc.framebuffer.enableStencil()
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GlStateManager.disableCull()
        nvgBeginFrame(vg, width, height, 1f)
        nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
    }

    override fun endFrame() {
        if (!drawing) throw IllegalStateException("[NVGRenderer] Not drawing, but called endFrame")
        nvgEndFrame(vg)
        GL11.glPopAttrib()
        GlStateManager.enableCull()
        drawing = false
    }

    override fun push() = nvgSave(vg)

    override fun pop() = nvgRestore(vg)

    override fun scale(x: Float, y: Float) = nvgScale(vg, x, y)

    override fun translate(x: Float, y: Float) = nvgTranslate(vg, x, y)

    override fun pushScissor(x: Float, y: Float, w: Float, h: Float) = nvgScissor(vg, x, y, w, h)

    override fun popScissor() = nvgResetScissor(vg)

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        nvgBeginPath(vg)
        nvgRect(vg, round(x), round(y), round(w), round(h))
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, tl: Float, bl: Float, br: Float, tr: Float) {
        nvgBeginPath(vg)
        nvgRoundedRectVarying(vg, x, y, w, h, tl, tr, br, bl)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
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
        nvgBeginPath(vg)
        nvgRoundedRectVarying(vg, x, y, w, h, tl, tr, br, bl)
        nvgStrokeWidth(vg, thickness)
        nvgPathWinding(vg, NVG_HOLE)
        color(color)
        nvgStrokeColor(vg, nvgColor)
        nvgStroke(vg)
    }

    override fun gradientRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color1: Int,
        color2: Int,
        direction: GradientDirection
    ) {
        nvgBeginPath(vg)
        nvgRect(vg, x, y, w, h)
        gradient(color1, color2, x, y, w, h, direction)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

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
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x, y, w, h, radius)
        gradient(color1, color2, x, y, w, h, direction)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    override fun text(text: String, x: Float, y: Float, size: Float, color: Int) {
        nvgBeginPath(vg)
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, tempFont.id)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgText(vg, x, y, text)
        nvgClosePath(vg)
    }

    override fun textWidth(text: String, size: Float): Float {
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, tempFont.id)
        return nvgTextBounds(vg, 0f, 0f, text, fontBounds)
    }

    private fun color(color: Int) {
        nvgRGBA(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte(), nvgColor)
    }

    private fun gradient(color1: Int, color2: Int, x: Float, y: Float, w: Float, h: Float, direction: GradientDirection) {
        nvgRGBA(color1.red.toByte(), color1.green.toByte(), color1.blue.toByte(), color1.alpha.toByte(), nvgColor)
        nvgRGBA(color2.red.toByte(), color2.green.toByte(), color2.blue.toByte(), color2.alpha.toByte(), nvgColor2)
        when (direction) {
            GradientDirection.LeftToRight -> nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint)
            GradientDirection.TopToBottom -> nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint)
        }
    }

    class NVGFont(val name: String, path: String) {

        var id: Int = -1
        private val buffer: ByteBuffer

        init {
            val stream = this::class.java.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            val bytes =  IOUtils.toByteArray(stream)
            stream.close()
            buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            buffer.flip()
            id = nvgCreateFontMem(vg, name, buffer, false)
            require(id != -1) { "Font failed to initialize" }
        }
    }
}