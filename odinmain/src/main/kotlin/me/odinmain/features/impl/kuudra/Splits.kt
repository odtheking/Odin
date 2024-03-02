package me.odinmain.features.impl.kuudra

import me.odinmain.config.Config
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.getTextWidth
import me.odinmain.ui.util.text
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Splits : Module(
    name = "Splits",
    description = "Splits for phases of Kuudra.",
    category = Category.KUUDRA
) {
    private val t1PB = +NumberSetting("T1 PB", 999.0, increment = 0.01, hidden = true)
    private val t2PB = +NumberSetting("T2 PB", 999.0, increment = 0.01, hidden = true)
    private val t3PB = +NumberSetting("T3 PB", 999.0, increment = 0.01, hidden = true)
    private val t4PB = +NumberSetting("T4 PB", 999.0, increment = 0.01, hidden = true)
    private val t5PB = +NumberSetting("T5 PB", 999.0, increment = 0.01, hidden = true)
    private val t5SupplyPB = +NumberSetting("T5 Supply PB", 999.0, increment = 0.01, hidden = true)
    private val t5BuildPB = +NumberSetting("T5 Build PB", 999.0, increment = 0.01, hidden = true)
    private val t5StunPB = +NumberSetting("T5 Stun PB", 999.0, increment = 0.01, hidden = true)
    private val t5KillPB = +NumberSetting("T5 Kill PB", 999.0, increment = 0.01, hidden = true)
    private val sendPB: Boolean by BooleanSetting("Send PB", true, description = "Sends a message when a new PB is achieved")
    private val sendSupplyTime: Boolean by BooleanSetting("Send Supply Time", true, description = "Sends a message when a supply is collected")
    private val splitsColor: Color by ColorSetting("Splits Color", Color.CYAN)
    val reset: () -> Unit by ActionSetting("Send PBs", description = "Sends your current PBs.") {
           modMessage(
               """§8List of PBs:
        §fT1: §a${t1PB.value.round(2)}s
        §fT2: §a${t2PB.value.round(2)}s 
        §fT3: §a${t3PB.value.round(2)}s
        §fT4: §a${t4PB.value.round(2)}s
            """.trimMargin())
           modMessage(
               """§8List of T5 PBs:
        §fSupplies: §a${t5SupplyPB.value.round(2)}s
        §fBuild: §a${t5BuildPB.value.round(2)}s
        §fStun: §a${t5StunPB.value.round(2)}s
        §fKill: §a${t5KillPB.value.round(2)}s
        §fTotal: §a${t5PB.value.round(2)}s
            """.trimMargin())
    }


    private val lines = listOf(
        "Supplies§f: ",
        "Build§f: ",
        "Fuel/Stun§f: ",
        "Kill§f: ",
        "Total§f: "
    )
    private val hud: HudElement by HudSetting("Splits Display HUD", 10f, 10f, 1f, true) {
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

    enum class KuudraTiers(
        val pbTime: NumberSetting<Double>,
        val tierName: String
    ) {
        T1(t1PB, "T1"),
        T2(t2PB, "T2"),
        T3s(t3PB, "T3s"),
        T4(t4PB, "T4"),
        T5(t5PB, "T5")
    }

    enum class t5PBs(
        val pbTime: NumberSetting<Double>,
        val splitName: String
    ) {
        Supplies(t5SupplyPB, "Supplies"),
        Build(t5BuildPB, "Build"),
        Stun(t5StunPB, "Stun"),
        Kill(t5KillPB, "Kill")
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        when (event.message) {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> {
                splits[0] = System.currentTimeMillis()
            }

            "[NPC] Elle: OMG! Great work collecting my supplies!" -> {
                splits[1] = System.currentTimeMillis()
                if (LocationUtils.kuudraTier != 5) return

                val oldPB = t5PBs.entries.find { it.splitName == "Supplies" }?.pbTime?.value ?: 999.0
                val timeP1 = splits[1] - splits[0]
                modMessage("§6Supplies Took§7: §a${formatTime(timeP1)}")
                if (timeP1 / 1000 < 1) return
                if (timeP1 / 1000 < oldPB) {
                    if (sendPB) modMessage("New best time for §6T5 Supplies §fis §a${formatTime(timeP1)}, §fold best time was §a${oldPB}s")
                    t5PBs.entries.find { it.splitName == "Supplies" }?.pbTime?.value = timeP1 / 1000.0
                    Config.saveConfig()
                }
            }

            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> {
                splits[2] = System.currentTimeMillis()
                if (LocationUtils.kuudraTier != 5) return

                val oldPB = t5PBs.entries.find { it.splitName == "Build" }?.pbTime?.value ?: 999.0
                val timeP2 = splits[2] - splits[1]
                modMessage("§6Build Took§7: §a${formatTime(timeP2)}")
                if (timeP2 / 1000 < 1) return
                if (timeP2 / 1000 < oldPB){
                    if (sendPB) modMessage("New best time for §6T5 Build §fis §a${formatTime(timeP2)}, §fold best time was §a${oldPB}s")
                    t5PBs.entries.find { it.splitName == "Build" }?.pbTime?.value = timeP2 / 1000.0
                    Config.saveConfig()
                }
            }

            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> {
                splits[3] = System.currentTimeMillis()
                if (LocationUtils.kuudraTier != 5) return

                val oldPB = t5PBs.entries.find { it.splitName == "Stun" }?.pbTime?.value ?: 999.0
                val timeP3 = splits[3] - splits[2]
                modMessage("§6Fuel/Stun Took§7: §a${formatTime(timeP3)}")
                if (timeP3 / 1000 < 1) return
                if (timeP3 / 1000 < oldPB){
                    if (sendPB) modMessage("New best time for §6T5 Stun §fis §a${formatTime(timeP3)}, §fold best time was §a${oldPB}s")
                    t5PBs.entries.find { it.splitName == "Stun" }?.pbTime?.value = timeP3 / 1000.0
                    Config.saveConfig()
                }
            }

            else -> {
                if (event.message.contains("KUUDRA DOWN!")) {
                    if (LocationUtils.kuudraTier == 5) {
                        val oldKillPB = t5PBs.entries.find { it.splitName == "Kill" }?.pbTime?.value ?: 999.0
                        val timeP4 = System.currentTimeMillis() - splits[3]
                        modMessage("§6Kill Took§7: §a${formatTime(timeP4)}")
                        if (timeP4 / 1000 < 1) return
                        if (timeP4 / 1000 < oldKillPB) {
                            if (sendPB) modMessage("New best time for §6T5 Kill §fis §a${formatTime(timeP4)}, §fold best time was §a${oldKillPB}s")
                            t5PBs.entries.find { it.splitName == "Kill" }?.pbTime?.value = timeP4 / 1000.0
                            Config.saveConfig()
                        }
                    }

                    splits[4] = System.currentTimeMillis()
                    val oldPB = KuudraTiers.entries.find { it.tierName == "T${LocationUtils.kuudraTier}" }?.pbTime?.value ?: 999.0
                    val (times, _) = getSplitTimes()
                    for (i in 0..4) {
                        val duration = formatTime(times[i])
                        modMessage("§8${lines[i]}§7$duration")
                    }
                    val totalTime = times[4] / 1000.0

                    if (totalTime < 1) return
                    if (totalTime < oldPB) {
                        if(sendPB) modMessage("§fNew best time for §6T${LocationUtils.kuudraTier} Kuudra §fis §a${totalTime}s, §fold best time was §a${oldPB}s")
                        KuudraTiers.entries.getSafe(LocationUtils.kuudraTier)?.pbTime?.value = totalTime.round(2)
                        Config.saveConfig()
                    }
                }
                else if (event.message.contains("DEFEAT")) splits[4] = System.currentTimeMillis()
            }
        }
    }
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S02PacketChat)  return
        val message = event.packet.chatComponent.unformattedText.noControlCodes
        if (message.matches(Regex("(\\[.+])? (\\w+) recovered one of Elle's supplies! \\((\\d/\\d)\\)"))) {
            if (!sendSupplyTime) return
            val matchResult = Regex("(\\[.+])? (\\w+) recovered one of Elle's supplies! \\((\\d/\\d)\\)").find(message) ?: return
            event.isCanceled = true
            modMessage("§6${matchResult.groupValues[2]}§a took ${formatTime((System.currentTimeMillis() - splits[0]))} to recover supply §8(${matchResult.groupValues[3]})!", false)
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
            "%.2fs".format(it)
        }
        return "$hours$minutes$seconds"
    }
}