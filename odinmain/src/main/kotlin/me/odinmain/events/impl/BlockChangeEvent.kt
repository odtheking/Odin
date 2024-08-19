package me.odinmain.events.impl

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Sent when a block has been updated.
 */
@Cancelable
class BlockChangeEvent(val pos: BlockPos, val old: IBlockState, val update: IBlockState, val world: World) : Event()