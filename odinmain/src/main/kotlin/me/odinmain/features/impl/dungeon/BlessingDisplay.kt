package me.odinmain.features.impl.dungeon

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.Utils.noControlCodes
import me.odinmain.utils.render.gui.nvg.getTextWidth
import me.odinmain.utils.render.gui.nvg.textWithControlCodes
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current blessings of the dungeon.",
    category = Category.DUNGEON,
) {
    private val power: Boolean by BooleanSetting("Power Blessing", true)
    private val time: Boolean by BooleanSetting("Time Blessing", true)
    private val stone: Boolean by BooleanSetting("Stone Blessing")
    private val life: Boolean by BooleanSetting("Life Blessing")
    private val wisdom: Boolean by BooleanSetting("Wisdom Blessing")

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            textWithControlCodes("§cPower §a29", 1f, 9f, 16f, Fonts.REGULAR)
            textWithControlCodes("§cT§6i§am§5e §a5", 1f, 26f, 16f, Fonts.REGULAR)
            max(getTextWidth("Power 29", 16f, Fonts.REGULAR), getTextWidth("Time 5", 16f, Fonts.REGULAR)) + 2f to 33f
        } else {
            var width = 0f
            var height = 0f
            Blessings.entries.forEach { blessing ->
                if (blessing.current == 0 || !blessing.enabled.invoke()) return@forEach
                textWithControlCodes("${blessing.displayString} §a${blessing.current}", 1f, 9f + height, 16f, Fonts.REGULAR)
                width = max(width, getTextWidth("${blessing.displayString} §a${blessing.current}".noControlCodes, 16f, Fonts.REGULAR))
                height += 17f
            }
            width to height
        }
    }

    private enum class Blessings(
        var regex: Regex,
        val displayString: String,
        val enabled: () -> Boolean,
        var current: Int = 0
    ) {
        POWER(Regex("Blessing of Power (X{0,3}(IX|IV|V?I{0,3}))"), "§cPower", { power }),
        LIFE(Regex("Blessing of Life (X{0,3}(IX|IV|V?I{0,3}))"), "§4Life", { life }),
        WISDOM(Regex("Blessing of Wisdom (X{0,3}(IX|IV|V?I{0,3}))"), "§bWisdom", { wisdom }),
        STONE(Regex("Blessing of Stone (X{0,3}(IX|IV|V?I{0,3}))"), "§8Stone", { stone }),
        TIME(Regex("Blessing of Time (V)"), "§cT§6i§am§5e", { time });

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