package me.odinmain.utils.skyblock

import me.odinmain.config.PBConfig

class PersonalBest(val name: String, val size: Int) {
    var pb get() = PBConfig.pbs[name]
            set(value) { value?.let { PBConfig.pbs[name] = it } }
    init {
        if (pb == null || pb?.size != size) {
            pb = MutableList(size) { 9999.0 }
            PBConfig.saveConfig()
        }
    }

    fun time(index: Int, time: Double, unit: String, message: String, addPBString: Boolean, addOldPBString: Boolean) {
        var msg = "$message$time$unit"
        val oldPB = pb?.get(index) ?: 999.0
        if (oldPB > time) {
            set(index, time)
            if (addPBString)
                msg += " §7(§d§lNew PB§r§7)"
            if (addOldPBString)
                msg += " Old PB was §8$oldPB"
        }
        modMessage(msg)
    }

    fun set(index: Int, value: Double) {
        pb?.set(index, value)
        //modMessage("Set $index value to $value")
        PBConfig.saveConfig()
    }
}