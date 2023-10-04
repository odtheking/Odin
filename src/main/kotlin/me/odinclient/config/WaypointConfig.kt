package me.odinclient.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinclient.ModCore.Companion.scope
import me.odinclient.config.utils.ConfigFile
import me.odinclient.features.impl.render.WaypointManager.Waypoint
import java.io.IOException

object WaypointConfig {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    var waypoints: MutableMap<String, MutableList<Waypoint>> = mutableMapOf()

    private val configFile = ConfigFile("waypoint-config")

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") return

                waypoints = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<Waypoint>>>() {}.type
                )
            }
        } catch (e: JsonSyntaxException) {
            println("Error parsing configs.\n${e.message}")
        } catch (e: JsonIOException) {
            println("Error reading configs.\n${e.message}")
        }
    }

    fun saveConfig() {
        scope.launch(Dispatchers.IO) {
            try {
                configFile.bufferedWriter().use {
                    it.write(gson.toJson(waypoints))
                }
            } catch (e: IOException) {
                println("Error saving Waypoint config.\n${e.message}")
            }
        }
    }
}
