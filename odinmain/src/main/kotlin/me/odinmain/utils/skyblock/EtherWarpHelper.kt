package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.*
import me.odinmain.utils.render.RenderUtils.renderVec
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.*

object EtherWarpHelper {
    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?) {
        val vec: Vec3? get() = pos?.let { Vec3(it) }
        companion object {
            val NONE = EtherPos(false, null)
        }
    }
    var etherPos: EtherPos = EtherPos.NONE

    /**
     * Gets the position of an entity in the "ether" based on the player's view direction.
     *
     * @param pos The initial position of the entity.
     * @param yaw The yaw angle representing the player's horizontal viewing direction.
     * @param pitch The pitch angle representing the player's vertical viewing direction.
     * @return An `EtherPos` representing the calculated position in the "ether" or `EtherPos.NONE` if the player is not present.
     */
    fun getEtherPos(pos: Vec3, yaw: Float, pitch: Float, distance: Double = 60.0): EtherPos {
        mc.thePlayer ?: return EtherPos.NONE

        val startPos: Vec3 = getPositionEyes(pos)
        val endPos = getLook(yaw = yaw, pitch = pitch).normalize().multiply(factor = distance).add(startPos)

        return traverseVoxels(startPos, endPos)
    }

    fun getEtherPos(positionLook: PositionLook = PositionLook(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), distance: Double = 60.0): EtherPos {
        return getEtherPos(positionLook.pos, positionLook.yaw, positionLook.pitch, distance)
    }

    /**
     * Traverses voxels from start to end and returns the first non-air block it hits.
     * @author Bloom
     */
    private fun traverseVoxels(start: Vec3, end: Vec3): EtherPos {
        val direction = end.subtract(start)
        val step = DoubleArray(3) { sign(direction.get(it)) }
        val invDirection = DoubleArray(3) { 1.0 / direction.get(it) }
        val tDelta = DoubleArray(3) { invDirection[it] * step[it] }
        val tMax = DoubleArray(3) {
            val startCoord = start.get(it)
            abs((floor(startCoord) + max(step[it], 0.0) - startCoord) * invDirection[it])
        }

        val currentPos = DoubleArray(3) { floor(start.get(it)) }
        val endPos = DoubleArray(3) { floor(end.get(it)) }
        var iterations = 0

        while (iterations++ < 1000) {
            val pos = BlockPos(currentPos[0].toInt(), currentPos[1].toInt(), currentPos[2].toInt())
            if (getBlockIdAt(pos) != 0) return EtherPos(isValidEtherWarpBlock(pos), pos)

            if (currentPos.contentEquals(endPos)) break // reached end

            val minIndex = tMax.indices.minByOrNull { tMax[it] } ?: 0
            tMax[minIndex] += tDelta[minIndex]
            currentPos[minIndex] += step[minIndex]
        }

        return EtherPos.NONE
    }
    
    /**
     * Checks if the block at the given position is a valid block to etherwarp onto.
     * @author Bloom
     */
    private fun isValidEtherWarpBlock(pos: BlockPos): Boolean {
        // Checking the actual block to etherwarp ontop of
        // Can be at foot level, but not etherwarped onto directly.
        if (getBlockAt(pos).registryName in validEtherwarpFeetBlocks || getBlockAt(pos.up(1)).registryName !in validEtherwarpFeetBlocks) return false

        return getBlockAt(pos.up(2)).registryName in validEtherwarpFeetBlocks
    }

    private val validEtherwarpFeetBlocks = setOf(
        "minecraft:air",
        "minecraft:fire",
        "minecraft:carpet",
        "minecraft:skull",
        "minecraft:lever",
        "minecraft:stone_button",
        "minecraft:wooden_button",
        "minecraft:torch",
        "minecraft:string",
        "minecraft:tripwire_hook",
        "minecraft:tripwire",
        "minecraft:rail",
        "minecraft:activator_rail",
        "minecraft:snow_layer",
        "minecraft:carrots",
        "minecraft:wheat",
        "minecraft:potatoes",
        "minecraft:nether_wart",
        "minecraft:pumpkin_stem",
        "minecraft:melon_stem",
        "minecraft:redstone_torch",
        "minecraft:redstone_wire",
        "minecraft:red_flower",
        "minecraft:yellow_flower",
        "minecraft:sapling",
        "minecraft:flower_pot",
        "minecraft:deadbush",
        "minecraft:tallgrass",
        "minecraft:ladder",
        "minecraft:double_plant",
        "minecraft:unpowered_repeater",
        "minecraft:powered_repeater",
        "minecraft:unpowered_comparator",
        "minecraft:powered_comparator",
        "minecraft:web",
        "minecraft:waterlily",
        "minecraft:water",
        "minecraft:lava",
        "minecraft:torch",
        "minecraft:vine",
    )
}