package com.odtheking.odin.features.impl.boss.termsim

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.ScreenCloseEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.boss.TerminalSounds
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.PlayerEquipment
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput
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
    protected val guiInventorySlots get() = menu.slots.subList(0, size)
    protected var ping = 0L

    open fun create() {
        setSlots { blackPane }
    }

    fun open(terminalPing: Long = 0L) {
        mc.setScreen(this)
        create()
        ping = terminalPing
    }

    open fun slotClick(slot: Slot, button: Int) {
        playTermSimSound()
    }

    internal fun TerminalHandler.onComplete() {
        ScreenCloseEvent.postAndCatch()
        TerminalEvent.Solve(this).postAndCatch()
        StartGUI.open(ping)
    }

    override fun onClose() {
        super.onClose()
    }

    override fun slotClicked(slot: Slot, i: Int, j: Int, clickType: ContainerInput) {
        if (GuiEvent.SlotClick(this, i, j).postAndCatch()) return
        slot?.let { delaySlotClick(it, j) }
    }

    fun clickIndex(index: Int, button: Int) {
        val slot = guiInventorySlots.getOrNull(index) ?: return
        delaySlotClick(slot, button)
    }

    private fun delaySlotClick(slot: Slot, button: Int) {
        if (mc.screen == StartGUI) return slotClick(slot, button)
        if (slot.container != inv || slot.item.item == Items.BLACK_STAINED_GLASS_PANE) return
        if (ping <= 0L) return slotClick(slot, button)
        schedule((ping / 50).toInt().coerceAtLeast(0)) {
            if (mc.screen == this) slotClick(slot, button)
        }
    }

    protected fun setSlots(block: (Slot) -> ItemStack) {
        guiInventorySlots.forEach { it.setSlot(block(it)) }
    }

    protected fun Slot.setSlot(stack: ItemStack) {
        menu.setItem(index, menu.incrementStateId(), stack)
    }

    protected fun playTermSimSound() {
        if (!TerminalSounds.enabled || !TerminalSounds.clickSounds) playSoundAtPlayer(
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            1f, 1f
        )
    }
}