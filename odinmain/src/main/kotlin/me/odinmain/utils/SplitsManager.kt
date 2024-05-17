package me.odinmain.utils

import me.odinmain.config.Config
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.createClickStyle
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.event.ClickEvent

data class Split(val message: String, val name: String, val pb: NumberSetting<Double>,var time: Long = 0L)
object SplitsManager {

    var currentSplits: MutableList<Split> = mutableListOf()

    fun getSplitTimes(): Pair<List<Long>, Int> {
        if (currentSplits.isEmpty() || currentSplits[0].time == 0L) return List(currentSplits.size) { 0L } to -1
        val latestTime = if (currentSplits.last().time == 0L) System.currentTimeMillis() else currentSplits.last().time
        val times = MutableList(currentSplits.size) { 0L }
        var current = currentSplits.size
        for (i in 0 until currentSplits.size - 1) {
            if (currentSplits[i + 1].time != 0L) {
                times[i] = currentSplits[i + 1].time - currentSplits[i].time
            } else {
                current = i
                times[i] = latestTime - currentSplits[i].time
                break
            }
        }
        return times to current
    }

    fun handleMessage(msg: String) {
        val currentSplit = currentSplits.find { it.message == msg } ?: return
        currentSplit.time = System.currentTimeMillis()

        val oldPB = currentSplit.pb.value
        val index = currentSplits.indexOf(currentSplit)
        if (index == 0) return

        val currentSplitTime = currentSplit.time - currentSplits[index - 1].time

        if (currentSplitTime.round(3).toDouble() <= oldPB) {
            currentSplit.pb.value = currentSplitTime.toDouble().round(3).toDouble()
            Config.save()
        }

        val allPBs = currentSplits.drop(1).joinToString("\n") { "§f${it.name}: ${formatTime(it.pb.value.toLong())}" }

        modMessage("§6${currentSplit.name} §ftook §a${formatTime(currentSplitTime)} ${if (currentSplitTime.round(3).toDouble() < oldPB) "§f(§d§lPB§f) §8${formatTime(oldPB.toLong())}" else ""}",
            chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, allPBs))
    }
}
