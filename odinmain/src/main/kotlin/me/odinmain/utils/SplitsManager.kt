package me.odinmain.utils

data class Split(val message: String, val name: String, var time: Long = 0L)

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


    }
}
