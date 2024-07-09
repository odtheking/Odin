package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.IceFillFloors.floors
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import net.minecraft.util.*
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object IceFillSolver {
    var scanned = false
    var currentPatterns: MutableList<List<Vec3i>> = ArrayList()
    private var renderRotation: Rotations? = null
    private var rPos: MutableList<Vec3> = ArrayList()

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

    private fun renderPattern(pos: Vec3, rotation: Rotations) {
        renderRotation = rotation
        rPos.add(Vec3(pos.xCoord + 0.5, pos.yCoord + 0.1, pos.zCoord + 0.5))
    }

    fun onRenderWorldLast(color: Color) {
        if (currentPatterns.size == 0 || rPos.size == 0 || DungeonUtils.currentRoomName != "Ice Fill") return
        val rotation = renderRotation ?: return

        val pointsList = mutableListOf<Vec3>()
        for (i in currentPatterns.indices) {
            val pattern = currentPatterns[i]
            val startPos = rPos[i]
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
        val room = event.room?.room ?: return
        if (room.data.name != "Ice Fill" || scanned) return
        val rotation = room.rotation

        val startPos = room.vec2.addRotationCoords(rotation, 8)
        scanAllFloors(Vec3(startPos.x.toDouble(), 70.0, startPos.z.toDouble()), rotation)
        scanned = true
    }

    private fun scanAllFloors(pos: Vec3, rotation: Rotations) {
        scan(pos, 0, rotation)

        scan(pos.add(transformTo(Vec3i(5, 1, 0), rotation)), 1, rotation)

        scan(pos.add(transformTo(Vec3i(12, 2, 0), rotation)), 2, rotation)
    }

    private fun scan(pos: Vec3, floorIndex: Int, rotation: Rotations) {
        val bPos = BlockPos(pos)

        val floorHeight = representativeFloors[floorIndex]
        val startTime = System.nanoTime()

        for (index in floorHeight.indices) {
            if (
                isAir(bPos.add(transform(floorHeight[index][0], floorHeight[index][1], rotation))) &&
                !isAir(bPos.add(transform(floorHeight[index][2], floorHeight[index][3], rotation)))
            ) {
                devMessage("Section ${floorIndex + 1} scan took ${(System.nanoTime() - startTime) / 1000000.0}ms pattern: ${index + 1}")

                renderPattern(pos, rotation)
                currentPatterns.add(floors[floorIndex][index].toMutableList())
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
        return when (rotation) {
            Rotations.NORTH -> Vec3(vec.z.toDouble(), vec.y.toDouble(), -vec.x.toDouble())
            Rotations.WEST -> Vec3(-vec.x.toDouble(), vec.y.toDouble(), -vec.z.toDouble())
            Rotations.SOUTH -> Vec3(-vec.z.toDouble(), vec.y.toDouble(), vec.x.toDouble())
            Rotations.EAST -> Vec3(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
            else -> Vec3(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
        }
    }

    fun reset() {
        currentPatterns = ArrayList()
        scanned = false
        renderRotation = null
        rPos = ArrayList()
    }
}
