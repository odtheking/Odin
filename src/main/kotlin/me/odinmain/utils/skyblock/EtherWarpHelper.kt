package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.*
import me.odinmain.utils.render.RenderUtils.renderVec
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sign

object EtherWarpHelper {
    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?) {
        inline val vec: Vec3? get() = pos?.let { Vec3(it) }
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
    fun getEtherPos(pos: Vec3, yaw: Float, pitch: Float, distance: Double = 60.0, returnEnd: Boolean = false): EtherPos {
        mc.thePlayer ?: return EtherPos.NONE

        val startPos: Vec3 = getPositionEyes(pos)
        val endPos = getLook(yaw = yaw, pitch = pitch).normalize().multiply(factor = distance).add(startPos)

        return traverseVoxels(startPos, endPos).takeUnless { it == EtherPos.NONE && returnEnd } ?: EtherPos(true, endPos.toBlockPos())
    }

    fun getEtherPos(positionLook: PositionLook = PositionLook(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), distance: Double =  56.0 + mc.thePlayer.heldItem.getTunerBonus): EtherPos {
        return getEtherPos(positionLook.pos, positionLook.yaw, positionLook.pitch, distance)
    }

    /**
     * Traverses voxels from start to end and returns the first non-air block it hits.
     * @author Bloom
     */
    private fun traverseVoxels(start: Vec3, end: Vec3): EtherPos {
        val direction = end.subtract(start)
        val step = IntArray(3) { sign(direction[it]).toInt() }
        val invDirection = DoubleArray(3) { if (direction[it] != 0.0) 1.0 / direction[it] else Double.MAX_VALUE }
        val tDelta = DoubleArray(3) { invDirection[it] * step[it] }
        val currentPos = IntArray(3) { floor(start[it]).toInt() }
        val endPos = IntArray(3) { floor(end[it]).toInt() }
        val tMax = DoubleArray(3) {
            val startCoord = start[it]
            abs((floor(startCoord) + max(step[it], 0) - startCoord) * invDirection[it])
        }

        repeat(1000) {
            val pos = BlockPos(currentPos[0], currentPos[1], currentPos[2])
            if (getBlockIdAt(pos) != 0) return EtherPos(isValidEtherWarpBlock(pos), pos)
            if (currentPos.contentEquals(endPos)) return EtherPos.NONE

            val minIndex = if (tMax[0] <= tMax[1])
                if (tMax[0] <= tMax[2]) 0 else 2
            else
                if (tMax[1] <= tMax[2]) 1 else 2

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