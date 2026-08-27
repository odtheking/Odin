package com.odtheking.odin.utils.skyblock.dungeon.terminals

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.features.impl.boss.termsim.TermSimGUI
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack

object TerminalUtils {

    private val termSolverRegex = Regex("^(.{1,16}) activated a terminal! \\((\\d)/(\\d)\\)$")
    private var lastClickTime = 0L

    @JvmStatic var currentTerm: TerminalHandler? = null
        private set
    var lastTermOpened: TerminalHandler? = null
        private set

    init {
        on<ScreenEvent.Open> (EventPriority.HIGHEST) {
            TerminalTypes.openHandler(screen.title.string)?.let {
                devMessage("§aNew terminal: §6${it.type.name}")
                currentTerm = it
                TerminalEvent.Open(it).postAndCatch()
                lastTermOpened = it
            }
        }

        on<ScreenCloseEvent> {
            currentTerm?.let {
                devMessage("§cLeft terminal: §6${it.type.name}")
                currentTerm = null
                TerminalEvent.Close(it).postAndCatch()
            }
        }

        on<SetSlotEvent> {
            if (menu !== (mc.screen as? AbstractContainerScreen<*>)?.menu) return@on
            currentTerm?.updateSlot(this)
            currentTerm?.isClicked = false
        }

        on<GuiEvent.SlotClick> {
            lastClickTime = System.currentTimeMillis()
            currentTerm?.isClicked = true
        }

        on<GuiEvent.SlotClick> (EventPriority.HIGH) {
            lastClickTime = System.currentTimeMillis()
            currentTerm?.isClicked = true
        }

        on<TickEvent.End> {
            currentTerm?.let {
                if (System.currentTimeMillis() - lastClickTime >= TerminalSolver.terminalReloadThreshold && it.isClicked) {
                    SetSlotEvent(0, ItemStack.EMPTY, emptyList(), mc.player!!.inventoryMenu).postAndCatch()
                    it.isClicked = false
                }
            }
        }

        on<ChatPacketEvent> {
            termSolverRegex.find(value)?.let { message ->
                if (message.groupValues[1] == mc.player?.name?.string) lastTermOpened?.let {
                    TerminalEvent.Solve(it).postAndCatch()
                }
            }
        }

        onSend<ServerboundContainerClickPacket> (EventPriority.LOW) {
            val termSimScreen = mc.screen as? TermSimGUI ?: return@onSend
            if (containerInput != ContainerInput.PICKUP_ALL) termSimScreen.clickIndex(slotNum.toInt(), buttonNum.toInt())
            it.cancel()
        }

        onReceive<ClientboundContainerSetSlotPacket> (EventPriority.HIGH) {
            val termSimScreen = mc.screen as? TermSimGUI ?: return@onReceive
            if (slot !in 0 until termSimScreen.size) return@onReceive
            item.let { item -> mc.player?.inventoryMenu?.setItem(slot, stateId, item) }
            it.cancel()
        }
    }
}