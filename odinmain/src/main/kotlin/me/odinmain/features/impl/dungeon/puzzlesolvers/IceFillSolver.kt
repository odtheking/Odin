package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.IceFillFloors
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import me.odinmain.utils.skyblock.isAir
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.*
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object IceFillSolver {
    var currentPatterns: ArrayList<List<Vec3>> = ArrayList()

    private var representativeFloors: List<List<List<Int>>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/icefillFloors.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            representativeFloors = gson.fromJson(text, object : TypeToken<List<List<List<Int>>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading ice fill floors", e)
            representativeFloors = emptyList()
        }
    }

    fun onRenderWorldLast(color: Color) {
        if (currentPatterns.isEmpty() || DungeonUtils.currentRoomName != "Ice Fill") return

        currentPatterns.forEach {
            Renderer.draw3DLine(*it.toTypedArray(), color = color, depth = true)
        }
    }

    fun enterDungeonRoom(event: RoomEnterEvent) {
        val room = event.fullRoom?.room ?: return
        if (room.data.name != "Ice Fill") return

        scanAllFloors(room.vec3.addRotationCoords(room.rotation, 8), room.rotation)
    }

    private fun scanAllFloors(pos: Vec3, rotation: Rotations) {
        listOf(pos, pos.add(transformTo(Vec3i(5, 1, 0), rotation)), pos.add(transformTo(Vec3i(12, 2, 0), rotation))).forEachIndexed { floorIndex, startPosition ->
            val floorHeight = representativeFloors[floorIndex]
            val startTime = System.nanoTime()

            for (patternIndex in floorHeight.indices) {
                if (
                    isAir(BlockPos(startPosition).add(transform(floorHeight[patternIndex][0], floorHeight[patternIndex][1], rotation))) &&
                    !isAir(BlockPos(startPosition).add(transform(floorHeight[patternIndex][2], floorHeight[patternIndex][3], rotation)))
                ) {
                    modMessage("Section $floorIndex scan took ${(System.nanoTime() - startTime) / 1000000.0}ms pattern: $patternIndex")

                    (if (PuzzleSolvers.useOptimizedPatterns) IceFillFloors.advanced[floorIndex][patternIndex] else IceFillFloors.IceFillFloors[floorIndex][patternIndex]).toMutableList().let {
                        currentPatterns.add(it.map { startPosition.addVec(x= 0.5, y = 0.1, z = 0.5).add(transformTo(it, rotation)) })
                    }

                    return@forEachIndexed
                }
            }
            modMessage("§cFailed to scan floor ${floorIndex + 1}")
        }
    }

    private fun transform(x: Int, z: Int, rotation: Rotations): Vec2 {
        return when (rotation) {
            Rotations.NORTH -> Vec2(z, -x)
            Rotations.WEST -> Vec2(-x, -z)
            Rotations.SOUTH -> Vec2(-z, x)
            Rotations.EAST -> Vec2(x, z)
            else -> Vec2(x, z)
        }
    }

    fun transformTo(vec: Vec3i, rotation: Rotations): Vec3 {
        return with(transform(vec.x, vec.z, rotation)) {
            Vec3(x.toDouble(), vec.y.toDouble(), z.toDouble())
        }
    }

    fun reset() {
        currentPatterns = ArrayList()
    }
}
