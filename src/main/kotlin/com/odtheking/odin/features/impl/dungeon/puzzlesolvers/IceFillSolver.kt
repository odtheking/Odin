package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.JsonResourceLoader
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.map.tile.DungeonRoom
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import java.lang.reflect.Type

object IceFillSolver {

    private var iceFillFloors = JsonResourceLoader.loadJson(
        "/assets/odin/puzzles/iceFillFloors.json",
        IceFillData(emptyList(), emptyList(), emptyList()),
        GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BlockPos::class.java, BlockPosDeserializer())
    )
    private var currentPatterns: ArrayList<Vec3> = ArrayList()


    fun onRenderWorld(event: RenderEvent.Extract, color: Color) {
        if (!currentPatterns.isEmpty() && DungeonUtils.currentRoomName == "Ice Fill")
            event.drawLine(currentPatterns, color, true)
    }

    fun onRoomEnter(event: RoomEnterEvent, optimizePatterns: Boolean) = with (event.room) {
        if (this?.data?.name != "Ice Fill" || currentPatterns.isNotEmpty()) return@with
        val patterns = if (optimizePatterns) iceFillFloors.hard else iceFillFloors.easy

        repeat(3) { index ->
            val floorIdentifiers = iceFillFloors.identifier[index]

            for (patternIndex in floorIdentifiers.indices) {
                if (isRealAir(floorIdentifiers[patternIndex][0]) && !isRealAir(floorIdentifiers[patternIndex][1])) {
                    currentPatterns.addAll(patterns[index][patternIndex].map { Vec3(getRealCoords(it)).add(0.5, 0.1, 0.5) })
                    return@repeat
                }
            }
            modMessage("§cFailed to scan floor $index")
        }
    }

    private fun DungeonRoom.isRealAir(pos: BlockPos): Boolean =
        mc.level?.getBlockState(getRealCoords(pos))?.isAir == true

    fun reset() {
        currentPatterns.clear()
    }

    private data class IceFillData(
        val identifier: List<List<List<BlockPos>>>,
        val easy: List<List<List<BlockPos>>>,
        val hard: List<List<List<BlockPos>>>
    )

    private class BlockPosDeserializer : JsonDeserializer<BlockPos> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BlockPos {
            val obj = json.asJsonObject
            val x = obj.get("x").asInt
            val y = obj.get("y").asInt
            val z = obj.get("z").asInt
            return BlockPos(x, y, z)
        }
    }
}