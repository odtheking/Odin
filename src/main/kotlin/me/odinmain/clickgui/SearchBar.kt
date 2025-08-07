package me.odinmain.clickgui

import me.odinmain.clickgui.ClickGUI.gray38
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.TextInputHandler
import me.odinmain.utils.ui.rendering.NVGRenderer

object SearchBar {

    var currentSearch = ""
        private set (value) {
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

    fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        return textInputHandler.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun mouseReleased() {
        textInputHandler.mouseReleased()
    }

    fun keyPressed(keyCode: Int): Boolean {
        return textInputHandler.keyPressed(keyCode)
    }

    fun keyTyped(typedChar: Char): Boolean {
        return textInputHandler.keyTyped(typedChar)
    }
}