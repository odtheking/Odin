package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.events.BlockInteractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.PlayerHeadBlock

object NoItemPlace : Module(
    name = "No Item Place",
    description = "Prevents placing block items while playing SkyBlock."
) {
    init {
        on<BlockInteractEvent> {
            if (!LocationUtils.isInSkyblock || !enabled) return@on

            val heldItem = mc.player?.mainHandItem?.item as? BlockItem ?: return@on
            if (heldItem.block is PlayerHeadBlock) return@on

            cancel()
        }
    }
}

