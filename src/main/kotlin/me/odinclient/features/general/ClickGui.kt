package me.odinclient.features.general

import cc.polyfrost.oneconfig.config.core.OneColor
import me.odinclient.OdinClient.Companion.clickGUI
import me.odinclient.OdinClient.Companion.display
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.NumberSetting
import org.lwjgl.input.Keyboard

object ClickGui: Module(
    "ClickGUI",
    Keyboard.KEY_RSHIFT,
    category = Category.GENERAL,
) {
    val blur: Boolean by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val color: OneColor by ColorSetting("Color", OneColor(50, 150, 220), allowAlpha = false, hidden = false, description = "Color theme in the gui.")

    val panelX = mutableMapOf<Category, NumberSetting>()
    val panelY = mutableMapOf<Category, NumberSetting>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

    const val PANEL_WIDTH = 120
    const val PANEL_HEIGHT = 20

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
            panelX +=  PANEL_WIDTH + 10
        }
    }

    override fun keyBind() {
        this.toggle()
    }

    override fun onEnable() {
        display = clickGUI
        super.onEnable()
        toggle()
    }
}