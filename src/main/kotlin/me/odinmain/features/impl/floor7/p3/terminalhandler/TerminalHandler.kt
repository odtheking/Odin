package me.odinmain.features.impl.floor7.p3.terminalhandler

import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

open class TerminalHandler(val type: TerminalTypes) {
    val solution: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    val items: Array<ItemStack?> = arrayOfNulls(type.windowSize)
    val timeOpened = System.currentTimeMillis()
    var isClicked = false
    var windowCount = 1

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onPacketReceived(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is S2FPacketSetSlot -> {
                if (func_149173_d() !in 0 until type.windowSize) return@with
                items[func_149173_d()] = func_149174_e()
                if (handleSlotUpdate(this)) TerminalEvent.Updated(this@TerminalHandler).postAndCatch()
            }
            is S2DPacketOpenWindow -> {
                isClicked = false
                items.fill(null)
                windowCount++
            }
        }
    }

    init {
        @Suppress("LeakingThis")
        MinecraftForge.EVENT_BUS.register(this)
    }

    open fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean = false

    open fun simulateClick(slotIndex: Int, clickType: ClickType) {}

    fun click(slotIndex: Int, clickType: ClickType, simulateClick: Boolean = true) {
        if (simulateClick) simulateClick(slotIndex, clickType)
        isClicked = true
        PlayerUtils.windowClick(slotIndex, clickType)
    }

    fun canClick(slotIndex: Int, button: Int, needed: Int = solution.count { it == slotIndex }): Boolean = when {
        type == TerminalTypes.MELODY -> slotIndex.equalsOneOf(16, 25, 34, 43)
        slotIndex !in solution -> false
        type == TerminalTypes.NUMBERS && slotIndex != solution.firstOrNull() -> false
        type == TerminalTypes.RUBIX && ((needed < 3 && button == 1) || (needed.equalsOneOf(3, 4) && button != 1)) -> false
        else -> true
    }
}