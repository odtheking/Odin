package com.github.stivais.ui.renderer

import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.color.blue
import com.github.stivais.ui.color.green
import com.github.stivais.ui.color.red
import me.odinmain.OdinMain.mc
import net.minecraft.client.renderer.GlStateManager
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL2.nvgCreate
import org.lwjgl.opengl.GL11
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.round


object NVGRenderer : Renderer {

    private var vg: Long = -1

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
    }

    override fun endFrame() {
        if (!drawing) throw IllegalStateException("[NVGRenderer] Not drawing, but called endFrame")
        nvgEndFrame(vg)
        GL11.glPopAttrib()
        GlStateManager.enableCull()
        drawing = false
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        nvgBeginPath(vg)
        nvgRect(vg, round(x), round(y), round(w), round(h))
        val nvgColor = color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
        nvgColor.free()
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, tl: Float, bl: Float, br: Float, tr: Float) {
        nvgBeginPath(vg)
        nvgRoundedRectVarying(vg, x, y, w, h, tl, tr, br, bl)
        val nvgColor = color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
        nvgColor.free()
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
        val nvgColor = color(color)
        nvgStrokeColor(vg, nvgColor)
        nvgStroke(vg)
        nvgColor.free()
    }

    override fun text(text: String, x: Float, y: Float, size: Float, color: Int) {
        nvgBeginPath(vg)
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, tempFont.id)
        nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
        val nvgColor = color(color)
        nvgFillColor(vg, nvgColor)
        nvgText(vg, x, y, text)
        nvgClosePath(vg)
        nvgColor.free()
    }

    override fun textWidth(text: String, size: Float): Float {
        val bounds = FloatArray(4)
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, tempFont.id)
        return nvgTextBounds(vg, 0f, 0f, text, bounds)
    }

    override fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
        nvgScissor(vg, x, y, w, h)
    }

    override fun popScissor() {
        nvgResetScissor(vg)
    }

    fun color(color: Int): NVGColor {
        val nvgColor = NVGColor.calloc()
        nvgRGBA(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte(), nvgColor)
        return nvgColor
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