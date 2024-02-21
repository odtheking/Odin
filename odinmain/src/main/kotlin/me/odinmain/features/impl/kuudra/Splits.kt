package me.odinmain.features.impl.kuudra

import gg.essential.universal.UChat
import me.odinmain.config.Config
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalTimes
import me.odinmain.features.impl.floor7.p3.TerminalTimes.unaryPlus
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.getTextWidth
import me.odinmain.ui.util.text
import me.odinmain.ui.util.wrappedText
import me.odinmain.utils.compareTo
import me.odinmain.utils.render.Color
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Splits : Module(
    name = "Splits",
    description = "Splits for phases of Kuudra.",
    category = Category.KUUDRA
) {
    private val t1PB = +NumberSetting("T1 PB", 99.0, increment = 0.01, hidden = true)
    private val t2PB = +NumberSetting("T2 PB", 99.0, increment = 0.01, hidden = true)
    private val t3sPB = +NumberSetting("T3 PB", 99.0, increment = 0.01, hidden = true)
    private val t4PB = +NumberSetting("T4 PB", 99.0, increment = 0.01, hidden = true)
    private val t5PB = +NumberSetting("T5 PB", 99.0, increment = 0.01, hidden = true)
    private val sendPB: Boolean by BooleanSetting("Send PB", true, description = "Sends a message when a new PB is achieved")
    private val splitsColor: Color by ColorSetting("Splits Color", Color.CYAN)

    private val lines = listOf(
        "Supplies§f: ",
        "Build§f: ",
        "Fuel/Stun§f: ",
        "Kill§f: ",
        "Total§f: "
    )
    private val hud: HudElement by HudSetting("Splits Display", 10f, 10f, 1f, true) {
        if (it) {
            for (i in 0..4) {
                text(lines[i], 1f, 9f + i * OdinFont.getTextHeight("12", 13f), splitsColor, 12f, shadow = true)
                text("0s", OdinFont.getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) - OdinFont.getTextWidth("0s", 12f), 9f + i * OdinFont.getTextHeight("12", 13f), Color.WHITE, 12f, shadow = true)
            }

            getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) + 2f to 80f
        } else {
            if (LocationUtils.currentArea != "Kuudra") return@HudSetting 0f to 0f
            var y = 0f
            val (times, current) = getSplitTimes()

            for (i in 0..4) {
                var time = times[i]
                time = time / 10 * 10
                text(lines[i], 1f, y, splitsColor, 12f, shadow = true)
                val duration = formatTime(time)
                text(duration, OdinFont.getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) - OdinFont.getTextWidth(duration, 12f), y, Color.WHITE, 12f, shadow = true)
                y += OdinFont.getTextHeight("12", 13f)
            }

            getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) + 2f to 80f
        }
    }


    private val splits = longArrayOf(0L, 0L, 0L, 0L, 0L)

    private fun getSplitTimes(): Pair<List<Long>, Int> {
        if (splits[0] == 0L) return listOf(0L, 0L, 0L, 0L, 0L) to -1
        val latestTime = if (splits.last() == 0L) System.currentTimeMillis() else splits.last()
        val times = mutableListOf(0, 0, 0, 0, latestTime - splits.first())
        var current = splits.size
        for (i in 0 until splits.size - 1) {
            if (splits[i + 1] != 0L) {
                times[i] = splits[i + 1] - splits[i]
            } else {
                current = i
                times[i] = latestTime - splits[i]
                break
            }
        }
        return times to current
    }

    enum class KuudraTiers(val value: NumberSetting<Double>) {
        T1(t1PB),
        T2(t2PB),
        T3s(t3sPB),
        T4(t4PB),
        T5(t5PB)
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (LocationUtils.currentArea != "Kuudra") return
        when (event.message) {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> splits[0] = System.currentTimeMillis()

            "[NPC] Elle: OMG! Great work collecting my supplies!" -> splits[1] = System.currentTimeMillis()

            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> splits[2] = System.currentTimeMillis()

            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> splits[3] = System.currentTimeMillis()

            else -> {
                if (event.message.contains("KUUDRA DOWN!")) {
                    splits[4] = System.currentTimeMillis()
                    val oldPB = KuudraTiers.valueOf("T${LocationUtils.kuudraTier}").value.value
                    val time = (splits[4] - splits[0]) / 1000.0
                    if (time < oldPB) {
                        if(sendPB) modMessage("§fNew best time for §6T${LocationUtils.kuudraTier} Kuudra §fis §a${time}s, §fold best time was §a${oldPB}s")
                        KuudraTiers.valueOf("T${LocationUtils.kuudraTier}").value.value = time.round(2)
                        Config.saveConfig()
                    }
                }
                else if (event.message.contains("DEFEAT")) splits[4] = System.currentTimeMillis()
            }
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        splits.fill(0L)
    }

    private fun formatTime(time: Long): String {
        if (time == 0L) return "0s"
        var remaining = time
        val hours = (remaining / 3600000).toInt().let {
            remaining -= it * 3600000
            if (it > 0) "${it}h " else ""
        }
        val minutes = (remaining / 60000).toInt().let {
            remaining -= it * 60000
            if (it > 0) "${it}m " else ""
        }
        val seconds = (remaining / 1000f).let {
            "%.1fs".format(it)
        }
        return "$hours$minutes$seconds"
    }
}