package me.odinmain.features.impl.floor7.p3.termsim


import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.config.Config
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.round
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

open class TermSimGui(val name: String, val size: Int) : GuiChest(
    mc.thePlayer.inventory,
    InventoryBasic(name, true, size)
) {
    val pane: Item = Item.getItemById(160)
    val blackPane = ItemStack(pane, 1, 15).apply { setStackDisplayName("") }
    private var startTime = 0L
    protected var ping = 0L

    open fun create() {
        this.inventorySlots.inventorySlots.subList(0, size).forEach { it.putStack(blackPane) } // override
    }

    fun open(ping: Long = 0L) {
        create()
        this.ping = ping
        display = this
        startTime = System.currentTimeMillis()
        TerminalSolver.onGuiLoad(GuiLoadedEvent(name, inventorySlots as ContainerChest))
        TerminalSolver.handlePacket(name)
    }

    fun solved(name: String, oldPb: NumberSetting<Double>) {
        val time = ((System.currentTimeMillis() - startTime) / 1000.0).round(2)
        if (time < oldPb.value) {
            modMessage("§a$name §7solved in §6$time §7(§d§lNew PB§r§7)! Old PB was §8${oldPb.value.round(2)}s§7.")
            oldPb.value = time
            Config.saveConfig()
        } else modMessage("§a$name solved in §6${time}s §a!")
        StartGui.open(ping)
    }

    open fun slotClick(slot: Slot, button: Int) {}

    fun delaySlotClick(slot: Slot, button: Int) {
        runIn((ping / 50).toInt()) {
            slotClick(slot, button)
        }
    }

    final override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val slot = slotUnderMouse ?: return
        if (slot.stack?.item == pane && slot.stack?.metadata == 15) return
        delaySlotClick(slot, mouseButton)
    }

    final override fun handleMouseClick(slotIn: Slot?, slotId: Int, clickedButton: Int, clickType: Int) {
        val slot = slotIn ?: return
        if (slot.stack?.item == pane && slot.stack?.metadata == 15 || clickedButton != 4) return
        delaySlotClick(slot, 0)
    }
}