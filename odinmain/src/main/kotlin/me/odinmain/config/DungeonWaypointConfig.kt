package me.odinmain.config

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import me.odinmain.utils.render.Color
import net.minecraft.util.AxisAlignedBB
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

object DungeonWaypointConfig {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Color::class.java, Color.ColorSerializer())
        .registerTypeAdapter(AxisAlignedBB::class.java, AxisAlignedBBSerializer())
        .registerTypeAdapter(DungeonWaypoint::class.java, DungeonWaypointDeserializer())
        .setPrettyPrinting().create()

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
                if (isEmpty()) return

                waypoints = gson.fromJson(
                    this,
                    object : TypeToken<MutableMap<String, MutableList<DungeonWaypoint>>>() {}.type
                )
            }
        } catch (e: JsonSyntaxException) {
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

    private class DungeonWaypointDeserializer : JsonDeserializer<DungeonWaypoint> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): DungeonWaypoint {
            val jsonObj = json.asJsonObject

            val x = jsonObj["x"].asDouble
            val y = jsonObj["y"].asDouble
            val z = jsonObj["z"].asDouble
            val color = context.deserialize<Color>(jsonObj["color"], Color::class.java)

            val filled = jsonObj["filled"].asBoolean
            val depth = jsonObj["depth"].asBoolean

            val aabb = context.deserialize<AxisAlignedBB>(jsonObj["aabb"], AxisAlignedBB::class.java)

            val title = jsonObj["title"]?.asString?.ifBlank { null }
            val waypointType = jsonObj["type"]?.asString?.let {
                runCatching { DungeonWaypoints.WaypointType.valueOf(it) }.getOrNull()
            }
            val timerType = jsonObj["timer"]?.asString?.let {
                runCatching { DungeonWaypoints.TimerType.valueOf(it) }.getOrNull()
            }

            val waypoint = DungeonWaypoint(x, y, z, color, filled, depth, aabb, title, type = waypointType, timer = timerType)
            // Handle the "secret" to "type" migration
            if (waypointType == null && jsonObj.has("secret") && jsonObj["secret"]?.asBoolean == true) waypoint.secret = true

            return waypoint
        }
    }

    class AxisAlignedBBSerializer : JsonDeserializer<AxisAlignedBB>, JsonSerializer<AxisAlignedBB> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext?): AxisAlignedBB {
            val jsobObj = json.asJsonObject
            if (jsobObj.has("minX")) {
                val minX = jsobObj["minX"].asDouble
                val minY = jsobObj["minY"].asDouble
                val minZ = jsobObj["minZ"].asDouble
                val maxX = jsobObj["maxX"].asDouble
                val maxY = jsobObj["maxY"].asDouble
                val maxZ = jsobObj["maxZ"].asDouble

                return AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
            } else {
                val minX = jsobObj["field_72340_a"].asDouble
                val minY = jsobObj["field_72338_b"].asDouble
                val minZ = jsobObj["field_72339_c"].asDouble
                val maxX = jsobObj["field_72336_d"].asDouble
                val maxY = jsobObj["field_72337_e"].asDouble
                val maxZ = jsobObj["field_72334_f"].asDouble

                return AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
            }
        }

        override fun serialize(aabb: AxisAlignedBB, type: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("minX", aabb.minX)
            jsonObject.addProperty("minY", aabb.minY)
            jsonObject.addProperty("minZ", aabb.minZ)
            jsonObject.addProperty("maxX", aabb.maxX)
            jsonObject.addProperty("maxY", aabb.maxY)
            jsonObject.addProperty("maxZ", aabb.maxZ)

            return jsonObject
        }

    }
}