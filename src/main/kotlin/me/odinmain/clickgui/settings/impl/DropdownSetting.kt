package me.odinmain.clickgui.settings.impl

import me.odinmain.clickgui.ClickGUI
import me.odinmain.clickgui.settings.RenderableSetting
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.HoverHandler
import me.odinmain.utils.ui.animations.LinearAnimation
import me.odinmain.utils.ui.isAreaHovered
import me.odinmain.utils.ui.rendering.NVGRenderer

/**
 * A setting intended to show or hide other settings in the GUI.
 *
 * @author Bonsai
 */
class DropdownSetting(
    name: String,
    override val default: Boolean = false,
    desc: String = ""
) : RenderableSetting<Boolean>(name, desc) {

    override var value: Boolean = default
    private var enabled: Boolean by this::value

    private val toggleAnimation = LinearAnimation<Float>(200)
    private val hoverHandler = HoverHandler(150)

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        val height = getHeight()

        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        hoverHandler.handle(lastX + width - 30f, lastY + getHeight() / 2f - 16f, 24f, 24f)

        val imageSize = 24f + (6f * hoverHandler.percent() / 100f)
        val offset = (imageSize - 24f) / 2f

        NVGRenderer.push()
        NVGRenderer.translate(x + width - 18f, y + height / 2f - 4f)
        NVGRenderer.rotate(toggleAnimation.get(0f, Math.PI.toFloat() / 2f, enabled))
        NVGRenderer.translate(-(12f + offset), -(12f + offset))
        NVGRenderer.image(ClickGUI.chevronImage, 0f, 0f, imageSize, imageSize)
        NVGRenderer.pop()

        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton != 0 || !isHovered) return false
        enabled = !enabled
        toggleAnimation.start()
        return true
    }

    override val isHovered: Boolean get() = isAreaHovered(lastX + width - 30f, lastY + getHeight() / 2f - 16f, 24f, 24f)
}