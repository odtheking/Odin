package com.github.stivais.ui.renderer

import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.color.blue
import com.github.stivais.ui.color.green
import com.github.stivais.ui.color.red
import org.apache.commons.io.IOUtils
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL2.nvgCreate
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder


object NVGRenderer : Renderer {

    private var vg: Long = -1

    var drawing: Boolean = false

    private var tempFont: NVGFont

    init {
        vg = nvgCreate(NVG_ANTIALIAS)
        require(vg != -1L) { "Failed to initialize NanoVG" }
        tempFont = NVGFont("Regular", "/assets/odinmain/fonts/Regular.ttf")
    }

    override fun beginFrame(width: Float, height: Float) {
        if (drawing) throw IllegalStateException("Already drawing, but called NVGRenderer beginFrame")
        drawing = true
        nvgBeginFrame(vg, width, height, 1f)
    }

    override fun endFrame() {
        if (!drawing) throw IllegalStateException("Not drawing, but called NVGRenderer endFrame")
        nvgEndFrame(vg)
        drawing = false
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        nvgBeginPath(vg)
        nvgRect(vg, x, y, w, h)
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
        nvgColor.free()
    }

    override fun textWidth(text: String, size: Float): Float {
        //
        return 0f
    }

    fun color(color: Int): NVGColor {
        val nvgColor = NVGColor.calloc()
        nvgRGBA(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte(), nvgColor)
        return nvgColor
    }

    class NVGFont(val name: String, path: String) {

        val id: Int

        init {
            val stream = this::class.java.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            val bytes =  IOUtils.toByteArray(stream)
            stream.close()
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            data.flip()
            id = nvgCreateFontMem(vg, name, data, false)
        }
    }
}