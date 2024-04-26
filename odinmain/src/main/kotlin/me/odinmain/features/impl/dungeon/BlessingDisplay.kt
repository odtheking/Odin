package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.*
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter
import kotlin.math.max

@AlwaysActive
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
        val activeBlessings = Blessings.entries.filter { a -> a.enabled }
        if (it) {
            activeBlessings.forEachIndexed { index, blessing ->
                mcText("${blessing.displayString} §a29§r", 0f, 10f * index, 1, blessing.color, center = false)
            }
        } else {
            activeBlessings.filter { blessing -> blessing.current > 0 }.forEachIndexed { index, blessing ->
                mcText("${blessing.displayString} §a${blessing.current}§r", 0f, 5f + 10 * (index - 1), 1, blessing.color, center = false)
            }
        }
        getMCTextWidth("Power: 29").toFloat() to 10f * max(activeBlessings.count(), 1)
    }


    enum class Blessings(
        var regex: Regex,
        val displayString: String,
        var color: Color,
        var enabled: Boolean,
        var current: Int = 0
    ) {
        POWER(Regex("Blessing of Power (X{0,3}(IX|IV|V?I{0,3}))"), "Power", powerColor, power),
        LIFE(Regex("Blessing of Life (X{0,3}(IX|IV|V?I{0,3}))"), "Life", lifeColor, life),
        WISDOM(Regex("Blessing of Wisdom (X{0,3}(IX|IV|V?I{0,3}))"), "Wisdom", wisdomColor, wisdom),
        STONE(Regex("Blessing of Stone (X{0,3}(IX|IV|V?I{0,3}))"), "Stone", stoneColor, stone),
        TIME(Regex("Blessing of Time (V)"), "Time", timeColor, time);

        fun reset() {
            current = 0
        }
    }

    private val romanMap = mapOf('I' to 1, 'V' to 5, 'X' to 10)
    private fun romanToInt(s: String): Int {
        var result = 0
        for (i in 0 until s.length - 1) {
            val current = romanMap[s[i]] ?: 0
            val next = romanMap[s[i + 1]] ?: 0
            result += if (current < next) -current else current
        }
        return result + (romanMap[s.last()] ?: 0)
    }

    init {
        onPacket(S47PacketPlayerListHeaderFooter::class.java) {
            Blessings.entries.forEach { blessing ->
                blessing.regex.find(it.footer.unformattedText.noControlCodes)?.let { match ->
                    blessing.current = romanToInt(match.groupValues[1])
                }
            }
            Blessings.TIME.color = timeColor
            Blessings.POWER.color = powerColor
            Blessings.STONE.color = stoneColor
            Blessings.LIFE.color = lifeColor
            Blessings.WISDOM.color = wisdomColor

            Blessings.TIME.enabled = time
            Blessings.POWER.enabled = power
            Blessings.STONE.enabled = stone
            Blessings.LIFE.enabled = life
            Blessings.WISDOM.enabled = wisdom
        }

        onWorldLoad {
            Blessings.entries.forEach { it.reset() }
        }
    }
}