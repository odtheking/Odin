package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

/**
 * Retrieves the block ID at the specified `BlockPos` in the Minecraft world.
 *
 * @param blockPos The position in the world to query for the block ID.
 * @return The block ID as an `Int`, or `null` if the block at the given position is not present.
 */
fun getBlockIdAt(blockPos: BlockPos): Int? {
    return Block.getIdFromBlock(getBlockStateAt(blockPos).block ?: return null)
}

/**
 * Checks if the block at the specified `BlockPos` is considered "air" in the Minecraft world.
 *
 * @param blockPos The position in the world to query.
 * @return `true` if the block at the given position is air, `false` otherwise.
 */
fun isAir(blockPos: BlockPos): Boolean =
    getBlockAt(blockPos) == Blocks.air

/**
 * Retrieves the block at the specified `BlockPos` in the Minecraft world.
 *
 * @param pos The position in the world to query for the block.
 * @return The block at the given position, or `Blocks.air` if the block is not present.
 */
fun getBlockAt(pos: BlockPos): Block =
    mc.theWorld?.chunkProvider?.provideChunk(pos.x shr 4, pos.z shr 4)?.getBlock(pos) ?: Blocks.air

/**
 * Retrieves the block state at the specified `BlockPos` in the Minecraft world.
 *
 * @param pos The position in the world to query for the block state.
 * @return The block state at the given position, or the default state of `Blocks.air` if the block is not present.
 */
fun getBlockStateAt(pos: BlockPos): IBlockState =
    mc.theWorld?.chunkProvider?.provideChunk(pos.x shr 4, pos.z shr 4)?.getBlockState(pos) ?: Blocks.air.defaultState