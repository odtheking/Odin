package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
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
    description = "Places a superboom when you left-click.",
    category = Category.DUNGEON,
    tag = TagType.RISKY
) {
    private val minecraft: Minecraft = Minecraft.getMinecraft()

    private val swapDelay: Long by NumberSetting("Swap Delay", 10L, 1, 20, unit = "ticks", description = "Superboom swapping delay in ticks.")
    private val placeDelay: Long by NumberSetting("Place Delay", 10L, 1, 20, unit = "ticks", description = "Placing Superboom delay in ticks.")
    private val anyBlock: Boolean by BooleanSetting("Any Block", default = false, description = "Place Superboom on any block.")

    @SubscribeEvent
    fun onLeftClick(event: PlayerInteractEvent) {
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            if (anyBlock || isLookingAtCrackedStoneBricks(event.pos)) {
                val superboomSlot = findSuperboomSlot()
                if (superboomSlot != -1) {
                    val swapDelayMillis = swapDelay * 50
                    val placeDelayMillis = placeDelay * 50

                    Timer().schedule(swapDelayMillis) {
                        minecraft.thePlayer.inventory.currentItem = superboomSlot

                        Timer().schedule(placeDelayMillis) {
                            triggerRightClick()
                        }
                    }
                } else {
                    minecraft.thePlayer.addChatMessage(ChatComponentText("No item named 'Superboom TNT' could be found."))
                }
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
        return -1
    }

    private fun isLookingAtCrackedStoneBricks(pos: BlockPos): Boolean {
        val block: Block = minecraft.theWorld.getBlockState(pos).block
        return block is BlockStoneBrick && block.getMetaFromState(minecraft.theWorld.getBlockState(pos)) == BlockStoneBrick.CRACKED_META
    }

    private fun triggerRightClick() {
        KeyBinding.onTick(minecraft.gameSettings.keyBindUseItem.keyCode)
    }
}
