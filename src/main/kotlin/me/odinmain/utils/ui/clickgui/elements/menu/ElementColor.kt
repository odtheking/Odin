package me.odinmain.utils.ui.clickgui.elements.menu

import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.*
import me.odinmain.utils.render.RenderUtils.bind
import me.odinmain.utils.render.RenderUtils.loadBufferedImage
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.animations.impl.ColorAnimation
import me.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.utils.ui.clickgui.elements.Element
import me.odinmain.utils.ui.clickgui.elements.ElementType
import me.odinmain.utils.ui.clickgui.elements.ModuleButton
import me.odinmain.utils.ui.clickgui.util.ColorUtil
import me.odinmain.utils.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.utils.ui.clickgui.util.ColorUtil.buttonColor
import me.odinmain.utils.ui.clickgui.util.ColorUtil.darker
import me.odinmain.utils.ui.clickgui.util.ColorUtil.elementBackground
import me.odinmain.utils.ui.clickgui.util.ColorUtil.hsbMax
import me.odinmain.utils.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.ui.clickgui.util.HoverHandler
import me.odinmain.utils.ui.util.MouseUtils.isAreaHovered
import me.odinmain.utils.ui.util.MouseUtils.mouseX
import me.odinmain.utils.ui.util.MouseUtils.mouseY
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.input.Keyboard
import kotlin.math.floor

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ElementColor(parent: ModuleButton, setting: ColorSetting) :
    Element<ColorSetting>(parent, setting, ElementType.COLOR) {

    private val anim = EaseInOut(200)
    var dragging: Int? = null

    inline val color: Color
        get() = setting.value

    private val hover = HoverHandler(0, 150)
    private val hueGradiant = DynamicTexture(loadBufferedImage("/assets/odinmain/clickgui/HueGradient.png"))

    private var hexString = "#FFFFFFFF"
    private var stringBefore = hexString
    private val colorAnim = ColorAnimation(100)
    private var listeningForString = false

    // TODO: MAKE A BETTER DESIGN (FUNCTION IS ALL HERE P MUCH)
    override fun draw() {
        h = floor(anim.get(36f, if (setting.allowAlpha) 285f else 255f, !extended))

        hover.handle(x + w - 41, y + 5, 31.5f, 19f)

        roundedRectangle(x, y, w, h, elementBackground)
        text(name, x + 6f, y + 18f, textColor, 12f, OdinFont.REGULAR)
        dropShadow(x + w - 40f, y + 5f, 31f, 19f, 10f, 0.75f)
        roundedRectangle(x + w - 40f, y + 5f, 31f, 19f, color.brighter(1 + hover.percent() / 500f), 5f)
        rectangleOutline(x + w - 40f, y + 5f, 31f, 19f, color.darker().withAlpha(1f), 5f, 1.5f)

        if (!extended && !anim.isAnimating()) return

        val scissor = scissor(x, y, w, h + 1)

        // SATURATION AND BRIGHTNESS
        dropShadow(x + 10f, y + 38f, w - 20f, 170f, 10f, 0.5f)
        drawHSBBox(x + 10f, y + 38f, w - 20f, 170f, color.hsbMax())

        val sbPointer = Pair((x + 10f + setting.saturation * 220), (y + 38f + (1 - setting.brightness) * 170))
        circle(sbPointer.first, sbPointer.second, 9f, Colors.TRANSPARENT, color.darker(.5f).withAlpha(1f), 3f)

        // HUE

        dropShadow(x + 10f, y + 214f, w - 20f, 15f, 10f, 0.5f)
        drawDynamicTexture(hueGradiant, x + 10f, y + 214f, w - 20f, 15f)
        rectangleOutline(x + 10f, y + 214f, w - 20f, 15f, buttonColor, 1f, 2.5f)

        val hue = x + 10f + setting.hue * 221f to y + 221f
        dropShadow(hue.first - 8.5f, hue.second - 8.5f, 17f, 17f, 2.5f, 2.5f)
        circle(hue.first, hue.second, 9f, color.hsbMax(), color.hsbMax().darker(.5f), 2f)

        // ALPHA
        if (setting.allowAlpha) {
            dropShadow(x + 10f, y + 235f, w - 20f, 15f, 10f, 0.5f)
            gradientRect(x + 10f, y + 235f, w - 20f, 15f, Colors.TRANSPARENT, color.withAlpha(1f), 5f, GradientDirection.Right, Colors.MINECRAFT_DARK_GRAY, 2.5f)

            val alpha = Pair((x + 10f + setting.alpha * 220f), y + 243f)
            dropShadow(alpha.first - 8.5f, alpha.second - 8.5f, 17f, 17f, 2.5f, 2.5f)
            circle(alpha.first, alpha.second, 9f, Colors.WHITE.withAlpha(setting.alpha), Colors.MINECRAFT_GRAY, 2f)
        }

        when (dragging) {
            0 -> {
                setting.saturation = (mouseX - (x + 10f)) / 220f
                setting.brightness = -((mouseY - (y + 38f)) - 170f) / 170f
            }
            1 -> setting.hue = (mouseX - (x + 10f)) / (w - 20f)
            2 -> setting.alpha = (mouseX - (x + 10f)) / (w - 20f)
        }

        if (dragging != null) {
            hexString = "#${color.hex}"
            stringBefore = hexString
        }

        val stringWidth = getTextWidth(hexString, 12f)
        roundedRectangle(x + w / 2 - stringWidth / 2 - 12, y + 260, stringWidth + 24, 22f, buttonColor, 5f)
        text(hexString, x + w / 2, y + 271, Colors.WHITE, 12f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle)

        if (listeningForString || colorAnim.isAnimating()) {
            val color = colorAnim.get(ColorUtil.clickGUIColor, buttonColor, listeningForString)
            rectangleOutline(x + w / 2 - stringWidth / 2 - 13 , y + 259, stringWidth + 25f, 23f, color, 5f,2f)
        }

        resetScissor(scissor)
        Colors.WHITE.bind()
    }

    private fun completeHexString() {
        if (colorAnim.isAnimating()) return
        if (colorAnim.start()) listeningForString = false
        if (hexString.isEmpty()) return
        val stringWithoutHash = hexString.substring(1)
        if (stringWithoutHash.length.equalsOneOf(6, 8)) {
            try {
                val alpha = if (stringWithoutHash.length == 8) stringWithoutHash.substring(6).toInt(16) / 255f else 1f
                val red = stringWithoutHash.substring(0, 2).toInt(16)
                val green = stringWithoutHash.substring(2, 4).toInt(16)
                val blue = stringWithoutHash.substring(4, 6).toInt(16)
                setting.value = Color(red, green, blue, alpha)
                stringBefore = hexString
            } catch (_: Exception) {
                hexString = stringBefore
                return
            }
        } else hexString = stringBefore
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listeningForString) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> completeHexString()
                Keyboard.KEY_BACK -> hexString = hexString.dropLast(1)
                !in ElementTextField.keyBlackList -> hexString += typedChar.toString()
            }
            hexString = hexString.uppercase()
            return true
        }
        return false
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isHovered) {
                if (anim.start()) extended = !extended
                return true
            }
            if (!extended) return false

            dragging = when {
                isAreaHovered(x + 10f, y + 38f, w - 20f, 170f) -> 0 // sat & brightness
                isAreaHovered(x + 10f, y + 214f, w - 20f, 15f) -> 1 // hue
                isAreaHovered(x + 10f, y + 235f, w - 20f, 15f) && setting.allowAlpha -> 2 // alpha
                else -> null
            }

            if (isAreaHovered(x + 10f, y + 255f, w, y + 278f)) {
                if (!colorAnim.isAnimating()) {
                    if (listeningForString) completeHexString()
                    else listeningForString = true
                }
            } else if (listeningForString) listeningForString = false

        } else if (mouseButton == 1) {
            if (isHovered) {
                if (anim.start()) extended = !extended
                return true
            }
        }
        return false
    }

    override fun mouseReleased(state: Int) {
        dragging = null
    }

    override val isHovered: Boolean
        get() = isAreaHovered(x + w - 41, y + 5, 31.5f, 19f)
}