package me.odinmain.features.impl.dungeon

import com.github.stivais.ui.color.Color
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.skyblock.dungeon.Blessing

object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current blessings of the dungeon.",
    category = Category.DUNGEON,
) {
    private val power: Boolean by BooleanSetting("Power Blessing", true, description = "Displays the power blessing.")
    private val powerColor: Color by ColorSetting("Power Color", Color.MINECRAFT_DARK_RED, true, description = "The color of the power blessing.").withDependency { power }
    private val time: Boolean by BooleanSetting("Time Blessing", true, description = "Displays the time blessing.")
    private val timeColor: Color by ColorSetting("Time Color", Color.MINECRAFT_DARK_PURPLE, true, description = "The color of the time blessing.").withDependency { time }
    private val stone: Boolean by BooleanSetting("Stone Blessing", false, description = "Displays the stone blessing.")
    private val stoneColor: Color by ColorSetting("Stone Color", Color.MINECRAFT_GRAY, true, description = "The color of the stone blessing.").withDependency { stone }
    private val life: Boolean by BooleanSetting("Life Blessing", false, description = "Displays the life blessing.")
    private val lifeColor: Color by ColorSetting("Life Color", Color.MINECRAFT_RED, true, description = "The color of the life blessing.").withDependency { life }
    private val wisdom: Boolean by BooleanSetting("Wisdom Blessing", false, description = "Displays the wisdom blessing.")
    private val wisdomColor: Color by ColorSetting("Wisdom Color", Color.MINECRAFT_BLUE, true, description = "The color of the wisdom blessing.").withDependency { wisdom }

    private data class BlessingData(val type: Blessing, val enabled: () -> Boolean, val color: () -> Color)
    private val blessings = listOf(
        BlessingData(Blessing.POWER, { power }, { powerColor }),
        BlessingData(Blessing.TIME, { time }, { timeColor }),
        BlessingData(Blessing.STONE, { stone }, { stoneColor }),
        BlessingData(Blessing.LIFE, { life }, { lifeColor }),
        BlessingData(Blessing.WISDOM, { wisdom }, { wisdomColor })
    )

    /*private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) { example ->
        return@HudSetting blessings.filterIndexed { index, blessing ->
            if (!blessing.enabled.invoke()) return@filterIndexed false
            val level = if (example) 19 else blessing.type.current
            if (level <= 0) return@HudSetting 0f to 0f
            mcText("${blessing.type.displayString} §a29§r", 0f, 10f * index, 1, blessing.color.invoke(), center = false)
            true
        }.let { getMCTextWidth("Power: 19").toFloat() to 10f * it.size.coerceAtLeast(1) }
    }*/
}