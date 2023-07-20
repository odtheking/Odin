package me.odinclient.utils.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.mixin.MinecraftAccessor
import me.odinclient.utils.AsyncUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ItemUtils.getItemIndexInContainerChest
import me.odinclient.utils.skyblock.ItemUtils.getItemIndexInInventory
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.EntityLivingBase
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.GuiOpenEvent
import kotlin.math.floor

object PlayerUtils {

    fun rightClick() =
        (mc as MinecraftAccessor).invokeRightClickMouse()

    fun leftClick() =
        (mc as MinecraftAccessor).invokeClickMouse()

    fun dropItem() {
        mc.thePlayer.dropOneItem(false)
    }

    fun useItem(item: String, swapBack: Boolean = true) {
        val inventory = mc.thePlayer.inventory
        val index = ItemUtils.getItemSlot(item)
        if (index !in 0..8) return modMessage("Couldn't find $item")

        val prevItem = inventory.currentItem
        inventory.currentItem = index
        rightClick()
        if (swapBack) inventory.currentItem = prevItem
    }

    fun hitWithItemFromInv(itemIndex: Int, blockPos: BlockPos) {
        if (itemIndex < 8) return
        val currentHeldItemStack = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem)
        windowClick(itemIndex, 2)
        mc.playerController?.clickBlock(blockPos, mc.objectMouseOver.sideHit)
        windowClick(itemIndex, 2)
        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = currentHeldItemStack
    }

    fun interactWithEntity(entity: EntityLivingBase) {
        val packet = C02PacketUseEntity(entity, C02PacketUseEntity.Action.INTERACT)
        mc.thePlayer.sendQueue.addToSendQueue(packet)
    }

    fun getFlooredPlayerCoords(): Vec3i? =
        if (mc.thePlayer == null) null
        else mc.thePlayer.positionVector.floored()
        //else Vec3i(floor(mc.thePlayer.posX), floor(mc.thePlayer.posY), floor(mc.thePlayer.posZ))

    fun Vec3.floored() = Vec3i(floor(this.xCoord), floor(this.yCoord), floor(this.zCoord))

    fun clipTo(pos: Vec3) =
        mc.thePlayer.setPosition(pos.xCoord + 0.5, pos.yCoord, pos.zCoord + 0.5)

    fun clipTo(x: Double, y: Double, z: Double) =
        mc.thePlayer.setPosition(x + 0.5, y, z + 0.5)

    private fun windowClick(slot: Int, mode: Int) =
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, mode, 2, mc.thePlayer)

    @OptIn(DelicateCoroutinesApi::class)
    fun clickItemInContainer(containerName: String, itemName: String, event: GuiOpenEvent, contains: Boolean = true) {
        if (event.gui !is GuiChest) return
        val container = (event.gui as GuiChest).inventorySlots
        if (container !is ContainerChest) return
        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (!chestName.contains(containerName)) return

        GlobalScope.launch{
            val deferred = AsyncUtils.waitUntilLastItem(container)
            try {
                deferred.await()
            } catch (e: Exception) {
                return@launch
            }
            val index = getItemIndexInContainerChest(itemName, container, contains)
            if (index == -1) {
                modMessage("§cCouldn't find §f$itemName!")
                return@launch
            }

            mc.playerController.windowClick(
                mc.thePlayer.openContainer.windowId,
                index,
                2,
                3,
                mc.thePlayer
            )
            mc.thePlayer.closeScreen()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun clickItemInInventory(containerName: String, itemName: String, event: GuiOpenEvent, contains: Boolean = true) {
        if (event.gui !is GuiChest) return
        val container = (event.gui as GuiChest).inventorySlots
        if (container !is ContainerChest) return
        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (!chestName.contains(containerName)) return

        GlobalScope.launch{
            val deferred = AsyncUtils.waitUntilLastItem(container)
            try {
                deferred.await()
            } catch (e: Exception) {
                println("Promise rejected")
                return@launch
            }
            println("last item loaded!")
            val index = getItemIndexInInventory(itemName, contains)
            if (index == -1) {
                modMessage("§cCouldn't find §f$itemName!")
                return@launch
            } else println("found item at index $index, clicking...")

            mc.playerController.windowClick(
                mc.thePlayer.openContainer.windowId,
                index,
                2,
                3,
                mc.thePlayer
            )
           mc.thePlayer.closeScreen()
        }
    }

    fun alert(title: String, playSound: Boolean = true) {
        if (playSound) mc.thePlayer.playSound("note.pling", 100f, 1f)
        val gui = mc.ingameGUI
        gui.displayTitle(title, null, 10, 250, 10)
        gui.displayTitle(null, "", 10, 250, 10)
        gui.displayTitle(null, null, 10, 250, 10)
    }

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ
}