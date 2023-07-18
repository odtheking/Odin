package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.display
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.clickgui.ClickGUI
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGui: Module(
    "ClickGUI",
    Keyboard.KEY_RSHIFT,
    category = Category.GENERAL,
) {
    val blur: Boolean by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val color: Color by ColorSetting("First Color", Color(50, 150, 220), allowAlpha = false, hidden = false, description = "Color theme in the gui.")
    val secondColor: Color by ColorSetting("Second Color", Color(70, 30, 220), allowAlpha = false, hidden = false, description = "Second color theme in the gui.")

    val panelX = mutableMapOf<Category, NumberSetting>()
    val panelY = mutableMapOf<Category, NumberSetting>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

    init {
        // The Panels
        // this will set the default click gui panel settings. These will be overwritten by the config once it is loaded
        resetPositions()

        for(category in Category.values()) {
            addSettings(
                panelX[category]!!,
                panelY[category]!!,
                panelExtended[category]!!
            )
        }
    }


    fun resetPositions() {
        var panelX = 10.0

        for (category in Category.values()) {
            this.panelX.getOrPut(category) { NumberSetting(category.name + ",x", default = panelX, hidden = true) }.value = panelX
            panelY.getOrPut(category) { NumberSetting(category.name + ",y", default = 10.0, hidden = true) }.value = 10.0
            panelExtended.getOrPut(category) { BooleanSetting(category.name + ",extended", default = true, hidden = true) }.enabled = true
            panelX += 260f
        }
    }

    override fun keyBind() {
        this.toggle()
    }

    override fun onEnable() {
        display = ClickGUI
        super.onEnable()
        toggle()
    }
}