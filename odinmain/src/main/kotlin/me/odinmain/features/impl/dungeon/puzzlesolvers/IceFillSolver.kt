package me.odinmain.features.impl.dungeon.puzzlesolvers

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.utils.addVec
import me.odinmain.utils.plus
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils.bind
import me.odinmain.utils.render.RenderUtils.worldRenderer
import me.odinmain.utils.skyblock.IceFillFloors.floors
import me.odinmain.utils.skyblock.IceFillFloors.representativeFloors
import me.odinmain.utils.skyblock.PlayerUtils.posFloored
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockIdAt
import me.odinmain.utils.skyblock.isAir
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11

object IceFillSolver {
    var scanned = false
    var currentPatterns: MutableList<List<Vec3i>> = ArrayList()
    private var renderRotation: Rotation? = null
    private var rPos: MutableList<Vec3> = ArrayList()


    private fun renderPattern(pos: Vec3i, rotation: Rotation) {
        renderRotation = rotation
        rPos.add(Vec3(pos.x + 0.5, pos.y + 0.1, pos.z + 0.5))
    }

    fun onRenderWorldLast(color: Color) {
        if (currentPatterns.size == 0 || rPos.size == 0 || DungeonUtils.currentRoomName != "Ice Fill") return
        val rotation = renderRotation ?: return

        GlStateManager.pushMatrix()
        color.bind()
        RenderUtils.preDraw()
        GlStateManager.depthMask(true)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(3f)

        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        for (i in currentPatterns.indices) {
            val pattern = currentPatterns[i]
            val startPos = rPos[i]
            worldRenderer.pos(startPos.xCoord, startPos.yCoord, startPos.zCoord).endVertex()
            for (point in pattern) {
                val pos = startPos + transformTo(point, rotation)
                worldRenderer.pos(pos.xCoord, pos.yCoord, pos.zCoord).endVertex()
            }
            val stairPos = startPos + transformTo(pattern.last().addVec(1, 1), rotation)
            worldRenderer.pos(stairPos.xCoord, stairPos.yCoord, stairPos.zCoord).endVertex()
        }

        Tessellator.getInstance().draw()
        GlStateManager.depthMask(true)
        RenderUtils.postDraw()
        GlStateManager.popMatrix()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || scanned || !DungeonUtils.inDungeons || DungeonUtils.currentRoomName != "Ice Fill") return
        val pos = posFloored
        if (pos.y != 70 || getBlockIdAt(BlockPos(pos.x, pos.y - 1, pos.z )) != 79) return
        GlobalScope.launch {
            val rotation = checkRotation(pos, 0) ?: return@launch
            if (!scan(pos, 0)) return@launch modMessage("Failed to scan floor 0")

            val a = transform(Vec3i(5, 1, 0), rotation)
            if (!scan(pos.addVec(a.x, a.y, a.z), 1)) return@launch modMessage("Failed to scan floor 1")

            val b = transform(Vec3i(12, 2, 0), rotation)
            if (!scan(pos.addVec(b.x, b.y, b.z), 2)) return@launch modMessage("Failed to scan floor 2")
            scanned = true
        }
    }

    private fun scan(pos: Vec3i, floorIndex: Int): Boolean {
        val rotation = checkRotation(pos, floorIndex) ?: return false

        val bPos = BlockPos(pos)

        val floorHeight = representativeFloors[floorIndex]
        val startTime = System.nanoTime()

        for (index in floorHeight.indices) {
            if (
                isAir(bPos.add(transform(floorHeight[index].first, rotation))) &&
                !isAir(bPos.add(transform(floorHeight[index].second, rotation)))
            ) {
                val scanTime: Double = (System.nanoTime() - startTime) / 1000000.0
                modMessage("Floor ${floorIndex + 1} scan took ${scanTime}ms")

                renderPattern(pos, rotation)
                currentPatterns.add(floors[floorIndex][index].toMutableList())
                return true
            }
        }
        return false
    }

    fun transform(vec: Vec3i, rotation: Rotation): Vec3i {
        return when (rotation) {
            Rotation.EAST -> Vec3i(vec.x, vec.y, vec.z)
            Rotation.WEST -> Vec3i(-vec.x, vec.y, -vec.z)
            Rotation.SOUTH -> Vec3i(vec.z, vec.y, vec.x)
            else -> Vec3i(vec.z, vec.y, -vec.x)
        }
    }

    fun transform(x: Int, z: Int, rotation: Rotation): Pair<Int, Int> {
        return when (rotation) {
            Rotation.EAST -> Pair(x, z)
            Rotation.WEST -> Pair(-x, -z)
            Rotation.SOUTH -> Pair(z, x)
            else -> Pair(z, -x)
        }
    }

    fun transformTo(vec: Vec3i, rotation: Rotation): Vec3 {
        return when (rotation) {
            Rotation.EAST -> Vec3(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
            Rotation.WEST -> Vec3(-vec.x.toDouble(), vec.y.toDouble(), -vec.z.toDouble())
            Rotation.SOUTH -> Vec3(vec.z.toDouble(), vec.y.toDouble(), vec.x.toDouble())
            else -> Vec3(vec.z.toDouble(), vec.y.toDouble(), -vec.x.toDouble())
        }
    }

    fun checkRotation(pos: Vec3i, floor: Int): Rotation? {
        val a = ((floor + 1) * 2) + 2
        if      (getBlockIdAt(pos.x + a, pos.y, pos.z) == 109) return Rotation.EAST
        else if (getBlockIdAt(pos.x - a, pos.y, pos.z) == 109) return Rotation.WEST
        else if (getBlockIdAt(pos.x, pos.y, pos.z + a) == 109) return Rotation.SOUTH
        else if (getBlockIdAt(pos.x, pos.y, pos.z - a) == 109) return Rotation.NORTH
        return null
    }

    fun onWorldLoad() {
        currentPatterns = ArrayList()
        scanned = false
        renderRotation = null
        rPos = ArrayList()
    }
}
enum class Rotation { EAST, WEST, SOUTH, NORTH }