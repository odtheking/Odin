package me.odinclient.features.impl.floor7.p3

import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.events.impl.DrawSlotEvent
import me.odinclient.events.impl.GuiClosedEvent
import me.odinclient.events.impl.GuiLoadedEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.AlwaysActive
import me.odinclient.features.settings.Setting.Companion.withDependency
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.render.Color
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ItemUtils.unformattedName
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver (WIP)",
    description = "Solves terminals in f7/m7",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val behindItem: Boolean by BooleanSetting("Behind Item", description = "Shows the item over the rendered solution")
    private val textColor: Color by ColorSetting("Text Color", Color(220, 220, 220), true)
    private val orderColor: Color by ColorSetting("Order Color", Color(0, 170, 170), true)
    private val startsWithColor: Color by ColorSetting("Starts With Color", Color(0, 170, 170), true)
    private val selectColor: Color by ColorSetting("Select Color", Color(0, 170, 170), true)
    private val cancelToolTip: Boolean by BooleanSetting("Stop Tooltips", default = true, description = "Stops rendering tooltips in terminals")
    private val removeWrong: Boolean by BooleanSetting("Stop Rendering Wrong", description = "Stops rendering wrong items in terminals")
    private val wrongColor: Color by ColorSetting("Wrong Color", Color(45, 45, 45), true).withDependency { removeWrong }
    private val removeWrongRubix: Boolean by BooleanSetting("Stop Rubix", true).withDependency { removeWrong }
    private val removeWrongOrder: Boolean by BooleanSetting("Stop Order", true).withDependency { removeWrong }
    private val removeWrongStartsWith: Boolean by BooleanSetting("Stop Starts With", true).withDependency { removeWrong }
    private val removeWrongSelect: Boolean by BooleanSetting("Stop Select", true).withDependency { removeWrong }

    private val zLevel: Float get() = if (behindItem) 200f else 999f
    private var lastLeftTerm = 0L

    private val terminalNames = listOf(
        "Correct all the panes!",
        "Change all to same color!",
        "Click in order!",
        "What starts with:",
        "Select all the"
    )
    var currentTerm = -1
    var solution = listOf<Int>()

    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        currentTerm = terminalNames.indexOfFirst { event.name.startsWith(it) }
        if (currentTerm == -1) return
        lastLeftTerm = 0L
        val items = event.gui.inventory.subList(0, event.gui.inventory.size - 37)
        when (currentTerm) {
            0 -> solvePanes(items)
            1 -> solveColor(items)
            2 -> solveNumbers(items)
            3 -> {
                val letter = Regex("What starts with: '(\\w+)'?").find(event.name)?.groupValues?.get(1) ?: return modMessage("Failed to find letter, please report this!")
                solveStartsWith(items, letter)
            }
            4 -> {
                val colorNeeded = EnumDyeColor.entries.find { event.name.contains(it.getName().replace("_", " ").uppercase()) }?.unlocalizedName ?: return modMessage("Failed to find color, please report this!")
                solveSelect(items, colorNeeded.lowercase())
            }
        }
    }

    @SubscribeEvent
    fun onSlotRender(event: DrawSlotEvent) {
        if (event.container !is ContainerChest || currentTerm == -1 || !enabled) return
        if (!event.slot.inventory.name.startsWith(terminalNames[currentTerm])) return
        if (event.slot.slotIndex !in solution) {
            val shouldCancel = when (currentTerm) {
                0 -> false
                1 -> removeWrongRubix
                2 -> removeWrongOrder
                3 -> removeWrongStartsWith
                4 -> removeWrongSelect
                else -> false
            }
            if (removeWrong && shouldCancel) {
                GlStateManager.disableLighting()
                Gui.drawRect(event.x - 1, event.y - 1, event.x + 18, event.y + 18, wrongColor.rgba)
                GlStateManager.enableDepth()
                event.isCanceled = true
            }
            return
        }
        GlStateManager.translate(0f, 0f, zLevel)
        GlStateManager.disableLighting()
        when (currentTerm) {
            1 -> {
                val needed = solution.count { it == event.slot.slotIndex }
                val text = if (needed < 3) needed.toString() else (needed - 5).toString()
                mc.fontRendererObj.drawString(text,event.x + 9 - mc.fontRendererObj.getStringWidth(text) / 2, event.y + 5, textColor.rgba)
            }
            2 -> {
                val index = solution.indexOf(event.slot.slotIndex)
                if (index < 3) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, orderColor.withAlpha(2f / (index + 3)).rgba)

                val amount = event.slot.stack?.stackSize ?: 0
                mc.fontRendererObj.drawString(amount.toString(), event.x + 9 - mc.fontRendererObj.getStringWidth(amount.toString()) / 2, event.y + 5, textColor.rgba)
                event.isCanceled = true
            }
            3 -> Gui.drawRect(event.x, event.y, event.x + if (removeWrongStartsWith && removeWrong) 18 else 16, event.y + if (removeWrongStartsWith && removeWrong) 18 else 16, startsWithColor.rgba)
            4 -> Gui.drawRect(event.x, event.y, event.x + if (removeWrongSelect && removeWrong) 18 else 16, event.y + if (removeWrongSelect && removeWrong) 18 else 16, selectColor.rgba)
        }
        GlStateManager.translate(0f, 0f, -zLevel)
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (!cancelToolTip || (currentTerm == -1 && System.currentTimeMillis() - lastLeftTerm > 100)) return
        event.toolTip.clear()
    }

    @SubscribeEvent
    fun onGuiClosed(event: GuiClosedEvent) {
        currentTerm = -1
        solution = emptyList()
        lastLeftTerm = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val match = Regex("(.+) (?:activated|completed) a terminal! \\((\\d)/(\\d)\\)").find(event.message) ?: return
        if (match.groups[1]?.value != mc.thePlayer.name) return
        currentTerm = -1
        lastLeftTerm = System.currentTimeMillis()
        solution = emptyList()
    }

    private fun solvePanes(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 }.map { items.indexOf(it) }
    }

    private val colorOrder = listOf(1, 4, 13, 11, 14)
    private fun solveColor(items: List<ItemStack?>) {
        val panes = items.filter { it?.metadata != 15 && Item.getIdFromItem(it?.item) == 160 }.filterNotNull()
        var temp = List(100) { i -> i }
        for (color in colorOrder) {
            val temp2 = panes.flatMap { pane ->
                if (pane.metadata != color) {
                    Array(dist(colorOrder.indexOf(pane.metadata), colorOrder.indexOf(color))) { pane }.toList()
                } else emptyList()
            }.map { items.indexOf(it) }

            if (getRealSize(temp2) < getRealSize(temp)) {
                temp = temp2
            }
        }
        solution = temp
    }

    private fun getRealSize(list: List<Int>): Int {
        var size = 0
        list.distinct().forEach { pane ->
            val count = list.count { it == pane }
            size += if (count >= 3) 5 - count else count
        }
        return size
    }

    private fun dist(pane: Int, most: Int): Int = if (pane > most) (most + colorOrder.size) - pane else most - pane

    private fun solveNumbers(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 && Item.getIdFromItem(it.item) == 160 }.filterNotNull().sortedBy { it.stackSize }.map { items.indexOf(it) }
    }

    private fun solveStartsWith(items: List<ItemStack?>, letter: String) {
        solution = items.filter { it?.unformattedName?.startsWith(letter, true) == true && !it.isItemEnchanted }.map { items.indexOf(it) }
    }

    private fun solveSelect(items: List<ItemStack?>, color: String) {
        solution = items.filter { it?.isItemEnchanted == false && it.unlocalizedName?.contains(color, true) == true && Item.getIdFromItem(it.item) != 160 }.map { items.indexOf(it) }
    }
}