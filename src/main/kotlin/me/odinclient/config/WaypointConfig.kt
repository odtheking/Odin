package me.odinclient.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinclient.features.general.WaypointManager.Waypoint
import java.io.File
import java.io.IOException

class WaypointConfig(path: File) {

    private val gson = GsonBuilder()
        .setPrettyPrinting().create()

    private val configFile = File(path, "waypoint-config.json")
    var waypoints: MutableMap<String, MutableList<Waypoint>> = mutableMapOf()

    init {
        try {
            if (!path.mkdirs()) path.mkdirs()
            configFile.createNewFile()
        } catch (e: Exception) {
            println("Error initializing Waypoint Config")
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

    @OptIn(DelicateCoroutinesApi::class)
    fun saveConfig() {
        GlobalScope.launch {
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