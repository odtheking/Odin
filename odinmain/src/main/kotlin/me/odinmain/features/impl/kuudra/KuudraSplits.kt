package me.odinmain.features.impl.kuudra

import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.formatTime
import me.odinmain.utils.getSafe
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextHeight
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.modMessage

object KuudraSplits : Module(
    name = "Kuudra Splits",
    description = "Splits for phases of Kuudra.",
    category = Category.KUUDRA
) {
    private val t1PB = +NumberSetting("T1 PB", 999.0, increment = 0.001, hidden = true)
    private val t2PB = +NumberSetting("T2 PB", 999.0, increment = 0.001, hidden = true)
    private val t3PB = +NumberSetting("T3 PB", 999.0, increment = 0.001, hidden = true)
    private val t4PB = +NumberSetting("T4 PB", 999.0, increment = 0.001, hidden = true)
    private val t5PB = +NumberSetting("T5 PB", 999.0, increment = 0.001, hidden = true)
    private val t5SupplyPB = +NumberSetting("T5 Supply PB", 999.0, increment = 0.001, hidden = true)
    private val t5BuildPB = +NumberSetting("T5 Build PB", 999.0, increment = 0.001, hidden = true)
    private val t5StunPB = +NumberSetting("T5 Stun PB", 999.0, increment = 0.001, hidden = true)
    private val t5KillPB = +NumberSetting("T5 Kill PB", 999.0, increment = 0.001, hidden = true)
    private val sendPB: Boolean by BooleanSetting("Send PB", true, description = "Sends a message when a new PB is achieved")
    private val sendSupplyTime: Boolean by BooleanSetting("Send Supply Time", true, description = "Sends a message when a supply is collected")
    private val splitsLine1: Color by ColorSetting("Splits Line 1", Color.GREEN, description = "Color of the first line of the splits display")
    private val splitsLine2: Color by ColorSetting("Splits Line 2", Color.ORANGE, description = "Color of the second line of the splits display")
    private val splitsLine3: Color by ColorSetting("Splits Line 3", Color.CYAN, description = "Color of the third line of the splits display")
    private val splitsLine4: Color by ColorSetting("Splits Line 4", Color.PURPLE, description = "Color of the fourth line of the splits display")
    private val splitsLine5: Color by ColorSetting("Splits Line 5", Color.RED, description = "Color of the fifth line of the splits display")
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
                val lineColor = when (i) {
                    0 -> splitsLine1
                    1 -> splitsLine2
                    2 -> splitsLine3
                    3 -> splitsLine4
                    4 -> splitsLine5
                    else -> Color.WHITE
                }
                text(lines[i], 1f, 9f + i * getTextHeight("12", 13f), lineColor, 12f, shadow = true)
                text("0s", getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) - getTextWidth("0s", 12f), 9f + i * getTextHeight("12", 13f), Color.WHITE, 12f, shadow = true)
            }

            getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) + 2f to 80f
        } else {
            if (LocationUtils.currentArea != Island.Kuudra) return@HudSetting 0f to 0f
            val (times, current) = getSplitTimes()

            for (i in 0..4) {
                var time = times[i]
                time = time / 10 * 10
                val lineColor = when (i) {
                    0 -> splitsLine1
                    1 -> splitsLine2
                    2 -> splitsLine3
                    3 -> splitsLine4
                    4 -> splitsLine5
                    else -> Color.WHITE
                }
                text(lines[i], 1f, 9f + i * getTextHeight("12", 13f), lineColor, 12f, shadow = true)

                val duration = formatTime(time)
                text(duration, getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) - getTextWidth(duration, 12f), 9f + i * getTextHeight("12", 13f), Color.WHITE, 12f, shadow = true)
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

    enum class T5PBs(
        val pbTime: NumberSetting<Double>,
        val splitName: String
    ) {
        Supplies(t5SupplyPB, "Supplies"),
        Build(t5BuildPB, "Build"),
        Stun(t5StunPB, "Stun"),
        Kill(t5KillPB, "Kill")
    }

    init{
        onMessage("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!", false) {
            splits[0] = System.currentTimeMillis()
        }

        onMessage("[NPC] Elle: OMG! Great work collecting my supplies!", false) {
            splits[1] = System.currentTimeMillis()
            if (LocationUtils.kuudraTier != 5) return@onMessage

            val oldPB = T5PBs.entries.find { it.splitName == "Supplies" }?.pbTime?.value ?: 999.0
            val timeP1 = (splits[1] - splits[0]) / 1000.0
            modMessage("§6Supplies Took§7: §a$timeP1")

            if (timeP1 < oldPB && timeP1 > 1L) {
                if (sendPB) modMessage("New best time for §6T5 Supplies §fis §a$timeP1, §fold best time was §a${oldPB}s")
                T5PBs.entries.find { it.splitName == "Supplies" }?.pbTime?.value = timeP1
                Config.save()
            }
        }

        onMessage("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!", false) {
            splits[2] = System.currentTimeMillis()
            if (LocationUtils.kuudraTier != 5) return@onMessage

            val oldPB = T5PBs.entries.find { it.splitName == "Build" }?.pbTime?.value ?: 999.0
            val timeP2 = (splits[2] - splits[1]) / 1000.0
            modMessage("§6Build Took§7: §a$timeP2")
            if (timeP2 < oldPB && timeP2 > 1L){
                if (sendPB) modMessage("New best time for §6T5 Build §fis §a$timeP2, §fold best time was §a${oldPB}s")
                T5PBs.entries.find { it.splitName == "Build" }?.pbTime?.value = timeP2
                Config.save()
            }
        }

        onMessage("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!", false) {
            splits[3] = System.currentTimeMillis()
            if (LocationUtils.kuudraTier != 5) return@onMessage

            val oldPB = T5PBs.entries.find { it.splitName == "Stun" }?.pbTime?.value ?: 999.0
            val timeP3 = (splits[3] - splits[2]) / 1000.0
            modMessage("§6Fuel/Stun Took§7: §a$timeP3")
            if (timeP3 < oldPB && timeP3 > 1L){
                if (sendPB) modMessage("New best time for §6T5 Stun §fis §a$timeP3, §fold best time was §a${oldPB}s")
                T5PBs.entries.find { it.splitName == "Stun" }?.pbTime?.value = timeP3
                Config.save()
            }
        }

        onMessage("KUUDRA DOWN!", true) {
            splits[4] = System.currentTimeMillis()

            if (LocationUtils.kuudraTier == 5) {
                val oldKillPB = T5PBs.entries.find { it.splitName == "Kill" }?.pbTime?.value ?: 999.0
                val timeP4 = (splits[4] - splits[3]) / 1000.0
                modMessage("§6Kill Took§7: §a$timeP4")
                if (timeP4 < oldKillPB && timeP4 > 1L) {
                    if (sendPB) modMessage("New best time for §6T5 Kill §fis §a$timeP4, §fold best time was §a${oldKillPB}s")
                    T5PBs.entries.find { it.splitName == "Kill" }?.pbTime?.value = timeP4
                    Config.save()
                }
            }

            val oldPB = KuudraTiers.entries.find { it.tierName == "T${LocationUtils.kuudraTier}" }?.pbTime?.value ?: 999.0
            val (times, _) = getSplitTimes()
            for (i in 0..4) {
                val duration = formatTime(times[i])
                modMessage("§8${lines[i]}§7$duration")
            }
            val totalTime = times[4] / 1000.0

            if (totalTime < oldPB && totalTime > 1L) {
                if (sendPB) modMessage("§fNew best time for §6T${LocationUtils.kuudraTier} Kuudra §fis §a${totalTime}s, §fold best time was §a${oldPB}s")
                KuudraTiers.entries.getSafe(LocationUtils.kuudraTier)?.pbTime?.value = totalTime.round(2)
                Config.save()
            }
        }

        onMessage("DEFEAT", true) {
            splits[4] = System.currentTimeMillis()
        }

        onMessageCancellable(Regex("(\\[.+])? (\\w+) recovered one of Elle's supplies! \\((\\d/\\d)\\)")) {
            if (!sendSupplyTime) return@onMessageCancellable
            val matchResult = Regex("(\\[.+])? (\\w+) recovered one of Elle's supplies! \\((\\d/\\d)\\)").find(it.message) ?: return@onMessageCancellable
            modMessage("§6${matchResult.groupValues[2]}§a took ${formatTime((System.currentTimeMillis() - splits[0]))} to recover supply §8(${matchResult.groupValues[3]})!", false)
            it.isCanceled = true
        }

        onWorldLoad { splits.fill(0L) }
    }
}