package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.runIn
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.block.Block
import net.minecraft.block.BlockStoneBrick
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoBoom : Module(
    name = "Auto Superboom",
    description = "Places a superboom when you left-click.",
    category = Category.DUNGEON,
    tag = TagType.RISKY
) {
    private val minecraft: Minecraft = Minecraft.getMinecraft()

    private val placeDelay: Long by NumberSetting("Place Delay", 10L, 1, 20, unit = "ticks", description = "Placing Superboom delay in ticks.")
    private val anyBlock: Boolean by BooleanSetting("Any Block", default = false, description = "Place Superboom on any block.")

    @SubscribeEvent
    fun onLeftClick(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK || (!anyBlock && !isLookingAtCrackedStoneBricks(event.pos))) return

        val superboomSlot = getItemSlot("Superboom TNT") ?: return minecraft.thePlayer.addChatMessage(ChatComponentText("No item named 'Superboom TNT' could be found."))

        schedulePlace(superboomSlot)
    }

    private fun schedulePlace(superboomSlot: Int) {
        // Instantly swap to Superboom
        runIn(3) {
            minecraft.thePlayer.inventory.currentItem = superboomSlot
        }

        // Delay for placing the item
        runIn(placeDelay.toInt()) {
            triggerRightClick()
        }
    }

    private fun isLookingAtCrackedStoneBricks(pos: BlockPos): Boolean {
        val block: Block = minecraft.theWorld.getBlockState(pos).block
        return block is BlockStoneBrick && block.getMetaFromState(minecraft.theWorld.getBlockState(pos)) == BlockStoneBrick.CRACKED_META
    }

    private fun triggerRightClick() {
        PlayerUtils.rightClick()
    }
}
