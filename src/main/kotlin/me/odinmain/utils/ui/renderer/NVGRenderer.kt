package me.odinmain.utils.ui.renderer

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.renderer.Renderer
import com.github.stivais.aurora.renderer.data.Font
import com.github.stivais.aurora.renderer.data.Gradient
import com.github.stivais.aurora.renderer.data.Image
import com.github.stivais.aurora.utils.alpha
import com.github.stivais.aurora.utils.blue
import com.github.stivais.aurora.utils.green
import com.github.stivais.aurora.utils.red
import me.odin.lwjgl.Lwjgl3Wrapper
import me.odin.lwjgl.Lwjgl3Wrapper.*
import me.odin.lwjgl.NanoVGColorWrapper
import me.odin.lwjgl.NanoVGPaintWrapper
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.wrapper
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*

// TODO: Needs scissors
object NVGRenderer : Renderer, Lwjgl3Wrapper by wrapper {

    private val nvgPaint: NanoVGPaintWrapper = createPaint()
    private val nvgColor: NanoVGColorWrapper = createColor()
    private val nvgColor2: NanoVGColorWrapper = createColor()

    private val fonts = HashMap<Font, Int>()

    private val images = HashMap<Image, NVGImage>()

    private var vg: Long = -1

    // used in getTextWidth to avoid reallocating
    private val fontBounds = FloatArray(4)

//    private val scissorStack = Stack<Scissor>()

    private var drawing: Boolean = false

    init {
        vg = nvgCreate(NVG_ANTIALIAS or NVG_DEBUG)
        require(vg != -1L) { "Failed to initialize NanoVG" }
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

    override fun push() = nvgSave(vg)

    override fun pop() = nvgRestore(vg)

    override fun scale(x: Float, y: Float) = nvgScale(vg, x, y)

    override fun translate(x: Float, y: Float) = nvgTranslate(vg, x, y)

    override fun rotate(amount: Float) = nvgRotate(vg, amount)

    override fun globalAlpha(amount: Float) = nvgGlobalAlpha(vg, amount.coerceIn(0f, 1f))

    override fun pushScissor(x: Float, y: Float, w: Float, h: Float) = nvgScissor(vg, x, y, w, h)

    override fun popScissor() = nvgResetScissor(vg)

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
        nvgRoundedRectVarying(vg, x, y, w, h + .5f, tl, tr, br, bl)
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
        nvgRGBA(0, 0, 0, 125, nvgColor)
        nvgRGBA(0, 0, 0, 0, nvgColor2)

        nvgBoxGradient(vg, x - spread, y - spread, width + 2 * spread, height + 2 * spread, tl + spread, blur, nvgColor, nvgColor2, nvgPaint)
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x - spread - blur, y - spread - blur, width + 2 * spread + 2 * blur, height + 2 * spread + 2 * blur, tl + spread)
        nvgRoundedRect(vg, x, y, width, height, tl)
        nvgPathWinding(vg, NVG_HOLE)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    override fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font) {
        nvgBeginPath(vg)
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getIDFromFont(font))
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgText(vg, x, y + .5f, text)
        nvgClosePath(vg)
    }

    override fun textWidth(text: String, size: Float, font: Font): Float {
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getIDFromFont(font))
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
        nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint)
        nvgBeginPath(vg)
        nvgRoundedRectVarying(vg, x, y, w, h, tl, tr, br, bl)
        color(Color.RED.rgba)
        nvgFillColor(vg, nvgColor)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    override fun createImage(image: Image) {
        when (image.type) {
            Image.Type.RASTER -> {
                images.getOrPut(image) { NVGImage(0, loadImage(image)) }.count++
            }
            Image.Type.VECTOR -> {
                images.getOrPut(image) { NVGImage(0, loadSVG(image)) }.count++
            }
        }
    }

    // lowers reference count by 1, if it reaches 0 it gets deleted from mem
    override fun deleteImage(image: Image) {
        val nvgImage = images[image] ?: return
        nvgImage.count--
        if (nvgImage.count == 0) {
            nvgDeleteImage(vg, nvgImage.nvg)
            images.remove(image)
        }
    }

    private fun getImage(image: Image): Int {
        return images[image]?.nvg ?: throw IllegalStateException("Image (${image.resourcePath}) doesn't exist")
    }

    private fun loadImage(image: Image): Int {
        val w = IntArray(1)
        val h = IntArray(1)
        val channels = IntArray(1)
        val buffer = stbi_load_from_memory(
            image.buffer(),
            w,
            h,
            channels,
            4
        ) ?: throw NullPointerException("Failed to load image: ${image.resourcePath}")
        return nvgCreateImageRGBA(vg, w[0], h[0], 0, buffer)
    }

    private fun loadSVG(image: Image): Int {
        val vec = image.stream.use { it.bufferedReader().readText() }
        val svg = nsvgParse(vec, "px", 96f) ?: throw IllegalStateException("Failed to parse ${image.resourcePath}")

        image.width = svg.width()
        image.height = svg.height()

        val memAlloc = memAlloc((svg.width() * svg.height() * 4).toInt())
        val rasterizer = nsvgCreateRasterizer()
        nsvgRasterize(rasterizer, svg, 0f, 0f, 1f, memAlloc, svg.width().toInt(), svg.height().toInt(), svg.width().toInt() * 4)
        val nvgImage = nvgCreateImageRGBA(vg, svg.width().toInt(), svg.height().toInt(), 0, memAlloc)
        nsvgDeleteRasterizer(rasterizer)
        nsvgDelete(svg)
        return nvgImage
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

    private fun getIDFromFont(font: Font): Int {
        return fonts[font] ?: nvgCreateFontMem(vg, font.name, font.buffer, 0).also {
            fonts[font] = it
        }
    }

    private data class NVGImage(var count: Int, val nvg: Int)
}