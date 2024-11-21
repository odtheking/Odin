package me.odinmain.events.impl

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

abstract class SecretPickupEvent : Event() {
    data class Interact(val blockPos: BlockPos, val blockState: IBlockState) : SecretPickupEvent()
    data class Item(val entity: EntityItem) : SecretPickupEvent()
    data class Bat(val packet: S29PacketSoundEffect) : SecretPickupEvent()
}