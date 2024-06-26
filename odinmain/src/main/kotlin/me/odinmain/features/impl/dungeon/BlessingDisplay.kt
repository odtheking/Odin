package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.dungeon.Blessing
import kotlin.math.max

object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current blessings of the dungeon.",
    category = Category.DUNGEON,
) {
    private val power: Boolean by BooleanSetting("Power Blessing", true, description = "Displays the power blessing.")
    private val powerColor: Color by ColorSetting("Power Color", Color.DARK_RED, true, description = "The color of the power blessing.").withDependency { power }
    private val time: Boolean by BooleanSetting("Time Blessing", true, description = "Displays the time blessing.")
    private val timeColor: Color by ColorSetting("Time Color", Color.PURPLE, true, description = "The color of the time blessing.").withDependency { time }
    private val stone: Boolean by BooleanSetting("Stone Blessing", false, description = "Displays the stone blessing.")
    private val stoneColor: Color by ColorSetting("Stone Color", Color.GRAY, true, description = "The color of the stone blessing.").withDependency { stone }
    private val life: Boolean by BooleanSetting("Life Blessing", false, description = "Displays the life blessing.")
    private val lifeColor: Color by ColorSetting("Life Color", Color.RED, true, description = "The color of the life blessing.").withDependency { life }
    private val wisdom: Boolean by BooleanSetting("Wisdom Blessing", false, description = "Displays the wisdom blessing.")
    private val wisdomColor: Color by ColorSetting("Wisdom Color", Color.BLUE, true, description = "The color of the wisdom blessing.").withDependency { wisdom }

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        val blessings = listOf(
            BlessingData(Blessing.POWER, power, powerColor),
            BlessingData(Blessing.TIME, time, timeColor),
            BlessingData(Blessing.STONE, stone, stoneColor),
            BlessingData(Blessing.LIFE, life, lifeColor),
            BlessingData(Blessing.WISDOM, wisdom, wisdomColor)
        )

        val activeBlessings = blessings.filter { a -> a.enabled }
        if (it) {
            activeBlessings.forEachIndexed { index, blessing ->
                mcText("${blessing.type.displayString} §a29§r", 0f, 10f * index, 1, blessing.color, center = false)
            }
        } else {
            activeBlessings.filter { blessing -> blessing.type.current > 0 }.forEachIndexed { index, blessing ->
                mcText("${blessing.type.displayString} §a${blessing.type.current}§r", 0f, 5f + 10 * (index - 1), 1, blessing.color, center = false)
            }
        }
        getMCTextWidth("Power: 29").toFloat() to 10f * max(activeBlessings.count(), 1)
    }

    data class BlessingData(val type: Blessing, val enabled: Boolean, val color: Color)
}