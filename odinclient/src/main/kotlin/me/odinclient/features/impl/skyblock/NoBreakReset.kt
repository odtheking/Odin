package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.skyblock.lore
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
    @JvmStatic
    fun isHittingPositionHook(pos: BlockPos, cir: CallbackInfoReturnable<Boolean>, itemHittingBlock: ItemStack?, currentBlock: BlockPos) {
        if (!enabled || mc.thePlayer.heldItem == null || itemHittingBlock == null) return cir.setReturnValue(cir.returnValue)
        val stack = mc.thePlayer.heldItem
        val lore = stack.lore.toString()
        if (lore.containsOneOf("GAUNTLET", "DRILL", "PICKAXE")) {
            cir.setReturnValue(pos == currentBlock && (stack.item === itemHittingBlock.item))
        }
    }
}
