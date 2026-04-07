package com.odtheking.odin.utils

import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.ModuleManager

class PersonalBest(module: Module, name: String) {

    private val mapSetting = module.registerSetting(MapSetting(name, mutableMapOf<String, Float>()))
    /**
     * Updates the personal best for a specific puzzle
     * 
     * @param name The name of the puzzle
     * @param time The new time achieved
     * @param unit The unit of measurement for display
     * @param message The message prefix to display
     */
    fun time(name: String, time: Float, unit: String = "s§7!", message: String, sendMessage: Boolean = true) {
        val oldPB = mapSetting.value[name] ?: 9999f

        val msg = if (oldPB > time) {
            set(name, time)
            "§7(§d§lNew PB§r§7) Old PB was §8$oldPB"
        } else "§8(§7$oldPB§8)"
        if (sendMessage) modMessage("$message$time$unit $msg")
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