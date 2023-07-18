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

object ClickGUIModule: Module(
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
        resetPositions()
    }

    fun resetPositions() {
        Category.values().forEach {
            val incr = 10.0 + 260.0 * it.ordinal
            panelX.getOrPut(it) { +NumberSetting(it.name + ",x", default = incr, hidden = true) }.value = incr
            panelY.getOrPut(it) { +NumberSetting(it.name + ",y", default = 10.0, hidden = true) }.value = 10.0
            panelExtended.getOrPut(it) { +BooleanSetting(it.name + ",extended", default = true, hidden = true) }.enabled = true
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