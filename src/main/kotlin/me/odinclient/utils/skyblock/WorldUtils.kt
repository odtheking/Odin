package me.odinclient.utils.skyblock

import me.odinclient.OdinClient.Companion.mc
import net.minecraft.block.Block.getIdFromBlock
import net.minecraft.util.BlockPos

object WorldUtils {

    fun getBlockIdAt(blockpos: BlockPos): Int? {
        return if (mc.theWorld?.getBlockState(blockpos)?.block == null) null
        else getIdFromBlock(mc.theWorld?.getBlockState(blockpos)?.block)
    }

    fun isAir(blockpos: BlockPos): Boolean {
        return getIdFromBlock(mc.theWorld?.getBlockState(blockpos)?.block) == 0
    }

    fun getBlockIdAt(x: Double, y: Double, z: Double): Int = getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)

    fun getBlockIdAt(x: Int, y: Int, z: Int): Int = getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
}