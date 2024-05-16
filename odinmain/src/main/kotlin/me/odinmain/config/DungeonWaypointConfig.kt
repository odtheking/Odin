package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.DungeonWaypoints.DungeonWaypoint
import me.odinmain.features.impl.dungeon.DungeonWaypoints.WaypointCategory
import me.odinmain.utils.render.Color
import java.io.File
import java.io.IOException

object DungeonWaypointConfig {
    private val gson =
        GsonBuilder().registerTypeAdapter(Color::class.java, Color.ColorSerializer()).setPrettyPrinting().create()

    var waypoints: MutableMap<String, MutableList<DungeonWaypoint>> = mutableMapOf()

    var waypointsRooms: MutableMap<String, MutableList<WaypointCategory>> = mutableMapOf()
    var waypointsRegions: MutableMap<String, MutableList<WaypointCategory>> = mutableMapOf()


    private val configFileRooms = File(mc.mcDataDir, "config/odin/dungeon-waypoint-config-rooms.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    private val configFileOther = File(mc.mcDataDir, "config/odin/dungeon-waypoint-config-other.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    fun loadConfig() {
        try {
            with(configFileRooms.bufferedReader().use { it.readText() }) {
                if (this.isEmpty()) return

                waypointsRooms = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<WaypointCategory>>>() {}.type
                )
            }
            with(configFileOther.bufferedReader().use { it.readText() }) {
                if (this.isEmpty()) return

                waypointsRegions = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<WaypointCategory>>>() {}.type
                )
            }
        } catch (e: JsonSyntaxException) {
            println("Error parsing configs.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading configs.")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveConfig() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                configFileRooms.bufferedWriter().use {
                    it.write(gson.toJson(waypointsRooms))
                }
                configFileOther.bufferedWriter().use {
                    it.write(gson.toJson(waypointsRegions))
                }
            } catch (e: IOException) {
                println("Error saving Waypoint config.")
            }
        }
    }
}