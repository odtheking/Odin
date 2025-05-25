package me.odinmain.features.impl.dungeon

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.skyblock.dungeon.Blessing
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.ui.Colors

object BlessingDisplay : Module(
    name = "Blessing Display",
    desc = "Displays the current active blessings of the dungeon."
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
    private val wisdomColor by ColorSetting("Wisdom Color", Colors.MINECRAFT_BLUE, true, desc = "The color of the wisdom blessing.").withDependency { wisdom }

    private data class BlessingData(val type: Blessing, val enabled: () -> Boolean, val color: () -> Color)
    private val blessings = listOf(
        BlessingData(Blessing.POWER, { power }, { powerColor }),
        BlessingData(Blessing.TIME, { time }, { timeColor }),
        BlessingData(Blessing.STONE, { stone }, { stoneColor }),
        BlessingData(Blessing.LIFE, { life }, { lifeColor }),
        BlessingData(Blessing.WISDOM, { wisdom }, { wisdomColor })
    )

    private val hud by HudSetting("Display", 10f, 10f, 1f, false) { example ->
        if (!DungeonUtils.inDungeons) return@HudSetting 0f to 0f
        (0..5).reduce { acc, index ->
            val blessing = blessings[index - 1].takeIf { it.enabled.invoke() } ?: return@reduce acc
            val level = if (example) 19 else if (blessing.type.current > 0) blessing.type.current else return@reduce acc
            RenderUtils.drawText("${blessing.type.displayString} §a$level§r", 0f, 10f * acc, 1f, blessing.color.invoke(), center = false)
            acc + 1
        }.let { getMCTextWidth("Power: 19").toFloat() to 10f * it }
    }
}