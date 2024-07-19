package me.odinmain.config

import com.github.stivais.ui.color.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

object DungeonWaypointConfigCLAY {
    private val gson = GsonBuilder().registerTypeAdapter(Color::class.java, ColorSerializer()).setPrettyPrinting().create()

    var waypoints: MutableMap<String, MutableList<DungeonWaypoint>> = mutableMapOf()

    private val configFile = File(mc.mcDataDir, "config/odin/dungeon-waypoint-config-CLAY.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
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
        } catch (e: JsonIOException) {
            println("Error reading configs.")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveConfig() {
        GlobalScope.launch(Dispatchers.IO) {
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

class ColorSerializer : JsonSerializer<Color>, JsonDeserializer<Color> {
    override fun serialize(src: Color?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive("#${src?.toHexString() ?: Color.BLACK.toHexString()}")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Color {
        val hexValue = json?.asString?.removePrefix("#") ?: "00000000"
        val color = hexToRGBA(hexValue)
        return Color.RGB(color.red, color.green, color.blue, color.alpha.toFloat())
    }
}