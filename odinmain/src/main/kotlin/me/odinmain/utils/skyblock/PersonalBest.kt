package me.odinmain.utils.skyblock

import me.odinmain.config.PBConfig

class PersonalBest(val name: String, val size: Int) {
    var pb  get() = PBConfig.pbs[name]
            set(value) { value?.let { PBConfig.pbs[name] = it } }
    init {
        if (pb == null || pb?.size != size) {
            pb = MutableList(size) { 9999.0 }
            PBConfig.saveConfig()
        }
    }

    fun set(index: Int, value: Double) {
        pb?.set(index, value)
        PBConfig.saveConfig()
    }
}