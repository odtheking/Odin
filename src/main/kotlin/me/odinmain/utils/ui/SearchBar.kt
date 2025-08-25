package me.odinmain.utils.ui

import me.odinmain.clickgui.ClickGUI.gray38
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.rendering.NVGRenderer

class SearchBar {
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

    fun draw(x: Float, y: Float, width: Float, height: Float, mouseX: Float, mouseY: Float) {
        NVGRenderer.dropShadow(x, y, width, height, 10f, 0.75f, 9f)
        NVGRenderer.rect(x, y, width, height, gray38.rgba, 9f)
        NVGRenderer.hollowRect(x, y, width, height, 3f, ClickGUIModule.clickGUIColor.rgba, 9f)

        val textY = y + 10f

        if (currentSearch.isEmpty()) NVGRenderer.text("Search here...", x + (width / 2) - placeHolderWidth / 2, textY, 20f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        textInputHandler.x = (x + (width / 2) - searchWidth / 2 - if (currentSearch.isEmpty()) placeHolderWidth / 2 + 2f else 0f).coerceAtLeast(x)
        textInputHandler.y = textY - 1
        textInputHandler.width = width - 100 // ?? not sure if these should be handled like this but whatever
        textInputHandler.height = height - 18 // ??
        textInputHandler.draw(mouseX, mouseY)
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean = textInputHandler.mouseClicked(mouseX, mouseY, mouseButton)

    fun mouseReleased() = textInputHandler.mouseReleased()

    fun keyPressed(keyCode: Int): Boolean = textInputHandler.keyPressed(keyCode)

    fun keyTyped(typedChar: Char): Boolean = textInputHandler.keyTyped(typedChar)
}