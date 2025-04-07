package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import java.io.File

object SlotBindsConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    var slotBinds: MutableMap<Int, Int> = mutableMapOf()

    private val configFile = File(mc.mcDataDir, "config/odin/slot-binds.json").apply {
        try {
            createNewFile()
        } catch (_: Exception) {
            println("Error creating slot binds config file.")
        }
    }

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (isEmpty()) return

                slotBinds = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<Int, Int>>() {}.type
                )
                println("Successfully loaded slot binds config $slotBinds")
            }
        } catch (e: Exception) {
            println("Odin: Error parsing slot binds.")
            println(e.message)
            logger.error("Error parsing slot binds.", e)
        }
    }

    fun saveConfig() {
        scope.launch(Dispatchers.IO) {
            try {
                configFile.bufferedWriter().use {
                    it.write(gson.toJson(slotBinds))
                }
            } catch (_: Exception) {
                println("Odin: Error saving slot binds config.")
            }
        }
    }
}