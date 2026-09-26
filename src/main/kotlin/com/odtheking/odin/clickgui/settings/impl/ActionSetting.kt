package com.odtheking.odin.clickgui.settings.impl

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedRectOutlined
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent

class ActionSetting(
    name: String,
    desc: String,
    override val default: () -> Unit = {}
) : RenderableSetting<() -> Unit>(name, desc) {

    override var value: () -> Unit = default

    var action: () -> Unit by this::value

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val left = x + 3
        val top = y + 2
        val right = x + width - 3
        val bottom = y + height - 2

        val hovered = hover
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f
        val scale = 1f - HOVER_SHRINK * hovered

        graphics.pose().pushMatrix()
        graphics.pose().translate(centerX, centerY)
        graphics.pose().scale(scale, scale)
        graphics.pose().translate(-centerX, -centerY)
        graphics.roundedRectOutlined(left, top, right, bottom, GuiTheme.surface.hoverTint(hovered), GuiTheme.accent.rgba, 1f, GuiTheme.RADIUS)
        graphics.pose().popMatrix()

        graphics.centeredText(mc.font, name, x + width / 2, GuiTheme.textY(y, height), Colors.WHITE.rgba)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) = action()

    private companion object {
        const val HOVER_SHRINK = 0.06f
    }
}