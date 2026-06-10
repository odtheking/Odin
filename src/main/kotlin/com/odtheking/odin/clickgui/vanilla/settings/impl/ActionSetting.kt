package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent

class ActionSetting(
    name: String,
    desc: String,
    override val default: () -> Unit = {}
) : VanillaRenderableSetting<() -> Unit>(name, desc) {

    override var value: () -> Unit = default

    var action: () -> Unit by this::value

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)
        val height = getHeight()
        graphics.roundedFill(x + 3, y + 2, x + width - 3, y + 19, gray38.rgba, 6, ClickGUIModule.clickGUIColor.rgba, 1.5f)
        val color = if (isHovered) Colors.WHITE.darker().rgba else Colors.WHITE.rgba

        graphics.drawCenteredString(mc.font, name, x + width / 2, y + height / 2 - 4, color)
        return height
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        return if (click.button() != 0 || !isHovered) false
        else {
            action()
            true
        }
    }
}