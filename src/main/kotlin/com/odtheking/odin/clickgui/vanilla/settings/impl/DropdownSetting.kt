package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.resources.Identifier

/**
 * A setting intended to show or hide other settings in the GUI.
 *
 * @author Bonsai
 */
class DropdownSetting(
    name: String,
    override val default: Boolean = false,
    desc: String = ""
) : VanillaRenderableSetting<Boolean>(name, desc) {

    override var value: Boolean = default
    private var enabled: Boolean by this::value

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)
        val height = getHeight()
        graphics.drawString(mc.font, name, x + 6, y + height / 2 - 4, Colors.WHITE.rgba, false)
        val iconX = x + width - 22
        val iconY = y + height / 2 - 8
        graphics.blit(chevron, iconX, iconY, iconX + 48, iconY + 48, 0f, 0f, 16f, 16f)
        return height
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        if (click.button() == 0 && isHovered) {
            enabled = !enabled
            return true
        }
        return false
    }

    override val isHovered: Boolean
        get() = isAreaHovered(lastX + width - 30, lastY + getHeight() / 2 - 16, 24, 24)

    companion object {
        val chevron = Identifier.fromNamespaceAndPath("odin", "textures/chevron.png")
    }
}