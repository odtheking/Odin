package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.config.utils.ConfigFile
import me.odinmain.features.impl.render.ClickGUIModule.blacklist
import me.odinmain.features.impl.render.CustomHighlight.highlightList

@Deprecated("Use settings under modules instead.", level = DeprecationLevel.WARNING)
object MiscConfig {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val espConfigFile = ConfigFile("esp-config")
    private val blacklistConfigFile = ConfigFile("blacklist-config")
    private val autoSellConfigFile = ConfigFile("autoSell-config")

    // after a few updates remove
    fun loadConfig() {
        try {
            if (espConfigFile.exists()) {
                with(espConfigFile.bufferedReader().use { it.readText() }) {
                    if (this != "") {
                        val temp = gson.fromJson<MutableList<String>>(this, object : TypeToken<MutableList<String>>() {}.type)
                        highlightList.addAll(temp)
                    }
                    espConfigFile.delete()
                }
            }
            if (blacklistConfigFile.exists()) {
                with(blacklistConfigFile.bufferedReader().use { it.readText() }) {
                    if (this != "") {
                        val temp = gson.fromJson<MutableList<String>>(this, object : TypeToken<MutableList<String>>() {}.type)
                        blacklist.addAll(temp)
                    }
                    blacklistConfigFile.delete()
                }
            }
            if (autoSellConfigFile.exists()) {
                autoSellConfigFile.delete()
            }
        } catch (e: Exception) {
            println("Error parsing configs.\n${e.message}")
        }
    }
}
