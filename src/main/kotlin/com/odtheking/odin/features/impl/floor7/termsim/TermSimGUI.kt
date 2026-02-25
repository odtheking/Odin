package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSounds
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.PlayerEquipment
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

open class TermSimGUI(
    val name: String,
    val size: Int,
    private val inv: SimpleContainer = SimpleContainer(size)
) : ContainerScreen(
    ChestMenu(
        if (size <= 9) MenuType.GENERIC_9x1
        else if (size <= 18) MenuType.GENERIC_9x2
        else if (size <= 27) MenuType.GENERIC_9x3
        else if (size <= 36) MenuType.GENERIC_9x4
        else if (size <= 45) MenuType.GENERIC_9x5
        else MenuType.GENERIC_9x6,
        0, Inventory(mc.player!!, PlayerEquipment(mc.player!!)), inv, size / 9
    ),
    Inventory(mc.player!!, PlayerEquipment(mc.player!!)),
    Component.literal(name)
) {
    val blackPane = ItemStack(Items.BLACK_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    val guiInventorySlots get() = menu?.slots?.subList(0, size) ?: emptyList()
    private var doesAcceptClick = true
    protected var ping = 0L
    private var syncId = 0

    open fun create() {
        guiInventorySlots.forEach { it.setSlot(blackPane) }
    }

    fun open(terminalPing: Long = 0L) {
        schedule(0) {
            mc.setScreen(this)
            create()
            ping = terminalPing
        }
    }

    open fun slotClick(slot: Slot, button: Int) {}

    internal fun TerminalHandler.onComplete() {
        PacketEvent.Receive(ClientboundContainerClosePacket(-2)).postAndCatch()
        TerminalEvent.Solve(this).postAndCatch()
        StartGUI.open(ping)
    }

    override fun onClose() {
        doesAcceptClick = true
        super.onClose()
    }

    override fun slotClicked(slot: Slot?, slotId: Int, button: Int, actionType: ClickType) {
        if (GuiEvent.SlotClick(this, slotId, button).postAndCatch()) return
        slot?.let { delaySlotClick(it, button) }
    }

    fun clickIndex(index: Int, button: Int) {
        val slot = guiInventorySlots.getOrNull(index) ?: return
        delaySlotClick(slot, button)
    }

    private fun delaySlotClick(slot: Slot, button: Int) {
        if (mc.screen == StartGUI) return slotClick(slot, button)
        if (!doesAcceptClick || slot.container != inv || slot.item?.item == Items.BLACK_STAINED_GLASS_PANE) return
        if (ping <= 0L) return slotClick(slot, button)
        doesAcceptClick = false
        schedule((ping / 50).toInt().coerceAtLeast(0)) {
            doesAcceptClick = true
            if (mc.screen == this) slotClick(slot, button)
        }
    }

    protected fun createNewGui(block: (Slot) -> ItemStack) {
        PacketEvent.Receive(ClientboundOpenScreenPacket(syncId++, MenuType.GENERIC_9x3, Component.literal(name))).postAndCatch()
        guiInventorySlots.forEach { it.setSlot(block(it)) }
    }

    protected fun Slot.setSlot(stack: ItemStack) {
        GuiEvent.SlotUpdate(mc.screen ?: return, ClientboundContainerSetSlotPacket(-2, 0, index, stack), menu).postAndCatch()
        set(stack)
    }

    protected fun playTermSimSound() {
        if (!TerminalSounds.enabled || !TerminalSounds.clickSounds) playSoundAtPlayer(
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            1f, 1f
        )
    }
}