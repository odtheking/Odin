package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.vanilla.Panel
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.clickgui.vanilla.VanillaTextInput
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Color.Companion.hsbMax
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.DrawContextRenderer
import com.odtheking.odin.utils.render.DrawContextRenderer.drawCircle
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.ui.animations.EaseInOutAnimation
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

class ColorSetting(
    name: String,
    override val default: Color,
    val allowAlpha: Boolean = false,
    desc: String
) : VanillaRenderableSetting<Color>(name, desc), Saving {

    override var value: Color = default.copy()

    private val expandAnim = EaseInOutAnimation(200)
    private var extended = false

    private var drag: Int? = null

    private val mainSliderAnim       = LinearAnimation<Float>(100)
    private var mainSliderPrevSat    = 0f
    private var mainSliderPrevBright = 0f

    private val hueSliderAnim  = LinearAnimation<Float>(100)
    private var hueSliderPrev  = 0f

    private val alphaSliderAnim = LinearAnimation<Float>(100)
    private var alphaSliderPrev = 0f

    private val hexInput = VanillaTextInput(
        if (allowAlpha) 8 else 6,
        if (allowAlpha) 8 else 6,
        { value.hex(allowAlpha) },
        { newHex ->
            val clean = newHex.filter { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }
            if (clean.length == if (allowAlpha) 8 else 6)
                value = Color(if (allowAlpha) clean else clean.padEnd(8, 'F'))
        }
    )

    private val hueColors = intArrayOf(
        Color(255, 0, 0).rgba,   Color(255, 255, 0).rgba, Color(0, 255, 0).rgba,
        Color(0, 255, 255).rgba, Color(0, 0, 255).rgba,   Color(255, 0, 255).rgba,
        Color(255, 0, 0).rgba
    )

    private val PAD              = 6
    private val SB_TOP_OFFSET    = Panel.HEIGHT + 3
    private val SB_HEIGHT        = 112
    private val HUE_TOP_OFFSET   = SB_TOP_OFFSET + SB_HEIGHT + 3
    private val HUE_HEIGHT       = 10
    private val ALPHA_TOP_OFFSET = HUE_TOP_OFFSET + HUE_HEIGHT + 3
    private val ALPHA_HEIGHT     = 10
    private val HEX_BOX_HEIGHT   = 16
    private val HEX_BOTTOM_PAD   = 18

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)

        graphics.drawString(mc.font, name, x + PAD, y + Panel.HEIGHT / 2 - 4, Colors.WHITE.rgba, false)
        graphics.roundedFill(x + width - 28, y + Panel.HEIGHT / 2 - 7, x + width - PAD, y + Panel.HEIGHT / 2 + 7, value.rgba, 3)
        graphics.roundedOutline(x + width - 28, y + Panel.HEIGHT / 2 - 7, x + width - PAD, y + Panel.HEIGHT / 2 + 7, value.withAlpha(1f).darker().rgba, 1.5f, 3)

        if (!extended && !expandAnim.isAnimating()) return Panel.HEIGHT

        val animH = getHeight()
        val clipH = animH - Panel.HEIGHT
        if (clipH <= 0) return Panel.HEIGHT
        graphics.enableScissor(x, y + Panel.HEIGHT, x + width, y + Panel.HEIGHT + clipH)

        val sbX = x + PAD
        val sbW = width - PAD * 2
        val sbY = y + SB_TOP_OFFSET

        val corners = DrawContextRenderer.RoundedOptions(DrawContextRenderer.CornerRadii.uniform(4f))
        DrawContextRenderer.roundedFillGradient(
            graphics, sbX, sbY, sbX + sbW, sbY + SB_HEIGHT,
            Colors.WHITE.rgba, value.hsbMax().rgba,
            DrawContextRenderer.GradientDirection.LEFT_TO_RIGHT, corners
        )
        DrawContextRenderer.roundedFillGradient(
            graphics, sbX, sbY, sbX + sbW, sbY + SB_HEIGHT,
            Colors.TRANSPARENT.rgba, 0xFF000000.toInt(),
            DrawContextRenderer.GradientDirection.TOP_TO_BOTTOM, corners
        )

        val animSat    = mainSliderAnim.get(mainSliderPrevSat,    value.saturation, false)
        val animBright = mainSliderAnim.get(mainSliderPrevBright, value.brightness, false)
        val pX = sbX + (animSat * sbW).toInt()
        val pY = sbY + ((1f - animBright) * SB_HEIGHT).toInt()
        graphics.drawCircle(pX, pY, 4, Colors.WHITE.rgba)

        val hueY = y + HUE_TOP_OFFSET
        drawHueBar(graphics, sbX, hueY, sbW)

        val animHue = hueSliderAnim.get(hueSliderPrev, value.hue, false)
        val hX = sbX + (animHue * sbW).toInt()
        graphics.roundedFill(hX - 2, hueY - 1, hX + 2, hueY + HUE_HEIGHT + 1, Colors.WHITE.rgba, 2)

        if (allowAlpha) {
            val alphaY = y + ALPHA_TOP_OFFSET
            DrawContextRenderer.roundedFillGradient(
                graphics, sbX, alphaY, sbX + sbW, alphaY + ALPHA_HEIGHT,
                Colors.TRANSPARENT.rgba, value.withAlpha(1f).rgba,
                DrawContextRenderer.GradientDirection.LEFT_TO_RIGHT,
                DrawContextRenderer.RoundedOptions(DrawContextRenderer.CornerRadii.uniform(4f))
            )
            val animAlpha = alphaSliderAnim.get(alphaSliderPrev, value.alphaFloat, false)
            val aX = sbX + (animAlpha * sbW).toInt()
            graphics.roundedFill(aX - 2, alphaY - 1, aX + 2, alphaY + ALPHA_HEIGHT + 1, Colors.WHITE.rgba, 2)
        }

        drag?.let { updateFromDrag(it, mouseX, mouseY, x, y, sbW) }

        val totalH = Panel.HEIGHT + expandedContentHeight()
        val rectX  = x + width / 4
        val rectY  = y + totalH - HEX_BOTTOM_PAD

        graphics.roundedFill(rectX, rectY, rectX + width / 2, rectY + HEX_BOX_HEIGHT, gray38.rgba, 3)
        graphics.roundedOutline(rectX, rectY, rectX + width / 2, rectY + HEX_BOX_HEIGHT, ClickGUIModule.clickGUIColor.rgba, 1f, 3)

        hexInput.x = rectX + 3
        hexInput.y = rectY + 3
        hexInput.width  = width / 2 - 6
        hexInput.height = HEX_BOX_HEIGHT - 6
        hexInput.placeholder = ""
        hexInput.draw(graphics)

        graphics.disableScissor()
        return animH
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (click.button() == 0 && isAreaHovered(lastX + width - 28, lastY + Panel.HEIGHT / 2 - 7, 22, 14)) {
            expandAnim.start()
            extended = !extended
            return true
        }
        if (!extended || click.button() != 0) return false

        val sbX    = lastX + PAD
        val sbW    = width - PAD * 2
        val sbY    = lastY + SB_TOP_OFFSET
        val hueY   = lastY + HUE_TOP_OFFSET
        val alphaY = lastY + ALPHA_TOP_OFFSET

        drag = when {
            isAreaHovered(sbX, sbY,    sbW, SB_HEIGHT) -> 0
            isAreaHovered(sbX, hueY,   sbW, HUE_HEIGHT) -> 1
            allowAlpha && isAreaHovered(sbX, alphaY, sbW, ALPHA_HEIGHT) -> 2
            else -> null
        }

        drag?.let { updateFromDrag(it, mouseX, mouseY, lastX, lastY, sbW); return true }

        hexInput.x = lastX + width / 4 + 3
        hexInput.y = lastY + Panel.HEIGHT + expandedContentHeight() - HEX_BOTTOM_PAD + 3
        hexInput.width  = width / 2 - 6
        hexInput.height = HEX_BOX_HEIGHT - 6
        return hexInput.mouseClicked(mouseX, mouseY, click)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: MouseButtonEvent) {
        drag = null
        hexInput.mouseReleased()
    }

    override fun keyTyped(input: CharacterEvent): Boolean  = if (extended) hexInput.keyTyped(input)  else false
    override fun keyPressed(input: KeyEvent): Boolean      = if (extended) hexInput.keyPressed(input) else false

    override val isHovered: Boolean
        get() = isAreaHovered(lastX + width - 28, lastY + Panel.HEIGHT / 2 - 7, 22, 14)

    private fun expandedContentHeight(): Int = if (allowAlpha) 168 else 152

    override fun getHeight(): Int {
        val collapsed = Panel.HEIGHT.toFloat()
        val fullOpen  = (Panel.HEIGHT + expandedContentHeight()).toFloat()
        return expandAnim.get(collapsed, fullOpen, !extended).toInt()
    }

    override fun write(gson: Gson): JsonElement = gson.toJsonTree(value, Color::class.java)
    override fun read(element: JsonElement, gson: Gson) {
        value = gson.fromJson(element, Color::class.java) ?: default.copy()
    }

    private fun drawHueBar(graphics: GuiGraphics, x: Int, y: Int, sbW: Int) {
        val segment = sbW / 6f
        for (i in 0 until 6) {
            val segR = DrawContextRenderer.CornerRadii(
                if (i == 0) 4f else 0f,  // topLeft
                if (i == 5) 4f else 0f,  // topRight
                if (i == 5) 4f else 0f,  // bottomRight
                if (i == 0) 4f else 0f   // bottomLeft
            )
            DrawContextRenderer.roundedFillGradient(
                graphics,
                (x + i * segment).toInt(), y,
                (x + (i + 1) * segment).toInt(), y + HUE_HEIGHT,
                hueColors[i], hueColors[i + 1],
                DrawContextRenderer.GradientDirection.LEFT_TO_RIGHT,
                DrawContextRenderer.RoundedOptions(segR)
            )
        }
    }

    private fun updateFromDrag(section: Int, mouseX: Int, mouseY: Int, x: Int, y: Int, sbW: Int) {
        val sbY = y + SB_TOP_OFFSET
        when (section) {
            0 -> {
                val newSat    = ((mouseX - (x + PAD).toFloat()) / sbW).coerceIn(0f, 1f)
                val newBright = (1f - ((mouseY - sbY.toFloat()) / SB_HEIGHT)).coerceIn(0f, 1f)
                if (newSat != value.saturation || newBright != value.brightness) {
                    mainSliderPrevSat    = mainSliderAnim.get(mainSliderPrevSat,    value.saturation, false)
                    mainSliderPrevBright = mainSliderAnim.get(mainSliderPrevBright, value.brightness, false)
                    mainSliderAnim.start()
                    value.saturation = newSat
                    value.brightness = newBright
                }
            }
            1 -> {
                val newHue = ((mouseX - (x + PAD).toFloat()) / sbW).coerceIn(0f, 1f)
                if (newHue != value.hue) {
                    hueSliderPrev = hueSliderAnim.get(hueSliderPrev, value.hue, false)
                    hueSliderAnim.start()
                    value.hue = newHue
                }
            }
            2 -> {
                val newAlpha = ((mouseX - (x + PAD).toFloat()) / sbW).coerceIn(0f, 1f)
                if (newAlpha != value.alphaFloat) {
                    alphaSliderPrev = alphaSliderAnim.get(alphaSliderPrev, value.alphaFloat, false)
                    alphaSliderAnim.start()
                    value.alphaFloat = newAlpha
                }
            }
        }
    }
}