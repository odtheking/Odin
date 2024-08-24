package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.runIn
import me.odinmain.OdinMain.mc
import me.odinclient.utils.skyblock.PlayerUtils
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

    private val placeDelay: Long by NumberSetting("Place Delay", 10L, 1, 20, unit = "ticks", description = "Placing Superboom delay in ticks.")
    private val anyBlock: Boolean by BooleanSetting("Any Block", default = false, description = "Place Superboom on any block.")

    @SubscribeEvent
    fun onLeftClick(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK || (!anyBlock && !isLookingAtCrackedStoneBricks(event.pos))) return

        val superboomSlot = getItemSlot("Superboom TNT") ?: return

        schedulePlace(superboomSlot)
    }

    private fun schedulePlace(superboomSlot: Int) {
        // Instantly swap to Superboom
        runIn(3) {
            mc.thePlayer.inventory.currentItem = superboomSlot
        }

        // Delay for placing the item
        runIn(placeDelay.toInt()) {
            triggerRightClick()
        }
    }

    private fun isLookingAtCrackedStoneBricks(pos: BlockPos): Boolean {
        val blockId = getBlockIdAt(pos)
        return blockId == 98 && mc.theWorld.getBlockState(pos).block.getMetaFromState(mc.theWorld.getBlockState(pos)) == 2
    }

    private fun triggerRightClick() {
        PlayerUtils.rightClick()
    }
}
