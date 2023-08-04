package me.odinclient.events

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.mixin.MixinWorld.onsetBlockState
 */
class BlockUpdateEvent(var pos: BlockPos, var state: IBlockState) : Event()