package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.skyblock.PlayerUtils.isHolding
import me.odinmain.utils.skyblock.hasAbility
import me.odinmain.utils.skyblock.lore
import net.minecraft.block.Block
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos

/**
 * Cancels block interactions to allow for items to be used.
 *
 * Modified from: https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/kotlin/floppaclient/module/impl/misc/CancelInteract.kt
 *
 * @author Aton
 */
object CancelInteract : Module(
    name = "Cancel Interact",
    category = Category.SKYBLOCK,
    description = "Cancels the interaction with certain blocks, so that the item can be used instead."
){
    private val cancelInteract by BooleanSetting("Cancel Interact", true, description = "Cancels the interaction with certain blocks, so that the item can be used instead.")
    private val onlyWithAbility by BooleanSetting("Only Ability", false, description = "Check whether the item has an ability before cancelling interactions.")
    private val noBreakReset by BooleanSetting("No Break Reset", false, description = "Prevents lore updates from resetting your breaking progress.")

    /**
     * Block which should always be interacted with.
     */
    private val interactionWhitelist = setOf<Block>(
        Blocks.lever, Blocks.chest, Blocks.trapped_chest,
        Blocks.stone_button, Blocks.wooden_button
    )

    /**
     * Set containing all the block which interactions should be canceled with.
     */
    private val interactionBlacklist = setOf<Block>(
        Blocks.cobblestone_wall, Blocks.oak_fence, Blocks.dark_oak_fence,
        Blocks.acacia_fence, Blocks.birch_fence, Blocks.jungle_fence,
        Blocks.nether_brick_fence, Blocks.spruce_fence, Blocks.birch_fence_gate,
        Blocks.acacia_fence_gate, Blocks.dark_oak_fence_gate, Blocks.oak_fence_gate,
        Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.hopper,
    )

    /**
     * Redirected to by the MinecraftMixin. Replaces the check for whether the targeted block is air.
     * @return true when the item's ability should be used.
     */
    fun cancelInteractHook(instance: WorldClient, blockPos: BlockPos): Boolean {
        // When the module is not enabled preform the vanilla action.
        if (cancelInteract && enabled) {
            if (interactionWhitelist.contains(instance.getBlockState(blockPos).block)) return false
            if (mc.thePlayer?.isHolding("Ender Pearl") == true) return true
            if (!onlyWithAbility || mc.thePlayer?.heldItem?.hasAbility == true)
                return interactionBlacklist.contains(instance.getBlockState(blockPos).block) || instance.isAirBlock(blockPos)
        }
        return instance.isAirBlock(blockPos)
    }

    /**
     * @see me.odinclient.mixin.mixins.MixinPlayerControllerMP
     */
    @JvmStatic
    fun isHittingPositionHook(blockPos: BlockPos, currentItemHittingBlock: ItemStack?, currentBlock: BlockPos): Boolean {
        val itemStack: ItemStack? = mc.thePlayer?.heldItem
        var flag = currentItemHittingBlock == null && itemStack == null
        if (currentItemHittingBlock != null && itemStack != null) {
            if (noBreakReset && enabled && itemStack.tagCompound != null)
                if (itemStack.lore.toString().containsOneOf("GAUNTLET", "DRILL", "PICKAXE"))
                    return blockPos == currentBlock && itemStack.item === currentItemHittingBlock.item

            flag = itemStack.item === currentItemHittingBlock.item && ItemStack.areItemStackTagsEqual(itemStack, currentItemHittingBlock)
                    && (itemStack.isItemStackDamageable || itemStack.metadata == currentItemHittingBlock.metadata)
        }
        return blockPos == currentBlock && flag
    }
}