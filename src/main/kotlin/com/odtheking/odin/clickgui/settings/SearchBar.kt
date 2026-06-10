package com.odtheking.odin.clickgui.settings

import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.TextInputHandler
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

object SearchBar {

    var currentSearch = ""
        private set(value) {
            if (value == field || value.length > 16) return
            field = value
            searchWidth = NVGRenderer.textWidth(value, 20f, NVGRenderer.defaultFont)
        }

    private var placeHolderWidth = NVGRenderer.textWidth("Search here...", 20f, NVGRenderer.defaultFont)
    private var searchWidth = NVGRenderer.textWidth(currentSearch, 20f, NVGRenderer.defaultFont)

    private val textInputHandler = TextInputHandler(
        textProvider = { currentSearch },
        textSetter = { currentSearch = it }
    )

    fun draw(x: Float, y: Float, mouseX: Float, mouseY: Float) {
        NVGRenderer.dropShadow(x, y, 350f, 40f, 10f, 0.75f, 9f)
        NVGRenderer.rect(x, y, 350f, 40f, gray38.rgba, 9f)
        NVGRenderer.hollowRect(x, y, 350f, 40f, 3f, ClickGUIModule.clickGUIColor.rgba, 9f)

        val textY = y + 10f

        if (currentSearch.isEmpty()) NVGRenderer.text("Search here...", x + 175f - placeHolderWidth / 2, textY, 20f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        textInputHandler.x = (x + 175f - searchWidth / 2 - if (currentSearch.isEmpty()) placeHolderWidth / 2 + 2f else 0f).coerceAtLeast(x)
        textInputHandler.y = textY - 1
        textInputHandler.width = 250f
        textInputHandler.height = 22f
        textInputHandler.draw(mouseX, mouseY)
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        return textInputHandler.mouseClicked(mouseX, mouseY, click)
    }

    fun mouseReleased() {
        textInputHandler.mouseReleased()
    }

    fun keyPressed(input: KeyEvent): Boolean {
        return textInputHandler.keyPressed(input)
    }

    fun keyTyped(input: CharacterEvent): Boolean {
        return textInputHandler.keyTyped(input)
    }
}