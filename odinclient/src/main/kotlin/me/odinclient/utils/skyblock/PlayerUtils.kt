package me.odinclient.utils.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGui
import me.odinmain.utils.AsyncUtils.waitUntilLastItem
import me.odinmain.utils.VecUtils.floored
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.ItemUtils.getItemIndexInContainerChest
import me.odinmain.utils.skyblock.ItemUtils.getItemSlot
import me.odinmain.utils.skyblock.ItemUtils.itemID
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.GuiOpenEvent


object PlayerUtils {

    /**
     * Right-clicks the next tick
     */
    fun rightClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode) // Simple way of making completely sure the right-clicks are sent at the same time as vanilla ones.
    }

    /**
     * Left-clicks the next tick
     */
    fun leftClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Simple way of making completely sure the left-clicks are sent at the same time as vanilla ones.
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
        middleClickWindow(itemIndex)
        mc.playerController?.clickBlock(blockPos, mc.objectMouseOver.sideHit)
        middleClickWindow(itemIndex)
        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = currentHeldItemStack
    }

    fun swapToItem(name: String, contains: Boolean = false) {
        val index = getItemSlot(name, contains) ?: return modMessage("Couldn't find $name")
        if (index !in 0..8) return modMessage("Couldn't find $name")
        swapToIndex(index)
    }

    fun swapToIndex(index: Int) {
        KeyBinding.onTick(mc.gameSettings.keyBindsHotbar[index].keyCode)
    }

    fun clipTo(pos: Vec3) {
        mc.thePlayer.setPosition(pos.xCoord + 0.5, pos.yCoord, pos.zCoord + 0.5)
    }

    fun clipTo(x: Double, y: Double, z: Double) {
        mc.thePlayer.setPosition(x + 0.5, y, z + 0.5)
    }

    sealed class ClickType {
        data object Left : ClickType()
        data object Right : ClickType()
        data object Middle : ClickType()
    }

    private data class WindowClick(val slotId: Int, val button: Int, val mode: Int)

    private val windowClickQueue = mutableListOf<WindowClick>()

    init {
        // Used to clear the click queue every 500ms, to make sure it isn't getting filled up.
        Executor(delay = 500) { windowClickQueue.clear() }.register()
    }

    fun windowClick(slotId: Int, button: Int, mode: Int) {
        if (mc.currentScreen is TermSimGui) {
            val gui = mc.currentScreen as TermSimGui
            gui.slotClick(gui.inventorySlots.getSlot(slotId), button)
        } else windowClickQueue.add(WindowClick(slotId, button, mode))
    }

    fun handleWindowClickQueue() {
        if (mc.thePlayer?.openContainer == null) return windowClickQueue.clear()
        if (windowClickQueue.isEmpty()) return
        windowClickQueue.first().apply {
            try {
                sendWindowClick(slotId, button, mode)
            } catch (e: Exception) {
                println("Error sending window click: $this")
                e.printStackTrace()
                windowClickQueue.clear()
            }
        }
        windowClickQueue.removeFirstOrNull()
    }

    private fun sendWindowClick(slotId: Int, button: Int, mode: Int) {
        mc.thePlayer.openContainer?.let {
            if (it !is ContainerChest) return@let
            mc.playerController.windowClick(it.windowId, slotId, button, mode, mc.thePlayer)
        }
    }

    private fun middleClickWindow(slot: Int) {
        windowClick(slot, 2, 2)
    }

    fun windowClick(slotId: Int, clickType: ClickType) {
        when (clickType) {
            is ClickType.Left -> windowClick(slotId, 0, 0)
            is ClickType.Right -> windowClick(slotId, 1, 0)
            is ClickType.Middle -> windowClick(slotId, 2, 3)
        }
    }

    fun shiftClickWindow(index : Int) {
        windowClick(index, 0, 1)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun clickItemInContainer(containerName: String, itemName: String, event: GuiOpenEvent, contains: Boolean = true) {
        if (event.gui !is GuiChest) return

        val container = (event.gui as GuiChest).inventorySlots
        if (container !is ContainerChest) return

        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (!chestName.contains(containerName)) return

        GlobalScope.launch {
            val deferred = waitUntilLastItem(container)
            try {
                deferred.await()
            } catch (e: Exception) {
                return@launch
            }

            val index = getItemIndexInContainerChest(container, itemName, contains)
                ?: return@launch modMessage("§cCouldn't find §f$itemName!")

            windowClick(index, 2, 3)
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
            val deferred = waitUntilLastItem(container)
            try {
                deferred.await()
            } catch (e: Exception) {
                println("Promise rejected")
                return@launch
            }

            println("last item loaded!")

            val index = getItemSlot(itemName, contains) ?: return@launch modMessage("§cCouldn't find §f$itemName!")

            println("found item at index $index, clicking...")

            windowClick(
                index,
                2,
                3
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

    fun EntityPlayerSP?.isHolding(vararg names: String, ignoreCase: Boolean = false, mode: Int = 0): Boolean {
        val regex = Regex("${if (ignoreCase) "(?i)" else ""}${names.joinToString("|")}")
        return this.isHolding(regex, mode)
    }

    fun EntityPlayerSP?.isHolding(regex: Regex, mode: Int = 0): Boolean {
        return this.isHolding { it?.run {
            when (mode) {
                0 -> displayName.contains(regex) || itemID.matches(regex)
                1 -> displayName.contains(regex)
                2 -> itemID.matches(regex)
                else -> false
            } } == true
        }
    }

    private fun EntityPlayerSP?.isHolding(predicate: (ItemStack?) -> Boolean): Boolean {
        if (this == null) return false
        return predicate(this.heldItem)
    }

    val posFloored
        get() = mc.thePlayer.positionVector.floored()

    fun getFlooredPlayerCoords(): Vec3i = mc.thePlayer.positionVector.floored()

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ
}