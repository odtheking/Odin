package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.events.BlockInteractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.LocationUtils
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

            val heldItemStack = mc.player?.mainHandItem ?: return@on
            val heldItem = heldItemStack.item as? BlockItem ?: return@on
            if (heldItem.block is PlayerHeadBlock) return@on

            val hitResult = mc.hitResult as? BlockHitResult ?: return@on
            mc.player?.connection?.send(
                ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0)
            )
            cancel()
        }
    }
}























