package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraft.block.Block

object FarmingHitboxes : Module(
    name = "Farming Hitboxes",
    category = Category.SKYBLOCK,
    description = "Expands the hitbox of some crops to a full block.",
    tag = TagType.NEW
) {

    fun setFullBlock(block: Block) {
        block.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f)
    }

}