package me.odinmain.utils.skyblock

import me.odinmain.config.PBConfig
import me.odinmain.utils.getSafe

class PersonalBest(val name: String, val size: Int) {
    var pb get() = PBConfig.pbs[name]
            set(value) { value?.let { PBConfig.pbs[name] = it } }
    init {
        if (pb == null || pb?.size != size) {
            pb = MutableList(size) { 9999.0 }
            PBConfig.saveConfig()
        }
    }

    /**
     * Updates the personal best at the specified index.
     *
     * @param index The index of the personal best to update.
     * @param time The time to compare with the personal best.
     * @param unit The unit of the time.
     * @param message The message to send.
     * @param alwaysSendPB Whether to always send the old personal best in the message.
     * @param sendMessage Whether to send the message at all.
     */
    fun time(index: Int, time: Double, unit: String = "s§7!", message: String, sendOnlyPB: Boolean = false, alwaysSendPB: Boolean = false, sendMessage: Boolean = true) {
        var msg = "$message$time$unit"
        val oldPB = pb?.getSafe(index) ?: 999.0
        if (oldPB > time) {
            set(index, time)
            msg += " §7(§d§lNew PB§r§7) Old PB was §8$oldPB"
            if (sendMessage) modMessage(msg)
        } else if (!sendOnlyPB && sendMessage) modMessage("$msg ${if (alwaysSendPB) "(§8$oldPB§7)" else ""}")
    }

    fun set(index: Int, value: Double) {
        pb?.set(index, value)
        PBConfig.saveConfig()
    }
}