package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass

object DungeonClassColors : Module(
    name = "Dungeon Class Colors",
    description = "When enabled, overrides the default dungeon class colors with your custom colors.",
    key = null
) {
    private val archerSetting = +ColorSetting("Archer", Colors.MINECRAFT_GOLD, false, desc = "Custom color for the Archer class.")
    private val berserkSetting = +ColorSetting("Berserk", Colors.MINECRAFT_DARK_RED, false, desc = "Custom color for the Berserk class.")
    private val healerSetting = +ColorSetting("Healer", Colors.MINECRAFT_LIGHT_PURPLE, false, desc = "Custom color for the Healer class.")
    private val mageSetting = +ColorSetting("Mage", Colors.MINECRAFT_AQUA, false, desc = "Custom color for the Mage class.")
    private val tankSetting = +ColorSetting("Tank", Colors.MINECRAFT_DARK_GREEN, false, desc = "Custom color for the Tank class.")
    private val unknownSetting = +ColorSetting("Unknown", Colors.WHITE, false, desc = "Custom color for the Unknown class.")

    private val colorSettings get() = listOf(archerSetting, berserkSetting, healerSetting, mageSetting, tankSetting, unknownSetting)

    init {
        +ActionSetting("Reset to Defaults", desc = "Resets all class colors back to their original default values.") {
            colorSettings.forEach { it.value = it.default.copy() }
        }
    }

    fun colorFor(clazz: DungeonClass): Color {
        if (!enabled) return clazz.defaultColor
        return when (clazz) {
            DungeonClass.Archer -> archerSetting.value
            DungeonClass.Berserk -> berserkSetting.value
            DungeonClass.Healer -> healerSetting.value
            DungeonClass.Mage -> mageSetting.value
            DungeonClass.Tank -> tankSetting.value
            DungeonClass.Unknown -> unknownSetting.value
        }
    }
}
