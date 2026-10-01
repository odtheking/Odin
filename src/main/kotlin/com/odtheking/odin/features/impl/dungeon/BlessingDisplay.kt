package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.Blessing
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current active blessings of the dungeon."
) {
    private val power by BooleanSetting("Power Blessing", true, desc = "Displays the power blessing.")
    private val powerColor by ColorSetting("Power Color", Colors.MINECRAFT_DARK_RED, true, desc = "The color of the power blessing.").withDependency { power }
    private val time by BooleanSetting("Time Blessing", true, desc = "Displays the time blessing.")
    private val timeColor by ColorSetting("Time Color", Colors.MINECRAFT_DARK_PURPLE, true, desc = "The color of the time blessing.").withDependency { time }
    private val stone by BooleanSetting("Stone Blessing", false, desc = "Displays the stone blessing.")
    private val stoneColor by ColorSetting("Stone Color", Colors.MINECRAFT_GRAY, true, desc = "The color of the stone blessing.").withDependency { stone }
    private val life by BooleanSetting("Life Blessing", false, desc = "Displays the life blessing.")
    private val lifeColor by ColorSetting("Life Color", Colors.MINECRAFT_RED, true, desc = "The color of the life blessing.").withDependency { life }
    private val wisdom by BooleanSetting("Wisdom Blessing", false, desc = "Displays the wisdom blessing.")
    private val wisdomColor by ColorSetting("Wisdom Color", Colors.MINECRAFT_AQUA, true, desc = "The color of the wisdom blessing.").withDependency { wisdom }

    private data class BlessingData(val type: Blessing, val enabled: () -> Boolean, val color: () -> Color)
    private val blessings = listOf(
        BlessingData(Blessing.POWER, { power }, { powerColor }),
        BlessingData(Blessing.TIME, { time }, { timeColor }),
        BlessingData(Blessing.STONE, { stone }, { stoneColor }),
        BlessingData(Blessing.LIFE, { life }, { lifeColor }),
        BlessingData(Blessing.WISDOM, { wisdom }, { wisdomColor })
    )

    private val hud by HUD(name, "Displays the current active blessings of the dungeon.", false) { example ->
        if (!DungeonUtils.inDungeons && !example) return@HUD 0 to 0
        (0..5).reduce { acc, index ->
            val blessing = blessings[index - 1].takeIf { it.enabled() } ?: return@reduce acc
            val level = if (example) 19 else if (blessing.type.current > 0) blessing.type.current else return@reduce acc
            text("${blessing.type.displayString} §a$level§r", 0, 9 * acc, blessing.color())
            acc + 1
        }.let { getStringWidth("Power: 19") to 1 + 10 * it }
    }
}