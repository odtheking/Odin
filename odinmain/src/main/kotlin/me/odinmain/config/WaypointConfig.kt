package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.features.impl.render.WaypointManager.Waypoint
import java.io.File
import java.io.IOException

object WaypointConfig {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    var waypoints: MutableMap<String, MutableList<Waypoint>> = mutableMapOf()

    private val configFile = File(mc.mcDataDir, "config/odin/waypoint-config.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") return

                waypoints = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<Waypoint>>>() {}.type
                )
            }
        }  catch (e: JsonSyntaxException) {
            println("Error parsing configs.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading configs.")
        }
    }

    fun saveConfig() {
        scope.launch(Dispatchers.IO) {
            try {
                configFile.bufferedWriter().use {
                    it.write(gson.toJson(waypoints))
                }
            } catch (e: IOException) {
                println("Error saving Waypoint config.")
            }
        }
    }
}