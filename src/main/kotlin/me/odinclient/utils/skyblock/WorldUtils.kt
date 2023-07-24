package me.odinclient.utils.skyblock

import me.odinclient.OdinClient.Companion.mc
import net.minecraft.block.Block.getIdFromBlock
import net.minecraft.util.BlockPos

object WorldUtils {

    fun getBlockIdAt(blockpos: BlockPos): Int? {
        return if (mc.theWorld?.getBlockState(blockpos)?.block == null) null
        else getIdFromBlock(mc.theWorld?.getBlockState(blockpos)?.block)
    }

    fun isAir(blockPos: BlockPos): Boolean {
        return getIdFromBlock(mc.theWorld?.getBlockState(blockPos)?.block) == 0
    }

    fun getBlockIdAt(x: Double, y: Double, z: Double): Int {
        return getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
    }

    fun getBlockIdAt(x: Int, y: Int, z: Int): Int {
        return getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
    }
}