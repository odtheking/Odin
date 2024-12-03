package me.odinmain.features.impl.dungeon

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.dsl.at
import me.odinmain.features.Module
import me.odinmain.features.huds.HUD.Companion.needs
import me.odinmain.features.huds.HUD.Companion.preview
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.skyblock.dungeon.Blessing
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.buildText

object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current blessings of the dungeon."
) {
    private val power by BooleanSetting("Power Blessing", true, description = "Displays the power blessing.")
    private val time by BooleanSetting("Time Blessing", true, description = "Displays the time blessing.")
    private val stone by BooleanSetting("Stone Blessing", false, description = "Displays the stone blessing.")
    private val life by BooleanSetting("Life Blessing", false, description = "Displays the life blessing.")
    private val wisdom by BooleanSetting("Wisdom Blessing", false, description = "Displays the wisdom blessing.")

    private val wisdomColor by ColorSetting("Wisdom Color", Colors.MINECRAFT_BLUE, true, description = "The color of the wisdom blessing.").withDependency { wisdom }
    private val powerColor by ColorSetting("Power Color", Colors.MINECRAFT_DARK_RED, true, description = "The color of the power blessing.").withDependency { power }
    private val timeColor by ColorSetting("Time Color", Colors.MINECRAFT_DARK_PURPLE, true, description = "The color of the time blessing.").withDependency { time }
    private val stoneColor by ColorSetting("Stone Color", Colors.MINECRAFT_GRAY, true, description = "The color of the stone blessing.").withDependency { stone }
    private val lifeColor by ColorSetting("Life Color", Color.RED, true, description = "The color of the life blessing.").withDependency { life }

    private data class BlessingData(val type: Blessing, val enabled: () -> Boolean, val color: () -> Color)
    private val blessings = listOf(
        BlessingData(Blessing.POWER, { power }, { powerColor }),
        BlessingData(Blessing.TIME, { time }, { timeColor }),
        BlessingData(Blessing.STONE, { stone }, { stoneColor }),
        BlessingData(Blessing.LIFE, { life }, { lifeColor }),
        BlessingData(Blessing.WISDOM, { wisdom }, { wisdomColor })
    )

    private val HUD by TextHUD("Blessings HUD") { color, font, shadow ->
        needs { DungeonUtils.inDungeons && blessings.none { it.enabled.invoke()} }
        column(at()) {
            (0..5).reduce { acc, index ->
                val blessing = blessings[index - 1].takeIf { it.enabled.invoke() } ?: return@reduce acc
                val level = if (preview) 19 else if (blessing.type.current > 0) blessing.type.current else return@reduce acc
                buildText(
                    string = blessing.type.displayString,
                    supplier = { level },
                    font, blessing.color.invoke(), color, shadow
                )
                acc + 1
            }
        }
    }.setting(description = "Displays the current active blessings in the dungeon.")
}