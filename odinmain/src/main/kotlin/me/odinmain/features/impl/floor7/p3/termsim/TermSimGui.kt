package me.odinmain.features.impl.floor7.p3.termsim


import me.odinmain.OdinMain.display
import me.odinmain.config.Config
import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.getRandom
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.round
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

open class TermSimGui(val name: String, val size: Int, private val inv: InventoryBasic = InventoryBasic(name, true, size)) : GuiChest(
    Minecraft.getMinecraft().thePlayer.inventory,
    inv
) {
    val pane: Item = Item.getItemById(160)
    val blackPane = ItemStack(pane, 1, 15).apply { setStackDisplayName("") }
    private var inventoryBefore = arrayOf<ItemStack>()
    private var startTime = 0L
    protected var ping = 0L
    private var consecutive = 0L
    private var doesAcceptClick = true
    private val minecraft get() = Minecraft.getMinecraft() // this is needed here for some fucking reason and I have no clue but the OdinMain one is sometimes (but not always) null when open() runs ???? (this took like 30 minutes to figure out)

    open fun create() {
        this.inventorySlots.inventorySlots.subList(0, size).forEach { it.putStack(blackPane) } // override
    }

    fun open(ping: Long = 0L, const: Long = 0L) {
        create()
        minecraft.thePlayer?.inventory?.mainInventory?.let {
            inventoryBefore = it.clone()
        }
        for (i in minecraft.thePlayer?.inventory?.mainInventory?.indices ?: 0..0) {
            minecraft.thePlayer?.inventory?.mainInventory?.set(i, null)
        }
        this.ping = ping
        this.consecutive = const - 1
        display = this
        startTime = System.currentTimeMillis()
        GuiEvent.GuiLoadedEvent(name, inventorySlots as ContainerChest).postAndCatch()
    }

    fun solved(name: String, oldPb: NumberSetting<Double>) {
        val time = ((System.currentTimeMillis() - startTime) / 1000.0).round(2)
        if (time < oldPb.value) {
            modMessage("§a$name §7solved in §6$time §7(§d§lNew PB§r§7)! Old PB was §8${oldPb.value.round(2)}s§7.")
            oldPb.value = time
            Config.save()
        } else modMessage("§a$name solved in §6${time}s §a!")
        resetInv()
        if (this.consecutive > 0) openTerminal(ping, consecutive) else StartGui.open(ping)
    }

    open fun slotClick(slot: Slot, button: Int) {}

    override fun onGuiClosed() {
        resetInv()
        doesAcceptClick = true
        super.onGuiClosed()
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketSentEvent) {
        val packet = event.packet
        if (packet !is C0EPacketClickWindow || mc.currentScreen != this) return
        delaySlotClick(this.inventorySlots.inventorySlots[packet.slotId], packet.usedButton)
        event.isCanceled = true
    }

    protected fun resetInv() {
        if (inventoryBefore.isNotEmpty())
            minecraft.thePlayer.inventory.mainInventory = inventoryBefore
        inventoryBefore = arrayOf()
    }

    fun delaySlotClick(slot: Slot, button: Int) {
        if (!doesAcceptClick || slot.inventory != this.inv) return
        slotClick(slot, button)
        doesAcceptClick = false
        runIn((ping / 50).toInt()) {
            doesAcceptClick = true
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

fun openTerminal(ping: Long = 0L, const: Long = 0L) {

    when (listOf(TerminalTypes.PANES, TerminalTypes.RUBIX, TerminalTypes.ORDER, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT).random()) {
        TerminalTypes.PANES -> CorrectPanes.open(ping, const)
        TerminalTypes.RUBIX -> Rubix.open(ping, const)
        TerminalTypes.ORDER -> InOrder.open(ping, const)
        TerminalTypes.STARTS_WITH -> StartsWith(StartsWith.letters.shuffled().first()).open(ping, const)
        TerminalTypes.SELECT -> SelectAll(EnumDyeColor.entries.getRandom().name.replace("_", " ").uppercase()).open(ping, const)
        TerminalTypes.NONE -> {}
    }
}