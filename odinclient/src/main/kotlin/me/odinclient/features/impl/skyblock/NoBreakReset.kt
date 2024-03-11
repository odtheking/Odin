package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.skyblock.lore
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos

/**
 * @see me.odinclient.mixin.mixins.MixinPlayerControllerMP
 */
object NoBreakReset : Module(
    name = "No Break Reset",
    category = Category.SKYBLOCK,
    description = "Prevents lore updates from resetting your breaking progress."

) {
    @JvmStatic
    fun isHittingPositionHook(blockPos: BlockPos, currentItemHittingBlock: ItemStack?, currentBlock: BlockPos): Boolean {
        var flag: Boolean
        val itemStack: ItemStack? = mc.thePlayer?.heldItem
        flag = currentItemHittingBlock == null && itemStack == null
        if (currentItemHittingBlock != null && itemStack != null) {
            if (enabled && itemStack.tagCompound != null) {
                val lore: String = itemStack.lore.toString()
                if (lore.containsOneOf("GAUNTLET", "DRILL", "PICKAXE")) {
                    return blockPos == currentBlock &&
                            itemStack.item === currentItemHittingBlock.item
                }
            }
            flag = itemStack.item === currentItemHittingBlock.item && ItemStack.areItemStackTagsEqual(
                itemStack,
                currentItemHittingBlock
            ) && (itemStack.isItemStackDamageable || itemStack.metadata == currentItemHittingBlock.metadata)
        }
        return blockPos == currentBlock && flag
    }
}
