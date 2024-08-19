package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.IceFillFloors.IceFillFloors
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import me.odinmain.utils.skyblock.isAir
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.*
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object IceFillSolver {
    var scanned = false
    var currentPatterns: MutableList<List<Vec3i>> = ArrayList()
    private var renderRotation: Rotations? = null
    private var patternStartPositions: MutableList<Vec3> = ArrayList()

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
        if (currentPatterns.size == 0 || patternStartPositions.size == 0 || DungeonUtils.currentRoomName != "Ice Fill") return
        val rotation = renderRotation ?: return

        val pointsList = mutableListOf<Vec3>()
        for (index in currentPatterns.indices) {
            val pattern = currentPatterns[index]
            val startPos = patternStartPositions[index]
            pointsList.add(startPos)
            for (point in pattern) {
                val transformedPoint = startPos.add(transformTo(point, rotation))
                pointsList.add(transformedPoint)
            }
            val stairPos = startPos.add(transformTo(pattern.last().addVec(1, 1), rotation))
            pointsList.add(stairPos)
        }
        Renderer.draw3DLine(*pointsList.toTypedArray(), color = color, depth = true)
    }

    fun enterDungeonRoom(event: RoomEnterEvent) {
        val room = event.fullRoom?.room ?: return
        if (room.data.name != "Ice Fill" || scanned) return

        val startPos = room.vec2.addRotationCoords(room.rotation, 8)
        scanAllFloors(Vec3(startPos.x.toDouble(), 70.0, startPos.z.toDouble()), room.rotation)
        scanned = true
    }

    private fun scanAllFloors(pos: Vec3, rotation: Rotations) {
        scan(pos, 0, rotation)

        scan(pos.add(transformTo(Vec3i(5, 1, 0), rotation)), 1, rotation)

        scan(pos.add(transformTo(Vec3i(12, 2, 0), rotation)), 2, rotation)
    }

    private fun scan(pos: Vec3, floorIndex: Int, rotation: Rotations) {
        val floorHeight = representativeFloors[floorIndex]
        val startTime = System.nanoTime()

        for (patternIndex in floorHeight.indices) {
            if (
                isAir(BlockPos(pos).add(transform(floorHeight[patternIndex][0], floorHeight[patternIndex][1], rotation))) &&
                !isAir(BlockPos(pos).add(transform(floorHeight[patternIndex][2], floorHeight[patternIndex][3], rotation)))
            ) {
                modMessage("Section $floorIndex scan took ${(System.nanoTime() - startTime) / 1000000.0}ms pattern: $patternIndex")

                renderRotation = rotation
                patternStartPositions.add(pos.addVec(x= 0.5, y = 0.1, z = 0.5))
                currentPatterns.add(IceFillFloors[floorIndex][patternIndex].toMutableList())
                return
            }
        }
        modMessage("§cFailed to scan floor ${floorIndex + 1}")
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
        scanned = false
        renderRotation = null
        patternStartPositions = ArrayList()
    }
}
