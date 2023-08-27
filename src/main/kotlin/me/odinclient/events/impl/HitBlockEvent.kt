package me.odinclient.events.impl

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class HitBlockEvent(val blockPos: BlockPos, val face: EnumFacing) : Event()