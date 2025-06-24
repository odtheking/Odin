package me.odinmain.aurora

import com.github.stivais.aurora.renderer.Renderer
import com.github.stivais.aurora.renderer.data.Font
import com.github.stivais.aurora.renderer.data.Gradient
import com.github.stivais.aurora.renderer.data.Image
import com.github.stivais.aurora.utils.alpha
import com.github.stivais.aurora.utils.blue
import com.github.stivais.aurora.utils.green
import com.github.stivais.aurora.utils.red
import me.odin.lwjgl.Lwjgl3Loader
import me.odin.lwjgl.Lwjgl3Wrapper
import me.odin.lwjgl.Lwjgl3Wrapper.*
import me.odin.lwjgl.NanoVGColorWrapper
import me.odin.lwjgl.NanoVGPaintWrapper
import me.odinmain.OdinMain.mc
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

object NVGRenderer : Renderer, Lwjgl3Wrapper by Lwjgl3Loader.load() {

    private var vg: Long = -1L

    private var drawing: Boolean = false

    private val nvgPaint: NanoVGPaintWrapper = NVGRenderer.createPaint()
    private val nvgColor: NanoVGColorWrapper = NVGRenderer.createColor()
    private val nvgColor2: NanoVGColorWrapper = NVGRenderer.createColor()

    private val fontMap = HashMap<Font, NVGFont>()
    private val fontBounds = FloatArray(4)

    private var scissor: Scissor? = null

    init {
        vg = nvgCreate(NVG_ANTIALIAS)
        require(vg != -1L) { "[NVGRenderer] Failed to initialize NanoVG." }
    }

    override fun beginFrame(width: Float, height: Float) {
        if (drawing) throw IllegalStateException("[NVGRenderer] Already drawing, but called beginFrame")
        drawing = true
        GlStateManager.pushMatrix()
        if (!mc.framebuffer.isStencilEnabled) mc.framebuffer.enableStencil()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        GlStateManager.disableAlpha()
        nvgBeginFrame(vg, width, height, 1f)
        nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
    }

    override fun endFrame() {
        if (!drawing) throw IllegalStateException("[NVGRenderer] Not drawing, but called endFrame")
        nvgEndFrame(vg)
        glPopAttrib()
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        drawing = false
    }

    // MAYBE track states here instead of relying on nanovg?
    override fun push() = nvgSave(vg)

    override fun pop() = nvgRestore(vg)

    override fun scale(x: Float, y: Float) = nvgScale(vg, x, y)

    override fun translate(x: Float, y: Float) = nvgTranslate(vg, x, y)

    override fun rotate(amount: Float) = nvgRotate(vg, amount)

    override fun globalAlpha(amount: Float) = nvgGlobalAlpha(vg, amount.coerceIn(0f, 1f))

    override fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
        scissor = Scissor(scissor, x, y, w + x, h + y)
        scissor?.applyScissor()
        // DO SPECIAL STUFF
    }

    override fun popScissor() {
        nvgResetScissor(vg)
        scissor = scissor?.previous
        scissor?.applyScissor()
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) {
        nvgBeginPath(vg)
        nvgMoveTo(vg, x1, y1)
        nvgLineTo(vg, x2, y2)
        nvgStrokeWidth(vg, thickness)
        color(color)
        nvgStrokeColor(vg, nvgColor)
        nvgStroke(vg)
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
        gradient: Gradient,
        tl: Float,
        bl: Float,
        br: Float,
        tr: Float
    ) {
        nvgBeginPath(vg)
        nvgRoundedRectVarying(vg, x, y, w, h, tl, tr, br, bl)
        gradient(color1, color2, x, y, w, h, gradient)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    override fun dropShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int,
        blur: Float,
        spread: Float,
        tl: Float,
        bl: Float,
        br: Float,
        tr: Float
    ) {
        // TODO: MAKE THIS LOOK NOT ASS
        color(color)
        nvgRGBA(0, 0, 0, 0, nvgColor2)

        nvgBoxGradient(vg, x - spread, y - spread, width + 2 * spread, height + 2 * spread, tl + spread, blur, nvgColor, nvgColor2, nvgPaint)
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x - spread - blur, y - spread - blur, width + 2 * spread + 2 * blur, height + 2 * spread + 2 * blur, tl + spread)
        nvgRoundedRect(vg, x, y, width, height, tl)
        nvgPathWinding(vg, NVG_HOLE)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    override fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font, blur: Float) {
        nvgBeginPath(vg)
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getFontID(font))
        nvgFontBlur(vg, blur)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgText(vg, x, y, text)
        nvgClosePath(vg)
    }

    override fun textWidth(text: String, size: Float, font: Font): Float {
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getFontID(font))
        return nvgTextBounds(vg, 0f, 0f, text, fontBounds)
    }

    override fun image(
        image: Image,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        tl: Float,
        bl: Float,
        br: Float,
        tr: Float
    ) {
        TODO("Not yet implemented")
    }

    override fun createImage(image: Image) {
        TODO("Not yet implemented")
    }

    override fun deleteImage(image: Image) {
        TODO("Not yet implemented")
    }


    private fun color(color: Int) {
        nvgRGBA(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte(), nvgColor)
    }

    private fun color(color1: Int, color2: Int) {
        nvgRGBA(color1.red.toByte(), color1.green.toByte(), color1.blue.toByte(), color1.alpha.toByte(), nvgColor)
        nvgRGBA(color2.red.toByte(), color2.green.toByte(), color2.blue.toByte(), color2.alpha.toByte(), nvgColor2)
    }

    private fun gradient(color1: Int, color2: Int, x: Float, y: Float, w: Float, h: Float, direction: Gradient) {
        color(color1, color2)
        when (direction) {
            Gradient.LeftToRight -> nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint)
            Gradient.TopToBottom -> nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint)
        }
    }

    private fun getFontID(font: Font): Int {
        return fontMap[font]?.id ?: kotlin.run {
            val buffer = font.buffer()
            val mem = nvgCreateFontMem(vg, font.name, buffer, 0)
            fontMap[font] = NVGFont(mem, buffer)
            mem
        }
    }

    private class Scissor(val previous: Scissor?, val x: Float, val y: Float, val maxX: Float, val maxY: Float) {
        fun applyScissor() {
            if (previous == null) {
                nvgScissor(vg, x, y, maxX - x, maxY - y)
            } else {
                val x = max(x, previous.x)
                val y = max(y, previous.y)
                val width = max(0f, (min(maxX, previous.maxX) - x))
                val height = max(0f, (min(maxY, previous.maxY) - y))
                nvgScissor(vg, x, y, width, height)
            }
        }
    }

    private data class NVGFont(val id: Int, val buffer: ByteBuffer)
}