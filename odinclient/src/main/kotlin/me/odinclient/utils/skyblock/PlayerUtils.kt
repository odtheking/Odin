package me.odinclient.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGui
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.Vec3


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

    fun dropAll() {
        mc.thePlayer.dropOneItem(true)
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
        data object Shift : ClickType()
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
            gui.delaySlotClick(gui.inventorySlots.getSlot(slotId), button)
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
            is ClickType.Shift -> windowClick(slotId, 0, 1)
        }
    }
}