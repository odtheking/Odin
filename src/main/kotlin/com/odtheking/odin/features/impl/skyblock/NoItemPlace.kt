package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.events.BlockInteractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.level.block.PlayerHeadBlock

object NoItemPlace : Module(
    name = "No Item Place",
    description = "Prevents placing block items while playing SkyBlock."
) {
    init {
        on<BlockInteractEvent> {
            if (!LocationUtils.isInSkyblock) return@on

            val heldItem = mc.player?.mainHandItem?.item as? BlockItem ?: return@on
            if (heldItem.block is PlayerHeadBlock) return@on

            val itemId = BuiltInRegistries.ITEM.getKey(heldItem).path
            
            // For TNT: block animation but send packet to server for ability
            if (itemId.contains("tnt")) {
                val hitResult = mc.hitResult as? BlockHitResult ?: return@on
                // Send packet to server so ability works
                mc.player?.connection?.send(
                    ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0)
                )
                // Block local animation
                cancel()
            }
            // For flowers: don't block (ability must work)
            else if (itemId.contains("flower") || itemId.contains("tulip") || itemId.contains("dandelion") || itemId.contains("allium")) {
                return@on
            }
            // For other block items: block test
            else {
                cancel()
            }
        }
    }
}























