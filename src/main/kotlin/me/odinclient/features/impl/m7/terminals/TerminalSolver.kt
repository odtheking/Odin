package me.odinclient.features.impl.m7.terminals

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.DrawSlotEvent
import me.odinclient.events.impl.GuiLoadedEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.impl.m7.terminals.TerminalSolver.solution
import me.odinclient.features.settings.AlwaysActive
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.nvg.TextAlign
import me.odinclient.utils.render.gui.nvg.drawNVG
import me.odinclient.utils.render.gui.nvg.text
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Solves terminals in f7/m7",
    category = Category.M7
) {
    private val terminalNames = listOf(
        "Correct all the panes!",
        //"Large Chest",
        "Change all to same color!",
        "Click in order!",
        "What starts with",
        "Select all the"
    )
    private var currentTerm = -1
    private var solution = listOf<ItemStack>()

    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        currentTerm = terminalNames.indexOfFirst { it.startsWith(event.name) }
        if (currentTerm == -1) return
        modMessage("solving terminal ${event.name}")
        val items = event.gui.inventory.subList(0, event.gui.inventory.size - 37)
        when (currentTerm) {
            0 -> solvePanes(items)
            1 -> solveColor(items)
            /*
            2 -> solveNumbers(items)
            3 -> solveStartsWith(items)
            4 -> solveSelect(items)

             */
        }
    }

    @SubscribeEvent
    fun onSlotRender(event: DrawSlotEvent) {
        if (event.container !is ContainerChest || currentTerm == -1) return
        if (event.container.lowerChestInventory.displayName.unformattedText != terminalNames[currentTerm]) return
        if (event.slot.stack !in solution) return
        if (currentTerm == 0) {
            Gui.drawRect(
                event.slot.xDisplayPosition,
                event.slot.yDisplayPosition,
                event.slot.xDisplayPosition + 16,
                event.slot.yDisplayPosition + 16,
                Color(255, 0, 0).rgba
            )
            event.isCanceled = true
        } else if (currentTerm == 1) {
            val needed = solution.count { it == event.slot.stack }
            val text = if (needed < 3) needed.toString() else (needed - 5).toString()

            GlStateManager.translate(0f, 0f, 600f)
            mc.fontRendererObj.drawString(
                text,
                event.slot.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(text) / 2,
                event.slot.yDisplayPosition + 5,
                Color(200, 200, 200).rgba
            )
            GlStateManager.translate(0f, 0f, -600f)
        }
    }

    private fun solvePanes(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 && Item.getIdFromItem(it.item) == 160 }.filterNotNull()
    }

    private val colorOrder = listOf(1, 4, 13, 11, 14)
    private fun solveColor(items: List<ItemStack>) {
        val panes = items.filter { it.metadata != 15 && Item.getIdFromItem(it.item) == 160 }.toMutableList()

        val most = colorOrder.maxByOrNull { color -> panes.count { it.metadata == color } } ?: 1

        solution = panes.flatMap { pane ->
            if (pane.metadata != most) {
                val index = dist(colorOrder.indexOf(pane.metadata), colorOrder.indexOf(most))
                Array(index) { pane }.toList()
            } else {
                emptyList()
            }
        }
    }

    private fun dist(pane: Int, most: Int): Int =
        if (pane > most)
            (most + colorOrder.size) - pane
        else
            most - pane
}