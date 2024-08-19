package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.logger
import java.io.File

object PBConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    var pbs: MutableMap<String, MutableList<Double>> = mutableMapOf()

    private val configFile = File(OdinMain.mc.mcDataDir, "config/odin/personal-bests.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing personal bests config")
        }
    }

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") return

                pbs = gson.fromJson(
                        this,
                        object : TypeToken<MutableMap<String, MutableList<Double>>>() {}.type
                )
                println("Successfully loaded pb config $pbs")
            }
        }  catch (e: Exception) {
            println("Odin: Error parsing pbs.")
            println(e.message)
            logger.error("Error parsing pbs.", e)
        }
    }

    fun saveConfig() {
        OdinMain.scope.launch(Dispatchers.IO) {
            try {
                configFile.bufferedWriter().use {
                    it.write(gson.toJson(pbs))
                }
            } catch (_: Exception) {
                println("Odin: Error saving Waypoint config.")
            }
        }
    }
}