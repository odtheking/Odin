package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.*
import me.odinclient.utils.skyblock.PlayerUtils.clipTo
import me.odinclient.utils.waitUntilPacked
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.puzzlesolvers.IceFillSolver
import me.odinmain.features.impl.dungeon.puzzlesolvers.IceFillSolver.currentPatterns
import me.odinmain.features.impl.dungeon.puzzlesolvers.IceFillSolver.transformTo
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.PlayerUtils.posFloored
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import me.odinmain.utils.skyblock.getBlockIdAt
import net.minecraft.util.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoIceFill: Module(
    name = "Auto Ice Fill",
    description = "Automatically completes the ice fill puzzle."
) {
    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!IceFillSolver.scanned) return
        val pos = posFloored
        if (!pos.y.equalsOneOf(70, 71, 72) || getBlockIdAt(BlockPos(pos.x, pos.y - 1, pos.z )) != 79) return
        val floorIndex = pos.y % 70
        GlobalScope.launch {
            //move(Vec3(pos.x.toDouble(), pos.y - 1.0, pos.z.toDouble()), currentPatterns[floorIndex], rotation, floorIndex)
        }
    }

    private suspend fun move(pos: Vec3, pattern: List<Vec3i>, rotation: Rotations, floorIndex: Int) {
        val x = mc.thePlayer.posX
        val y = mc.thePlayer.posY - 1
        val z = mc.thePlayer.posZ

        val deferred1 = waitUntilPacked(x, y, z)
        try {
            deferred1.await()
        } catch (e: Exception) {
            return
        }
        val (bx, bz) = Vec2(0,0) //transform(pattern[0].x, pattern[0].z, rotation)
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
        val (bx2, bz2) = Vec2(0,0) //transform(pattern[pattern.size - 1].x, pattern[pattern.size - 1].z, rotation)
        val deferred = waitUntilPacked(x + bx2, y, z + bz2)
        try {
            deferred.await()
        } catch (e: Exception) {
            return
        }
        clipToNext(pos, rotation, bx2, bz2, floorIndex + 1)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun clipToNext(pos: Vec3, rotation: Rotations, bx: Int, bz: Int, floorIndex: Int) {
        val x = pos.xCoord
        val y = pos.yCoord
        val z = pos.zCoord
        val (nx, ny) = when (rotation) {
            Rotations.WEST -> Pair(0.5f, 0f)
            Rotations.EAST -> Pair(-0.5f, 0f)
            Rotations.SOUTH -> Pair(0f, 0.5f)
            else -> Pair(0f, -0.5f)
        }
        clipTo(x + bx + nx, y + 1.5, z + bz + ny)
        GlobalScope.launch {
            delay(100)
            clipTo(x + bx + nx * 2, y + 2, z + bz + ny * 2)
            delay(150)
            clipTo(x + bx + nx * 4, y + 2, z + bz + ny * 4)
            delay(150)
            move(pos + transformTo(Vec3i(bx, 0, bz), rotation), currentPatterns[floorIndex], rotation, floorIndex)
        }
    }
}