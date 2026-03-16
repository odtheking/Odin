package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.google.common.primitives.Shorts
import com.google.common.primitives.SignedBytes
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min

abstract class TerminalHandler(val type: TerminalTypes) {
    val solution: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    val timeOpened = System.currentTimeMillis()
    var isClicked = false
    var windowCount = 0

    open fun updateSlot(event: GuiEvent.SlotUpdate) {
        if (event.packet.slot !in 0 until type.windowSize) return
        val items = event.menu.items.subList(0, min(event.menu.items.size, type.windowSize))

        if (canSolve(items, event.packet.slot)) {
            solution.clear()
            solution.addAll(solve(items))
        }
    }

    fun openScreen() {
        isClicked = false
        windowCount++
    }

    open fun canSolve(items: List<ItemStack>, currentIndex: Int): Boolean = currentIndex == type.windowSize - 1

    open fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    abstract fun solve(items: List<ItemStack>): List<Int>

    open fun click(slotIndex: Int, button: Int, simulateClick: Boolean) {
        val screenHandler = (mc.screen as? ContainerScreen)?.menu ?: return
        if (simulateClick) simulateClick(slotIndex, button)
        isClicked = true

        if (mc.screen is TermSimGUI) {
            PacketEvent.Send(
                ServerboundContainerClickPacket(
                    -1, -1,
                    Shorts.checkedCast(slotIndex.toLong()), SignedBytes.checkedCast(button.toLong()),
                    if (button == GLFW.GLFW_MOUSE_BUTTON_3) ClickType.CLONE else ClickType.PICKUP,
                    Int2ObjectOpenHashMap(), HashedStack.EMPTY
                )
            ).postAndCatch()
            return
        }
        mc.player?.clickSlot(screenHandler.containerId, slotIndex, button, if (button == GLFW.GLFW_MOUSE_BUTTON_3) ClickType.CLONE else ClickType.PICKUP)
    }

    open fun canClick(slotIndex: Int, button: Int): Boolean = slotIndex in solution
}