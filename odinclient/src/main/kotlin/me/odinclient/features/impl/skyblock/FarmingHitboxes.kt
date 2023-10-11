package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import net.minecraft.block.Block
import net.minecraft.block.BlockCocoa

object FarmingHitboxes : Module(
    name = "Farming Hitboxes",
    category = Category.SKYBLOCK,
    description = "Expands the hitbox of some crops to a full block.",
    tag = TagType.NEW
) {

    val mushroom: Boolean by BooleanSetting(name = "Mushroom", default = true)
    private val cocoa: Boolean by BooleanSetting(name = "Cocoa", default = true)

    fun setBlockBoundsMixin(block: Block): Boolean
    {
        if (this.enabled)
        {
            if (cocoa && block is BlockCocoa) { block.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f); return true }
        }
        return false
    }

}