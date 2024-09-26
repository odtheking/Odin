package me.odinmain.config

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import me.odinmain.utils.render.Color
import java.io.File
import java.io.IOException

object DungeonWaypointConfig {
    private val gson = GsonBuilder().registerTypeAdapter(Color::class.java, Color.ColorSerializer()).setPrettyPrinting().create()

    var waypoints: MutableMap<String, MutableList<DungeonWaypoint>> = mutableMapOf()

    private val configFile = File(mc.mcDataDir, "config/odin/dungeon-waypoint-config-CLAY.json").apply {
        try {
            createNewFile()
        } catch (_: Exception) {
            println("Error initializing module config")
        }
    }

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this.isEmpty()) return

                waypoints = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<DungeonWaypoint>>>() {}.type
                )
            }
        }  catch (e: JsonSyntaxException) {
            println("Error parsing configs.")
            println(e.message)
            logger.error("Error parsing configs.", e)
        } catch (_: JsonIOException) {
            println("Error reading configs.")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveConfig() {
        scope.launch(Dispatchers.IO) {
            try {
                configFile.bufferedWriter().use {
                    it.write(gson.toJson(waypoints))
                }
            } catch (_: IOException) {
                println("Error saving Waypoint config.")
            }
        }
    }
}