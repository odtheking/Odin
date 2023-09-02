package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.DualSetting
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SuperBoom : Module(
    name = "Super Boom",
    description = "Places TNT when you left click on a block which can be blown up.",
    category = Category.DUNGEON,
) {
    private val behavior: Boolean by DualSetting("Behavior", "Place", "Switch")

    private var lastclick = 0L
    @SubscribeEvent
    fun onMouseInput(event: MouseEvent) {
        if (
            event.button != 0 ||
            event.buttonstate ||
            System.currentTimeMillis() - lastclick < 2000 ||
            mc.currentScreen != null ||
            mc.thePlayer?.heldItem?.displayName?.contains("TNT") == true ||
            !DungeonUtils.inDungeons
        ) return
        val lookingAt = mc.objectMouseOver ?: return
        if (lookingAt.typeOfHit != net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) return
        val blockState = mc.theWorld?.getBlockState(lookingAt.blockPos) ?: return
        val block = blockState.block
        if (
            block.damageDropped(blockState) != 2 &&
            !block.localizedName.contains("Stone Brick Stairs") &&
            !block.localizedName.contains("Stone Slab") &&
            !block.localizedName.contains("Barrier")
        ) return
        val superboomIndex = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it?.displayName?.contains("TNT") == true } ?: return

        if (!behavior) {
            if (superboomIndex < 9) {
                mc.thePlayer.inventory.currentItem = superboomIndex
            }
            return
        }
        if (superboomIndex > 8) {
            PlayerUtils.hitWithItemFromInv(superboomIndex, lookingAt.blockPos)
            return
        }
        val previousItemIndex = mc.thePlayer.inventory.currentItem

        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(superboomIndex))
        mc.thePlayer.inventory.currentItem = superboomIndex
        mc.playerController.clickBlock(lookingAt.blockPos, lookingAt.sideHit)
        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(previousItemIndex))
        mc.thePlayer.inventory.currentItem = previousItemIndex

        lastclick = System.currentTimeMillis()
    }
}
