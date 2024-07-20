package me.odinclient.features.impl.skyblock

import me.odinclient.mixin.accessors.IBlockAccessor
import me.odinmain.features.Module
import net.minecraft.block.Block

object FarmingHitboxes : Module(
    name = "Farming Hitboxes",
    description = "Expands the hitbox of some crops to a full block."
) {
    fun setFullBlock(block: Block) {
        val accessor = (block as IBlockAccessor)
        accessor.setMinX(0.0)
        accessor.setMinY(0.0)
        accessor.setMinZ(0.0)
        accessor.setMaxX(1.0)
        accessor.setMaxY(1.0)
        accessor.setMaxZ(1.0)
    }
}