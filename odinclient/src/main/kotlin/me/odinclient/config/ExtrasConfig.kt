package me.odinclient.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.features.impl.dungeon.DungeonWaypoints
import me.odinmain.utils.render.Color
import java.io.File
import java.io.IOException

/*
TODO: fully add following features
Room/Region Extras:
{
    "region/room-name": {
        "name-acts-as-id": {
            "enabled": true,
            "online": "url-if-its-shared-via-a-link",
            "preBlocks": {
                "minecraft:air": [
                    "0, 0, 0",
                    "0, 0, 1"
                ],
                "minecraft:stained_glass[color=white]": [
                    "0, 0, 2",
                    "0, 0, 3"
                ]
            }
        }
    }
}

*/


object ExtrasConfig {
    private val gson =
        GsonBuilder().registerTypeAdapter(Color::class.java, Color.ColorSerializer()).setPrettyPrinting().create()

    var waypoints: MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>> = mutableMapOf()

    private val dungeonExtrasConfigFile = File(OdinMain.mc.mcDataDir, "config/odin/extra-config-dungeon.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    private val otherExtrasConfigFile = File(OdinMain.mc.mcDataDir, "config/odin/extra-config-other.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    fun loadConfig() {
        try {
            with(dungeonExtrasConfigFile.bufferedReader().use { it.readText() }) {
                if (this.isEmpty()) return

                waypoints = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>>>() {}.type
                )
            }
            with(otherExtrasConfigFile.bufferedReader().use { it.readText() }) {
                if (this.isEmpty()) return

                waypoints = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<DungeonWaypoints.DungeonWaypoint>>>() {}.type
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
                dungeonExtrasConfigFile.bufferedWriter().use {
                    it.write(gson.toJson(waypoints))
                }
                otherExtrasConfigFile.bufferedWriter().use {
                    it.write(gson.toJson(waypoints))
                }
            } catch (e: IOException) {
                println("Error saving Waypoint config.")
            }
        }
    }
}