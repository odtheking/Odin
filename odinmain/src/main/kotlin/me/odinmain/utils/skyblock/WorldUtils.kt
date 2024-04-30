package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

/**
 * Retrieves the block ID at the specified `BlockPos` in the Minecraft world.
 *
 * @param blockpos The position in the world to query for the block ID.
 * @return The block ID as an `Int`, or `null` if the block at the given position is not present.
 */
fun getBlockIdAt(blockpos: BlockPos): Int? {
    return if (mc.theWorld?.getBlockState(blockpos)?.block == null) null
    else Block.getIdFromBlock(mc.theWorld?.getBlockState(blockpos)?.block)
}

/**
 * Checks if the block at the specified `BlockPos` is considered "air" in the Minecraft world.
 *
 * @param blockPos The position in the world to check for an air block.
 * @return `true` if the block at the given position is air, `false` otherwise.
 */
fun isAir(blockPos: BlockPos): Boolean {
    return Block.getIdFromBlock(mc.theWorld?.getBlockState(blockPos)?.block) == 0
}

/**
 * Checks if the block at the specified `BlockPos` is considered "air" in the Minecraft world.
 *
 * @param x The x-coordinate of the position to query.
 * @param y The y-coordinate of the position to query.
 * @param z The z-coordinate of the position to query.
 * @return `true` if the block at the given position is air, `false` otherwise.
 */
fun isAir(x: Int, y: Int, z: Int): Boolean {
    return Block.getIdFromBlock(mc.theWorld?.getBlockState(BlockPos(x, y, z))?.block) == 0
}

/**
 * Retrieves the block ID at the specified coordinates in the Minecraft world.
 *
 * @param x The x-coordinate of the position to query.
 * @param y The y-coordinate of the position to query.
 * @param z The z-coordinate of the position to query.
 * @return The block ID as an `Int`.
 */
fun getBlockIdAt(x: Double, y: Double, z: Double): Int {
    return Block.getIdFromBlock(mc.theWorld?.getBlockState(BlockPos(x, y, z))?.block)
}

/**
 * Retrieves the block ID at the specified integer coordinates in the Minecraft world.
 *
 * @param x The x-coordinate of the position to query.
 * @param y The y-coordinate of the position to query.
 * @param z The z-coordinate of the position to query.
 * @return The block ID as an `Int`.
 */
fun getBlockIdAt(x: Int, y: Int, z: Int): Int {
    return Block.getIdFromBlock(mc.theWorld?.getBlockState(BlockPos(x, y, z))?.block)
}

/**
 * Retrieves the block object at the specified position
 *
 * @param pos The position to query.
 * @return The block as a `Block`.
 */
fun getBlockAt(pos: BlockPos): Block =
    mc.theWorld?.getBlockState(pos)?.block ?: Blocks.air

/**
 * Retrieves the block object at the specified integer coordinates.
 *
 * @param x The x-coordinate of the position to query.
 * @param y The y-coordinate of the position to query.
 * @param z The z-coordinate of the position to query.
 * @return The block as a `Block`.
 */
fun getBlockAt(x: Int, y: Int, z: Int): Block =
    getBlockAt(BlockPos(x, y, z))

val Block.blockBounds: AxisAlignedBB
    get() = AxisAlignedBB(blockBoundsMinX, blockBoundsMinY, blockBoundsMinZ, blockBoundsMaxX, blockBoundsMaxY, blockBoundsMaxZ)