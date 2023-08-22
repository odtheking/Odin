package me.odinclient.features.impl.dungeon

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.events.impl.ReceivePacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.render.gui.nvg.getTextWidth
import me.odinclient.utils.render.gui.nvg.textWithControlCodes
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object BlessingDisplay : Module(
    name = "Blessing Display",
    description = "Displays the current blessings of the dungeon",
    category = Category.DUNGEON
) {
    private val power: Boolean by BooleanSetting("Power Blessing", true)
    private val time: Boolean by BooleanSetting("Time Blessing", true)
    private val stone: Boolean by BooleanSetting("Stone Blessing")
    private val life: Boolean by BooleanSetting("Life Blessing")
    private val wisdom: Boolean by BooleanSetting("Wisdom Blessing")

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, true) {
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
        var current: Int,
        var regex: Regex,
        val displayString: String,
        val enabled: () -> Boolean
    ){
        POWER(0, Regex("Blessing of Power (X{0,3}(IX|IV|V?I{0,3}))"), "§cPower", { power }),
        LIFE(0, Regex("Blessing of Life (X{0,3}(IX|IV|V?I{0,3}))"), "§4Life", { life }),
        WISDOM(0, Regex("Blessing of Wisdom (X{0,3}(IX|IV|V?I{0,3}))"), "§bWisdom", { wisdom }),
        STONE(0, Regex("Blessing of Stone (X{0,3}(IX|IV|V?I{0,3}))"), "§8Stone", { stone }),
        TIME(0, Regex("Blessing of Time V"), "§cT§6i§am§5e", { time });

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

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S47PacketPlayerListHeaderFooter || !DungeonUtils.inDungeons) return
        val footer = event.packet.footer.unformattedText.noControlCodes
        Blessings.entries.forEach { blessing ->
            blessing.regex.find(footer)?.let { match ->
                blessing.current = romanToInt(match.groupValues[1])
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        Blessings.entries.forEach { it.reset() }
    }
}
