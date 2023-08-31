package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraft.block.BlockCarpet
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoCarpet : Module(
    name = "No Carpet",
    category = Category.RENDER
) {

    var carpetList: ArrayList<BlockCarpet> = ArrayList()

    override fun onEnable() {
        for (carpet in carpetList)
        {
            carpet.setBlockBounds(0f, 0f, 0f, 1f, 0f, 1f)
        }

        super.onEnable()
    }

    override fun onDisable() {
        for (carpet in carpetList)
        {
            carpet.setBlockBounds(0f, 0f, 0f, 1f, 0.0625f, 1f)
        }

        super.onDisable()
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload)
    {
        carpetList.clear()
    }

    // MixinBlockCarpet#onSetBlockBoundsFromMeta

}