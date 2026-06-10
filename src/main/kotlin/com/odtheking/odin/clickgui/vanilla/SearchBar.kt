package com.odtheking.odin.clickgui.vanilla

import com.odtheking.odin.clickgui.settings.ClickGUI
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.roundedOutline
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

object SearchBar {

    var currentSearch = ""
        private set

    private val searchInput by lazy {
        VanillaTextInput(16, 32, { currentSearch }, { currentSearch = it })
    }

    fun draw(context: GuiGraphics) {
        val x = context.guiWidth() / 2 - 175
        val y = context.guiHeight() - 75

        context.roundedFill(x, y, x + 350, y + 40, ClickGUI.gray38.rgba, 9)
        context.roundedOutline(x, y, x + 350, y + 40, ClickGUIModule.clickGUIColor.rgba, 2f, 9)

        searchInput.x = x + 8
        searchInput.y = y + 11
        searchInput.width = 334
        searchInput.height = 18
        searchInput.placeholder = "Search here..."
        searchInput.draw(context)
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        return searchInput.mouseClicked(mouseX, mouseY, click)
    }

    fun mouseReleased() {
        searchInput.mouseReleased()
    }

    fun keyPressed(input: KeyEvent): Boolean {
        return searchInput.keyPressed(input)
    }

    fun keyTyped(input: CharacterEvent): Boolean {
        return searchInput.keyTyped(input)
    }
}