package me.odinmain.clickgui.settings.impl

import me.odinmain.clickgui.ClickGUI.gray38
import me.odinmain.clickgui.settings.RenderableSetting
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Color.Companion.darker
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.rendering.NVGRenderer

class ActionSetting(
    name: String,
    desc: String,
    override val default: () -> Unit = {}
) : RenderableSetting<() -> Unit>(name, desc) {

    override var value: () -> Unit = default

    var action: () -> Unit by this::value

    private val textWidth by lazy { NVGRenderer.textWidth(name, 16f, NVGRenderer.defaultFont) }

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        val height = getHeight()

        NVGRenderer.rect(x + 4f, y + height / 2f - 13f, width - 8f, 26f, gray38.rgba, 6f)
        NVGRenderer.hollowRect(x + 4f, y + height / 2f - 13f, width - 8f, 26f, 2f, ClickGUIModule.clickGUIColor.rgba, 6f)
        NVGRenderer.text(name, x + width / 2f - textWidth / 2, y + height / 2f - 8f, 16f, if (isHovered) Colors.WHITE.darker().rgba else Colors.WHITE.rgba, NVGRenderer.defaultFont)
        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        return if (mouseButton != 0 || !isHovered) false
        else {
            action()
            true
        }
    }
}