package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.SetSlotEvent
import com.odtheking.odin.features.impl.boss.TerminalSimulator
import com.odtheking.odin.features.impl.boss.TerminalSolver.firstClickProt
import com.odtheking.odin.features.impl.boss.TerminalSolver.firstClickProtTicks
import com.odtheking.odin.features.impl.boss.TerminalSolver.shouldFirstClickProtWithTicks
import com.odtheking.odin.features.impl.boss.termsim.TermSimGUI
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW
import java.util.concurrent.CopyOnWriteArrayList

abstract class TerminalHandler(val type: TerminalTypes) {
    val solution: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    val timeOpened = System.currentTimeMillis()
    var isClicked = false
    var ticksOpened = -1

    open fun updateSlot(event: SetSlotEvent) {
        if (event.slots.isEmpty()) return
        if (event.slotIndex !in 0 until type.windowSize - 9 || event.itemStack.item == Items.BLACK_STAINED_GLASS_PANE) return

        solution.clear()
        solution.addAll(solve(event.slots.subList(0, type.windowSize - 9), event.slotIndex))
    }

    protected abstract fun renderSlot(slotIndex: Int): Pair<Color, String?>?

    fun getSlotRendering(slotIndex: Int): Pair<Color, String?>? =
        if (slotIndex !in solution) null else renderSlot(slotIndex)

    open fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    abstract fun solve(slots: List<Slot>, updatedIndex: Int): List<Int>

    open fun click(slotIndex: Int, button: Int, simulateClick: Boolean) {
        val screen = mc.screen ?: return
        if (simulateClick) simulateClick(slotIndex, button)
        isClicked = true

        if (screen is TermSimGUI) {
            screen.clickIndex(slotIndex, button)
            return
        }
        mc.player?.clickSlot(slotIndex, button, if (button == GLFW.GLFW_MOUSE_BUTTON_3) ContainerInput.CLONE else ContainerInput.PICKUP)
    }

    open fun canClick(slotIndex: Int, button: Int): Boolean = slotIndex in solution

    fun shouldProtect(): Boolean =
            !(TerminalSimulator.disableFirstClickProtection && mc.screen is TermSimGUI)
            && (System.currentTimeMillis() - timeOpened < firstClickProt ||
            (!LocationUtils.isCurrentArea(Island.SinglePlayer) && shouldFirstClickProtWithTicks && ticksOpened < firstClickProtTicks))
}