package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.*
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

@AlwaysActive
object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current blessings of the dungeon.",
    category = Category.DUNGEON,
) {
    private val power: Boolean by BooleanSetting("Power Blessing", true, description = "Displays the power blessing.")
    private val powerColor: Color by ColorSetting("Power Color", Color.DARK_RED, description = "The color of the power blessing.").withDependency { power }
    private val time: Boolean by BooleanSetting("Time Blessing", true, description = "Displays the time blessing.")
    private val timeColor: Color by ColorSetting("Time Color", Color.PURPLE, description = "The color of the time blessing.").withDependency { time }
    private val stone: Boolean by BooleanSetting("Stone Blessing", false, description = "Displays the stone blessing.")
    private val stoneColor: Color by ColorSetting("Stone Color", Color.GRAY, description = "The color of the stone blessing.").withDependency { stone }
    private val life: Boolean by BooleanSetting("Life Blessing", false, description = "Displays the life blessing.")
    private val lifeColor: Color by ColorSetting("Life Color", Color.RED, description = "The color of the life blessing.").withDependency { life }
    private val wisdom: Boolean by BooleanSetting("Wisdom Blessing", false, description = "Displays the wisdom blessing.")
    private val wisdomColor: Color by ColorSetting("Wisdom Color", Color.BLUE, description = "The color of the wisdom blessing.").withDependency { wisdom }

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            textWithControlCodes("Power: §a29", 1f, 9f, powerColor, 16f, Fonts.REGULAR, TextAlign.Left, TextPos.Middle, true)
            textWithControlCodes("Time: §a5", 1f, 26f, timeColor,16f, Fonts.REGULAR, TextAlign.Left, TextPos.Middle, false)
            max(getTextWidth("Power: 29", 16f, Fonts.REGULAR), getTextWidth("Time 5", 16f, Fonts.REGULAR)) + 2f to 33f
        } else {
            var width = 0f
            var height = 0f
            Blessings.entries.forEach { blessing ->
                if (blessing.current == 0 || !blessing.enabled.invoke()) return@forEach
                textWithControlCodes("${blessing.displayString} §a${blessing.current}", 1f, 9f + height, blessing.color,16f, Fonts.REGULAR, TextAlign.Left, TextPos.Middle, true)
                width = max(width, getTextWidth("${blessing.displayString} §a${blessing.current}".noControlCodes, 16f, Fonts.REGULAR))
                height += 17f
            }
            width to height
        }
    }

    enum class Blessings(
        var regex: Regex,
        val displayString: String,
        var color: Color,
        val enabled: () -> Boolean,
        var current: Int = 0
    ) {
        POWER(Regex("Blessing of Power (X{0,3}(IX|IV|V?I{0,3}))"), "Power", powerColor, { power }),
        LIFE(Regex("Blessing of Life (X{0,3}(IX|IV|V?I{0,3}))"), "Life", lifeColor, { life }),
        WISDOM(Regex("Blessing of Wisdom (X{0,3}(IX|IV|V?I{0,3}))"), "Wisdom", wisdomColor,{ wisdom }),
        STONE(Regex("Blessing of Stone (X{0,3}(IX|IV|V?I{0,3}))"), "Stone", stoneColor, { stone }),
        TIME(Regex("Blessing of Time (V)"), "Time", timeColor, { time });

        fun reset() {
            current = 0
        }
    }

    private val romanMap = hashMapOf('I' to 1, 'V' to 5, 'X' to 10)
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
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        Blessings.entries.forEach { it.reset() }
    }
}