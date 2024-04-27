package me.odinmain.events.impl

import net.minecraft.tileentity.TileEntityChest
import net.minecraftforge.fml.common.eventhandler.Event

open class RenderChestEvent(var chest: TileEntityChest, var x: Double, var y: Double, var z: Double, var partialTicks: Float) : Event() {

    class Pre(tileEntity: TileEntityChest, x: Double, y: Double, z: Double, partialTicks: Float) : RenderChestEvent(tileEntity, x, y, z, partialTicks)

    class Post(tileEntity: TileEntityChest, x: Double, y: Double, z: Double, partialTicks: Float) : RenderChestEvent(tileEntity, x, y, z, partialTicks)
}