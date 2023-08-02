package me.odinclient.events

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

class BlockUpdateEvent(var pos: BlockPos, var state: IBlockState) : Event()