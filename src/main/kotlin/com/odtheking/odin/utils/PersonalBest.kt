package com.odtheking.odin.utils

import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.ModuleManager

class PersonalBest(module: Module, name: String) {

    private val mapSetting = module.registerSetting(MapSetting(name, mutableMapOf<String, Float>()))
    /**
     * Updates the personal best for a specific puzzle
     * 
     * @param index The name of the puzzle
     * @param time The new time achieved
     * @param unit The unit of measurement for display
     * @param message The message prefix to display
     */
    fun time(index: String, time: Float, unit: String = "s§7!", message: String, sendMessage: Boolean = true) {
        val msg = "$message$time$unit"
        val oldPB = mapSetting.value[index] ?: 9999f

        if (oldPB > time) {
            set(index, time)
            if (sendMessage) modMessage("$msg §7(§d§lNew PB§r§7) Old PB was §8$oldPB")
        } else if (sendMessage) modMessage("$msg §8(§7$oldPB§8)")
    }

    fun get(index: String): Float? = mapSetting.value[index]

    fun set(index: String, time: Float) {
        mapSetting.value[index] = time
        ModuleManager.saveConfigurations()
    }

    fun reset() {
        mapSetting.value.clear()
        ModuleManager.saveConfigurations()
    }
}