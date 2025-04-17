package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.OdinMain
import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.impl.floor7.p3.TerminalSounds
import me.odinmain.features.impl.floor7.p3.TerminalSounds.clickSounds
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.runIn
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

open class TermSimGUI(
    val name: String,
    val size: Int,
    private val inv: InventoryBasic = InventoryBasic(name, true, size)
) : GuiChest(InventoryPlayer(mc.thePlayer), inv) {

    val pane: Item = Item.getItemById(160)
    val blackPane = ItemStack(pane, 1, 15).apply { setStackDisplayName("") }
    inline val guiInventorySlots get() = inventorySlots?.inventorySlots?.subList(0, size) ?: emptyList()
    private var doesAcceptClick = true
    protected var ping = 0L

    open fun create() {
        guiInventorySlots.forEach { it.setSlot(blackPane) } // override
    }

    fun open(terminalPing: Long = 0L) {
        display = this
        create()

        ping = terminalPing
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTerminalSolved(event: TerminalEvent.Solved) {
        if (OdinMain.mc.currentScreen !== this) return
        PacketEvent.Receive(S2EPacketCloseWindow(-2)).postAndCatch()
        StartGUI.open(ping)
    }

    open fun slotClick(slot: Slot, button: Int) {}

    override fun onGuiClosed() {
        MinecraftForge.EVENT_BUS.unregister(this)
        doesAcceptClick = true
        super.onGuiClosed()
    }

    override fun initGui() {
        MinecraftForge.EVENT_BUS.register(this)
        super.initGui()
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onPacketSend(event: PacketEvent.Send) {
        val packet = event.packet as? C0EPacketClickWindow ?: return
        if (OdinMain.mc.currentScreen !== this) return
        delaySlotClick(guiInventorySlots.getOrNull(packet.slotId) ?: return, packet.usedButton)
        event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPacketReceived(event: PacketEvent.Receive) {
        val packet = event.packet as? S2FPacketSetSlot ?: return
        if (OdinMain.mc.currentScreen !== this || packet.func_149175_c() == -2 || event.packet.func_149173_d() !in 0 until size) return
        packet.func_149174_e()?.let { mc.thePlayer?.inventoryContainer?.putStackInSlot(packet.func_149173_d(), it) }
        event.isCanceled = true
    }

    private fun delaySlotClick(slot: Slot, button: Int) {
        if (OdinMain.mc.currentScreen == StartGUI) return slotClick(slot, button)
        if (!doesAcceptClick || slot.inventory != inv || (slot.stack?.item == pane && slot.stack?.metadata == 15)) return
        doesAcceptClick = false
        runIn((ping / 50).toInt()) {
            if (OdinMain.mc.currentScreen != this) return@runIn
            doesAcceptClick = true
            slotClick(slot, button)
        }
    }

    final override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        delaySlotClick(slotUnderMouse ?: return, mouseButton)
    }

    final override fun handleMouseClick(slotIn: Slot?, slotId: Int, clickedButton: Int, clickType: Int) {
        delaySlotClick(slotIn ?: return, clickedButton)
    }

    protected fun createNewGui(block: (Slot) -> ItemStack) {
        PacketEvent.Receive(S2DPacketOpenWindow(0, "minecraft:chest", ChatComponentText(name), size)).postAndCatch()
        guiInventorySlots.forEach { it.setSlot(block(it)) }
    }

    protected fun Slot.setSlot(stack: ItemStack) {
        PacketEvent.Receive(S2FPacketSetSlot(-2, slotNumber, stack)).postAndCatch()
        putStack(stack)
    }

    protected fun playTermSimSound() {
        if (!TerminalSounds.enabled || !clickSounds) mc.thePlayer?.playSound("random.orb", 1f, 1f)
    }
}