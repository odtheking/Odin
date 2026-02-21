package com.odtheking.odin.config

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import com.odtheking.odin.utils.Color
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.encoding.Base64

object DungeonWaypointConfig {
    private val gson = GsonBuilder()
        .registerTypeAdapter(AABB::class.java, AABBSerializer())
        .registerTypeAdapter(DungeonWaypoint::class.java, DungeonWaypointDeserializer())
        .setPrettyPrinting().create()

    fun encodeWaypoints(waypointsMap: MutableMap<String, MutableList<DungeonWaypoint>>): String? {
        return try {
            Base64.encode(compress(gson.toJson(waypointsMap)))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun compress(input: String): ByteArray {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
                gzipOutputStream.write(input.toByteArray())
            }
            return byteArrayOutputStream.toByteArray()
        }
    }

    fun decodeWaypoints(input: String, isJson: Boolean): MutableMap<String, MutableList<DungeonWaypoint>>? {
        return try {
            gson.fromJson(
                if (isJson) input else decompress(Base64.decode(input)),
                object : TypeToken<MutableMap<String, MutableList<DungeonWaypoint>>>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decompress(compressed: ByteArray): String {
        GZIPInputStream(compressed.inputStream()).use { gzipInputStream ->
            return gzipInputStream.bufferedReader().use { it.readText() }
        }
    }

    internal class DungeonWaypointDeserializer : JsonDeserializer<DungeonWaypoint> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): DungeonWaypoint {
            val jsonObj = json.asJsonObject

            val blockPos = if (jsonObj.has("blockPos")) context.deserialize(jsonObj["blockPos"], BlockPos::class.java)
            else BlockPos(jsonObj["x"].asInt, jsonObj["y"].asInt, jsonObj["z"].asInt)

            val color = context.deserialize<Color>(jsonObj["color"], Color::class.java)

            val filled = jsonObj["filled"].asBoolean
            val depth = jsonObj["depth"].asBoolean

            val aabb = context.deserialize<AABB>(jsonObj["aabb"], AABB::class.java)

            val title = jsonObj["title"]?.asString?.ifBlank { null }
            val waypointType = jsonObj["type"]?.asString?.let {
                runCatching { DungeonWaypoints.WaypointType.valueOf(it) }.getOrNull()
            }

            val waypoint = DungeonWaypoint(blockPos, color, filled, depth, aabb, title, type = waypointType)

            if (waypointType == null && jsonObj.has("secret") && jsonObj["secret"]?.asBoolean == true)
                waypoint.type = DungeonWaypoints.WaypointType.SECRET

            return waypoint
        }
    }

    internal class AABBSerializer : JsonSerializer<AABB>, JsonDeserializer<AABB> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): AABB {
            val jsonObj = json.asJsonObject

            if (jsonObj.has("minX")) {
                val minX = jsonObj["minX"].asDouble
                val minY = jsonObj["minY"].asDouble
                val minZ = jsonObj["minZ"].asDouble
                val maxX = jsonObj["maxX"].asDouble
                val maxY = jsonObj["maxY"].asDouble
                val maxZ = jsonObj["maxZ"].asDouble

                return AABB(minX, minY, minZ, maxX, maxY, maxZ)
            }

            if (jsonObj.has("field_1323")) {
                val minX = jsonObj["field_1323"].asDouble
                val minY = jsonObj["field_1322"].asDouble
                val minZ = jsonObj["field_1321"].asDouble
                val maxX = jsonObj["field_1320"].asDouble
                val maxY = jsonObj["field_1325"].asDouble
                val maxZ = jsonObj["field_1324"].asDouble

                return AABB(minX, minY, minZ, maxX, maxY, maxZ)
            }

            if (jsonObj.has("field_72340_a")) {
                val minX = jsonObj["field_72340_a"].asDouble
                val minY = jsonObj["field_72338_b"].asDouble
                val minZ = jsonObj["field_72339_c"].asDouble
                val maxX = jsonObj["field_72336_d"].asDouble
                val maxY = jsonObj["field_72337_e"].asDouble
                val maxZ = jsonObj["field_72334_f"].asDouble

                return AABB(minX, minY, minZ, maxX, maxY, maxZ)
            }

            return AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        override fun serialize(src: AABB, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("minX", src.minX)
            jsonObject.addProperty("minY", src.minY)
            jsonObject.addProperty("minZ", src.minZ)
            jsonObject.addProperty("maxX", src.maxX)
            jsonObject.addProperty("maxY", src.maxY)
            jsonObject.addProperty("maxZ", src.maxZ)

            return jsonObject
        }
    }
}

