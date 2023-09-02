package me.odinclient.utils.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.AsyncUtils
import me.odinclient.utils.VecUtils.floored
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ItemUtils.getItemIndexInContainerChest
import me.odinclient.utils.skyblock.ItemUtils.getItemSlot
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.GuiOpenEvent


object PlayerUtils {

    /**
     * Right-clicks the next tick
     */
    fun rightClick() {
        KeyBinding.onTick(100 - mc.gameSettings.keyBindUseItem.keyCode) // Simple way of making completely sure the right-clicks are sent at the same time as vanilla ones.
    }

    /**
     * Left-clicks the next tick
     */
    fun leftClick() {
        KeyBinding.onTick(-100) // Simple way of making completely sure the left-clicks are sent at the same time as vanilla ones.
    }

    fun dropItem() {
        mc.thePlayer.dropOneItem(false)
    }

    fun useItem(item: String, swapBack: Boolean = true) {
        val inventory = mc.thePlayer.inventory
        val index = getItemSlot(item) ?: return modMessage("Couldn't find $item")
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

    fun clipTo(pos: Vec3) {
        mc.thePlayer.setPosition(pos.xCoord + 0.5, pos.yCoord, pos.zCoord + 0.5)
    }

    fun clipTo(x: Double, y: Double, z: Double) {
        mc.thePlayer.setPosition(x + 0.5, y, z + 0.5)
    }


    fun windowClick(windowId: Int, slotId: Int, button: Int, mode: Int) {
        mc.playerController.windowClick(windowId, slotId, button, mode, mc.thePlayer)
    }

    private fun windowClick(slot: Int, button: Int) {
        windowClick(mc.thePlayer.inventoryContainer.windowId, slot, button, 2)
    }

    fun leftClickWindow(windowId: Int, index : Int) {
        windowClick(windowId, index, 0, 0)
    }

    fun shiftClickWindow(windowId: Int, index : Int) {
        windowClick(windowId, index, 0, 1)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun clickItemInContainer(containerName: String, itemName: String, event: GuiOpenEvent, contains: Boolean = true) {
        if (event.gui !is GuiChest) return

        val container = (event.gui as GuiChest).inventorySlots
        if (container !is ContainerChest) return

        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (!chestName.contains(containerName)) return

        GlobalScope.launch {
            val deferred = AsyncUtils.waitUntilLastItem(container)
            try {
                deferred.await()
            } catch (e: Exception) {
                return@launch
            }

            val index = getItemIndexInContainerChest(container, itemName, contains)
                ?: return@launch modMessage("§cCouldn't find §f$itemName!")

            windowClick(container.windowId, index, 2, 3)
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

            val index = getItemSlot(itemName, contains) ?: return@launch modMessage("§cCouldn't find §f$itemName!")

            println("found item at index $index, clicking...")

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
        mc.ingameGUI.run {
            displayTitle(title, null, 10, 250, 10)
            displayTitle(null, "", 10, 250, 10)
            displayTitle(null, null, 10, 250, 10)
        }
    }

    val posFloored
        get() = mc.thePlayer.positionVector.floored()

    fun getFlooredPlayerCoords(): Vec3i = mc.thePlayer.positionVector.floored()

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ
}