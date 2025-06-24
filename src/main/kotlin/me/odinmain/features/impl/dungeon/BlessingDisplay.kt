package me.odinmain.features.impl.dungeon

import com.github.stivais.aurora.color.Color
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.skyblock.dungeon.Blessing
import me.odinmain.utils.ui.Colors

object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current blessings of the dungeon."
) {

    private var power by BooleanSetting("Power Blessing", true, description = "Displays the power blessing.")
    private val powerColor by ColorSetting("Power Color", Colors.MINECRAFT_DARK_RED, allowAlpha = false, description = "The color of the power blessing.").withDependency { power }
    private val time by BooleanSetting("Time Blessing", true, description = "Displays the time blessing.")
    private val timeColor by ColorSetting("Time Color", Colors.MINECRAFT_DARK_PURPLE, allowAlpha = false, description = "The color of the time blessing.").withDependency { time }
    private val stone by BooleanSetting("Stone Blessing", false, description = "Displays the stone blessing.")
    private val stoneColor by ColorSetting("Stone Color", Colors.MINECRAFT_GRAY, allowAlpha = false, description = "The color of the stone blessing.").withDependency { stone }
    private val life by BooleanSetting("Life Blessing", false, description = "Displays the life blessing.")
    private val lifeColor by ColorSetting("Life Color", Color.RED, allowAlpha = false, description = "The color of the life blessing.").withDependency { life }
    private val wisdom by BooleanSetting("Wisdom Blessing", false, description = "Displays the wisdom blessing.")
    private val wisdomColor by ColorSetting("Wisdom Color", Colors.MINECRAFT_BLUE, allowAlpha = false, description = "The color of the wisdom blessing.").withDependency { wisdom }

    private val blessings = arrayListOf(
        BlessingData(Blessing.POWER, { power }, { powerColor }),
        BlessingData(Blessing.TIME, { time }, { timeColor }),
        BlessingData(Blessing.STONE, { stone }, { stoneColor }),
        BlessingData(Blessing.LIFE, { life }, { lifeColor }),
        BlessingData(Blessing.WISDOM, { wisdom }, { wisdomColor })
    )

    private data class BlessingData(val type: Blessing, val enabled: () -> Boolean, val color: () -> Color)
}