package me.odinclient.features.impl.m7.p3

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.DrawSlotEvent
import me.odinclient.events.impl.GuiClosedEvent
import me.odinclient.events.impl.GuiLoadedEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.AlwaysActive
import me.odinclient.utils.render.Color
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
        "Change all to same color!",
        "Click in order!",
        "What starts with",
        "Select all the"
    )
    private var currentTerm = -1
    private var solution = listOf<Int>()

    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        currentTerm = terminalNames.indexOfFirst { it.startsWith(event.name) }
        if (currentTerm == -1) return
        val items = event.gui.inventory.subList(0, event.gui.inventory.size - 37)
        when (currentTerm) {
            0 -> solvePanes(items)
            1 -> solveColor(items)
            2 -> solveNumbers(items)
            3 -> {
                val letter = Regex("What starts with: '(\\w+)'?").find(event.gui.lowerChestInventory.displayName.unformattedText)?.groupValues?.get(1) ?: return
                solveStartsWith(items, letter)
            }
            4 -> {
                val color = Regex("Select all the (\\w+) items!").find(event.gui.lowerChestInventory.displayName.unformattedText)?.groupValues?.get(1) ?: return
                solveSelect(items, color.lowercase())
            }
        }
    }

    @SubscribeEvent
    fun onSlotRender(event: DrawSlotEvent) {
        if (event.container !is ContainerChest || currentTerm == -1 || !enabled) return
        if (event.slot.inventory.name != terminalNames[currentTerm]) return
        if (event.slot.slotIndex !in solution) {
            /*if (currentTerm == 3 /*&& removeWrong*/) {
                Gui.drawRect(
                    event.slot.xDisplayPosition,
                    event.slot.yDisplayPosition,
                    event.slot.xDisplayPosition + 16,
                    event.slot.yDisplayPosition + 16,
                    Color(45, 45, 45, 1f).rgba
                )
                event.isCanceled = true
            }
             */
            return
        }
        GlStateManager.translate(0f, 0f, 600f)
        when (currentTerm) {
            1 -> {
                val needed = solution.count { it == event.slot.slotIndex }
                val text = if (needed < 3) needed.toString() else (needed - 5).toString()

                mc.fontRendererObj.drawString(
                    text,
                    event.slot.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(text) / 2,
                    event.slot.yDisplayPosition + 5,
                    Color(200, 200, 200).rgba
                )
            }
            2 -> {
                val index = solution.indexOf(event.slot.slotIndex)
                if (index < 3) {
                    Gui.drawRect(
                        event.slot.xDisplayPosition,
                        event.slot.yDisplayPosition,
                        event.slot.xDisplayPosition + 16,
                        event.slot.yDisplayPosition + 16,
                        Color(0, 0, 170, 2f / (index + 3)).rgba
                    )
                }

                val amount = event.slot.stack?.stackSize ?: 0
                mc.fontRendererObj.drawString(
                    amount.toString(),
                    event.slot.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(amount.toString()) / 2,
                    event.slot.yDisplayPosition + 5,
                    Color(200, 200, 200).rgba
                )

                event.isCanceled = true
            }
            3 -> {
                Gui.drawRect(
                    event.slot.xDisplayPosition,
                    event.slot.yDisplayPosition,
                    event.slot.xDisplayPosition + 16,
                    event.slot.yDisplayPosition + 16,
                    Color(0, 170, 170, 0.5f).rgba
                )
                event.isCanceled = true
            }
            4 -> {
                Gui.drawRect(
                    event.slot.xDisplayPosition,
                    event.slot.yDisplayPosition,
                    event.slot.xDisplayPosition + 16,
                    event.slot.yDisplayPosition + 16,
                    Color(0, 170, 170, 0.5f).rgba
                )
                event.isCanceled = true
            }
        }
        GlStateManager.translate(0f, 0f, -600f)
    }

    @SubscribeEvent
    fun onGuiClosed(event: GuiClosedEvent) {
        currentTerm = -1
        solution = emptyList()
    }

    private fun solvePanes(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 && Item.getIdFromItem(it.item) == 160 }.filterNotNull().map { items.indexOf(it) }
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
        }.map { panes.indexOf(it) }
    }

    private fun dist(pane: Int, most: Int): Int = if (pane > most) (most + colorOrder.size) - pane else most - pane

    private fun solveNumbers(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 && Item.getIdFromItem(it.item) == 160 }.filterNotNull().sortedBy { it.stackSize }.map { items.indexOf(it) }
    }

    private fun solveStartsWith(items: List<ItemStack?>, letter: String) {
        solution = items.filter { it?.displayName?.startsWith(letter) == true }.map { items.indexOf(it) }
    }

    private val colorMap = listOf("wool" to "white", "bone" to "white", "lapis" to "blue", "ink" to "black", "cocoa" to "brown", "dandelion" to "yellow", "rose" to "red", "cactus" to "green", "light gray" to "silver")
    private fun solveSelect(items: List<ItemStack?>, color: String) {
        solution = items.filter { (colorMap.any {c -> it?.displayName?.lowercase()?.contains(c.first) == true && c.second == color } || it?.displayName?.contains(color) == true) && it?.isItemEnchanted == false }.map { items.indexOf(it) }
    }
}