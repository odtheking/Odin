package com.odtheking.odin.clickgui.settings

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.widget.OdinContainerWidget
import com.odtheking.odin.clickgui.widget.isOver
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.network.chat.Component

abstract class RenderableSetting<T>(
    override val name: String,
    override var description: String,
    height: Int = GuiTheme.ROW_HEIGHT
) : OdinContainerWidget(0, 0, GuiTheme.ROW_WIDTH, height, Component.literal(name)), Setting<T> {

    override var hidden: Boolean = false

    override var visibilityDependency: (() -> Boolean)? = null

    override val clickButtons: IntArray = LEFT_ONLY

    override fun children(): List<GuiEventListener> = emptyList()

    open fun measure() = Unit

    open fun place() = Unit

    open fun release() = Unit

    final override fun wantsDescription(mouseX: Int, mouseY: Int): Boolean =
        isOver(mouseX, mouseY, x, y, width, GuiTheme.ROW_HEIGHT)

    protected fun drawLabel(graphics: GuiGraphicsExtractor, rowY: Int = y, rowHeight: Int = GuiTheme.ROW_HEIGHT) {
        graphics.text(mc.font, name, x + GuiTheme.PADDING, GuiTheme.textY(rowY, rowHeight), Colors.WHITE.rgba, false)
    }
}