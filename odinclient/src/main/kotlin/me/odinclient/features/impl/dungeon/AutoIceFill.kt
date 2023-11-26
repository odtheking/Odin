package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.utils.AsyncUtils.waitUntilPacked
import me.odinclient.utils.skyblock.IceFillFloors.floors
import me.odinclient.utils.skyblock.IceFillFloors.representativeFloors
import me.odinclient.utils.skyblock.PlayerUtils.clipTo
import me.odinclient.utils.skyblock.PlayerUtils.posFloored
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.floored
import me.odinmain.utils.plus
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.WorldUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.sin

object AutoIceFill: Module(
    name = "Auto Ice Fill",
    description = "Automatically completes the ice fill puzzle.",
    category = Category.DUNGEON,
    tag = TagType.RISKY
) {
    private var scanned = false
    private var currentPatterns: MutableList<List<Vec3i>> = ArrayList()
    private var renderRotation: Rotation? = null
    private var rPos: MutableList<Vec3> = ArrayList()
    private enum class Rotation {
        EAST, WEST, SOUTH, NORTH
    }

    private fun renderPattern(pos: Vec3i, rotation: Rotation) {
        renderRotation = rotation
        rPos.add(Vec3(pos.x + 0.5, pos.y + 0.1, pos.z + 0.5))
    }

    private fun getRainbowColor(): Color {
        val time = System.currentTimeMillis()
        val frequency = 0.001
        val r = sin(frequency * time + 0) * 127 + 128
        val g = sin(frequency * time + 2) * 127 + 128
        val b = sin(frequency * time + 4) * 127 + 128
        return Color((r / 255).toFloat(), (g / 255).toFloat(), (b / 255).toFloat())
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (currentPatterns.size == 0 || rPos.size == 0) return

        val color = getRainbowColor()
        val pt = event.partialTicks

        for (i in currentPatterns.indices) {
            val pattern = currentPatterns[i]
            val pos = rPos[i]
            RenderUtils.draw3DLine(pos, pos + transformTo(pattern[0], renderRotation!!), color, 10, true, pt)

            for (j in 1 until pattern.size) {
                RenderUtils.draw3DLine(
                    pos + transformTo(pattern[j - 1], renderRotation!!), pos + transformTo(pattern[j], renderRotation!!), color, 10, true, pt
                )
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null) return
        val pos = posFloored
        if (
            scanned ||
            !DungeonUtils.inDungeons ||
            WorldUtils.getBlockIdAt(BlockPos(pos.x, pos.y - 1, pos.z )) != 79 ||
            pos.y != 70
        ) return
        scanned = true
        GlobalScope.launch {
            scan(pos, 0)
        }
    }

    private suspend fun scan(pos: Vec3i, floorIndex: Int) {
        val rotation = checkRotation(pos, floorIndex) ?: return

        val bPos = BlockPos(pos)

        val floorHeight = representativeFloors[floorIndex]
        val startTime = System.nanoTime()

        for (index in floorHeight.indices) {
            if (
                WorldUtils.isAir(bPos.add(transform(floorHeight[index].first, rotation))) &&
                !WorldUtils.isAir(bPos.add(transform(floorHeight[index].second, rotation)))
            ) {
                val scanTime: Double = (System.nanoTime() - startTime) / 1000000.0
                modMessage("Scan took $scanTime ms")

                renderPattern(pos, rotation)
                currentPatterns.add(floors[floorIndex][index].toMutableList())


                move(Vec3(pos.x.toDouble(), pos.y - 1.0, pos.z.toDouble()), floors[floorIndex][index].toMutableList(), rotation, floorIndex)
                return
            }
        }
    }

    private suspend fun move(pos: Vec3, pattern: List<Vec3i>, rotation: Rotation, floorIndex: Int) {
        val x = pos.xCoord
        val y = pos.yCoord
        val z = pos.zCoord
        val deferred1 = waitUntilPacked(x, y, z)
        try {
            deferred1.await()
        } catch (e: Exception) {
            return
        }
        val (bx, bz) = transform(pattern[0].x, pattern[0].z, rotation)
        clipTo(x + bx, y + 1, z + bz)
        for (i in 0..pattern.size - 2) {
            val deferred = waitUntilPacked(
                pos + transformTo(pattern[i], rotation)
            )
            try {
                deferred.await()
            } catch (e: Exception) {
                return
            }
            clipTo(
                pos + transformTo(pattern[i + 1], rotation).addVector(0.0, 1.0, 0.0)
            )
        }
        if (floorIndex == 2) return
        val (bx2, bz2) = transform(pattern[pattern.size - 1].x, pattern[pattern.size - 1].z, rotation)
        val deferred = waitUntilPacked(x + bx2, y, z + bz2)
        try {
            deferred.await()
        } catch (e: Exception) {
            return
        }
        clipToNext(pos, rotation, bx2, bz2, floorIndex)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun clipToNext(pos: Vec3, rotation: Rotation, bx: Int, bz: Int, floorIndex: Int) {
        val x = pos.xCoord
        val y = pos.yCoord
        val z = pos.zCoord
        val (nx, ny) = when (rotation) {
            Rotation.EAST -> Pair(0.5f, 0f)
            Rotation.WEST -> Pair(-0.5f, 0f)
            Rotation.SOUTH -> Pair(0f, 0.5f)
            else -> Pair(0f, -0.5f)
        }
        clipTo(x + bx + nx, y + 1.5, z + bz + ny)
        GlobalScope.launch {
            delay(100)
            clipTo(x + bx + nx * 2, y + 2, z + bz + ny * 2)
            delay(100)
            clipTo(x + bx + nx * 4, y + 2, z + bz + ny * 4)
            scan(mc.thePlayer.positionVector.floored(), floorIndex + 1)
        }
    }

    private fun transform(vec: Vec3i, rotation: Rotation): Vec3i {
        return when (rotation) {
            Rotation.EAST -> Vec3i(vec.x, vec.y, vec.z)
            Rotation.WEST -> Vec3i(-vec.x, vec.y, -vec.z)
            Rotation.SOUTH -> Vec3i(vec.z, vec.y, vec.x)
            else -> Vec3i(vec.z, vec.y, -vec.x)
        }
    }

    private fun transformTo(vec: Vec3i, rotation: Rotation): Vec3 {
        return when (rotation) {
            Rotation.EAST -> Vec3(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
            Rotation.WEST -> Vec3(-vec.x.toDouble(), vec.y.toDouble(), -vec.z.toDouble())
            Rotation.SOUTH -> Vec3(vec.z.toDouble(), vec.y.toDouble(), vec.x.toDouble())
            else -> Vec3(vec.z.toDouble(), vec.y.toDouble(), -vec.x.toDouble())
        }
    }

    private fun transform(x: Int, z: Int, rotation: Rotation): Pair<Int, Int> {
        return when (rotation) {
            Rotation.EAST -> Pair(x, z)
            Rotation.WEST -> Pair(-x, -z)
            Rotation.SOUTH -> Pair(z, x)
            else -> Pair(z, -x)
        }
    }

    private fun checkRotation(pos: Vec3i, floor: Int): Rotation? {
        val a = (floor+1)*2+2
        if      (WorldUtils.getBlockIdAt(pos.x + a, pos.y, pos.z) == 109) return Rotation.EAST
        else if (WorldUtils.getBlockIdAt(pos.x - a, pos.y, pos.z) == 109) return Rotation.WEST
        else if (WorldUtils.getBlockIdAt(pos.x, pos.y, pos.z + a) == 109) return Rotation.SOUTH
        else if (WorldUtils.getBlockIdAt(pos.x, pos.y, pos.z - a) == 109) return Rotation.NORTH
        return null
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentPatterns = ArrayList()
        scanned = false
        renderRotation = null
        rPos = ArrayList()
    }
}