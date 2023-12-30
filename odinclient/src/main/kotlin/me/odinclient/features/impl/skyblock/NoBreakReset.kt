package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/**
 * @see me.odinclient.mixin.mixins.MixinPlayerControllerMP
 */
object NoBreakReset : Module(
    name = "No Break Reset",
    category = Category.SKYBLOCK,
    description = "Prevents lore updates from resetting your breaking progress",
    tag = TagType.NEW

) {

    fun isHittingPositionHook(pos: BlockPos, cir: CallbackInfoReturnable<Boolean>, currentItemHittingBlock: ItemStack, currentBlock: BlockPos) {
        if (enabled) {
            val itemStack = mc.thePlayer.heldItem
            val lore = itemStack?.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8)?.toString()
            if (lore != null) {
                if (lore.contains("GAUNTLET") || lore.contains("DRILL") || lore.contains("PICKAXE")) {
                    cir.setReturnValue(pos == currentBlock && (itemStack.item === currentItemHittingBlock.item))
                }
            }
        } else {
            cir.setReturnValue(cir.returnValue)
        }
    }

}