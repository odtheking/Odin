package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.block.Block
import net.minecraft.block.BlockStoneBrick
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Timer
import kotlin.concurrent.schedule

object AutoBoom : Module(
    name = "Auto Superboom",
    description = "Places a superboom when you left-click cracked stone bricks. (has a delay so no ban)",
    category = Category.DUNGEON
) {
    private val minecraft: Minecraft = Minecraft.getMinecraft()

    @SubscribeEvent
    fun onLeftClick(event: PlayerInteractEvent) {
        // Check if the action is left-click on block and if it's cracked stone bricks
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && isLookingAtCrackedStoneBricks(event.pos)) {
            val superboomSlot = findSuperboomSlot()
            if (superboomSlot != -1) {
                // First, switch to the Superboom TNT slot after 1 second
                Timer().schedule(1000) {
                    minecraft.thePlayer.inventory.currentItem = superboomSlot

                    // Then, trigger the right-click after another 1 second
                    Timer().schedule(1000) {
                        triggerRightClick()
                    }
                }
            } else {
                minecraft.thePlayer.addChatMessage(ChatComponentText("No item named 'Superboom TNT' could be found."))
            }
        }
    }

    private fun findSuperboomSlot(): Int {
        val inventory = minecraft.thePlayer.inventory.mainInventory
        for (i in inventory.indices) {
            val itemStack: ItemStack? = inventory[i]
            if (itemStack != null && itemStack.displayName.contains("Superboom TNT", true)) {
                return i
            }
        }
        return -1 // Not found
    }

    private fun isLookingAtCrackedStoneBricks(pos: BlockPos): Boolean {
        val block: Block = minecraft.theWorld.getBlockState(pos).block
        return block is BlockStoneBrick && block.getMetaFromState(minecraft.theWorld.getBlockState(pos)) == BlockStoneBrick.CRACKED_META
    }

    private fun triggerRightClick() {
        KeyBinding.onTick(minecraft.gameSettings.keyBindUseItem.keyCode)
    }
}
