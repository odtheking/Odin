package me.odinclient.features.impl.qol

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import net.minecraft.block.Block
import net.minecraft.block.BlockCocoa

object FarmingHitboxes : Module(
        name = "Farming Hitboxes",
        category = Category.QOL,
        description = "Expands the hitbox of some crops to a full block"
) {

    var mushroom: Boolean by BooleanSetting(name = "Cocoa", default = true)
    private var cocoa: Boolean by BooleanSetting(name = "Cocoa", default = true)

    fun setBlockBoundsMixin(block: Block): Boolean
    {
        if (this.enabled)
        {
            if (cocoa && block is BlockCocoa) { block.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f); return true }
        }
        return false
    }

}