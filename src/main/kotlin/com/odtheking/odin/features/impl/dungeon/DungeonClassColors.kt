package com.odtheking.odin.features.impl.dungeon

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
    val archerColor by ColorSetting("Archer", Colors.MINECRAFT_GOLD, false, desc = "Custom color for the Archer class.")
    val berserkColor by ColorSetting("Berserk", Colors.MINECRAFT_DARK_RED, false, desc = "Custom color for the Berserk class.")
    val healerColor by ColorSetting("Healer", Colors.MINECRAFT_LIGHT_PURPLE, false, desc = "Custom color for the Healer class.")
    val mageColor by ColorSetting("Mage", Colors.MINECRAFT_AQUA, false, desc = "Custom color for the Mage class.")
    val tankColor by ColorSetting("Tank", Colors.MINECRAFT_DARK_GREEN, false, desc = "Custom color for the Tank class.")
    val unknownColor by ColorSetting("Unknown", Colors.WHITE, false, desc = "Custom color for the Unknown class.")

    fun colorFor(clazz: DungeonClass): Color {
        if (!enabled) return clazz.defaultColor
        return when (clazz) {
            DungeonClass.Archer -> archerColor
            DungeonClass.Berserk -> berserkColor
            DungeonClass.Healer -> healerColor
            DungeonClass.Mage -> mageColor
            DungeonClass.Tank -> tankColor
            DungeonClass.Unknown -> unknownColor
        }
    }
}
