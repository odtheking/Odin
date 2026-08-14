package com.odtheking.odin.clickgui.widget

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedRectOutlined
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class SearchBarWidget(onSearch: (String) -> Unit) : OdinContainerWidget(0, 0, WIDTH, HEIGHT, Component.literal("Search")) {

    private val input = rowTextField(WIDTH - PADDING * 2, "Search", true).apply {
        setMaxLength(32)
        setResponder(onSearch)
    }

    override fun children(): List<GuiEventListener> = listOf(input)

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? =
        if (isFocused) null else ComponentPath.path(this, ComponentPath.leaf(input))

    fun place(x: Int, y: Int) {
        this.x = x
        this.y = y
        input.x = x + PADDING
        input.y = GuiTheme.textY(y, height)
    }

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.roundedRectOutlined(x, y, x + width, y + height, GuiTheme.surface.rgba, GuiTheme.accent.rgba, 1f, RADIUS)

        if (input.value.isEmpty() && !input.isFocused) graphics.centeredText(mc.font, PLACEHOLDER, x + width / 2, input.y, Colors.MINECRAFT_GRAY.rgba)
        else renderChildren(graphics, mouseX, mouseY)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        setFocused(input)
        input.onClick(event, doubleClick)
    }

    fun selectAll() {
        input.moveCursorToStart(false)
        input.moveCursorToEnd(true)
    }

    companion object {
        const val WIDTH = 180
        const val HEIGHT = 20

        private const val PLACEHOLDER = "Search..."
        private const val PADDING = 8

        private const val RADIUS = HEIGHT / 2f
    }
}