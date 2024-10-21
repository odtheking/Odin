package me.odinmain.features.impl.floor7.p3

import io.github.moulberry.notenoughupdates.NEUApi
import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termGUI.CustomTermGui
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.util.MouseUtils
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.item.*
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7.",
    category = Category.FLOOR7
) {
    val renderType by SelectorSetting("Mode", "Odin", arrayListOf("Odin", "Skytils", "SBE", "Custom GUI"), description = "How the terminal solver should render.")
    val customGuiText by SelectorSetting("Custom Gui Title", "Top Left", arrayListOf("Top Left", "Middle", "Disabled"), description = "Where the custom gui text should be rendered.").withDependency { renderType == 3 }
    val customScale by NumberSetting("Custom Scale", 1f, .8f, 2.5f, .1f, description = "Size of the Custom Terminal Gui.").withDependency { renderType == 3 }
    val textShadow by BooleanSetting("Text Shadow", true, description = "Adds a shadow to the text.")
    private val lockRubixSolution by BooleanSetting("Lock Rubix Solution", true, description = "Locks the 'correct' color of the rubix terminal to the one that was scanned first, should make the solver less 'jumpy'.")
    private val cancelToolTip by BooleanSetting("Stop Tooltips", true, description = "Stops rendering tooltips in terminals.")
    private val blockIncorrectClicks by BooleanSetting("Block Incorrect Clicks", true, description = "Blocks incorrect clicks in terminals.")
    private val cancelMelodySolver by BooleanSetting("Stop Melody Solver", false, description = "Stops rendering the melody solver.")

    private val showRemoveWrongSettings by DropdownSetting("Render Wrong Settings").withDependency { renderType == 1 }
    private val removeWrong by BooleanSetting("Stop Rendering Wrong", true, description = "Main toggle for stopping the rendering of incorrect items in terminals.").withDependency { renderType == 1 && showRemoveWrongSettings }
    private val removeWrongPanes by BooleanSetting("Stop Panes", true, description = "Stops rendering wrong panes in the panes terminal.").withDependency { renderType == 1 && showRemoveWrongSettings && removeWrong }
    private val removeWrongRubix by BooleanSetting("Stop Rubix", true, description = "Stops rendering wrong colors in the rubix terminal.").withDependency { renderType == 1 && showRemoveWrongSettings && removeWrong }
    private val removeWrongStartsWith by BooleanSetting("Stop Starts With", true, description = "Stops rendering wrong items in the starts with terminal.").withDependency { renderType == 1 && showRemoveWrongSettings && removeWrong }
    private val removeWrongSelect by BooleanSetting("Stop Select", true, description = "Stops rendering wrong items in the select terminal.").withDependency { renderType == 1 && showRemoveWrongSettings && removeWrong }
    private val removeWrongMelody by BooleanSetting("Stop Melody", true, description = "Stops rendering wrong items in the melody terminal.").withDependency { renderType == 1 && showRemoveWrongSettings && removeWrong }

    val gap: Int by NumberSetting("Gap", 10, 0, 20, 1, false, "Gap between items for the custom gui.").withDependency { renderType == 3 }
    val textScale: Int by NumberSetting("Text Scale", 1, 1, 3, increment = 1, description = "Scale of the text in the custom gui.").withDependency { renderType == 3 }

    private val showColors by DropdownSetting("Color Settings")
    private val backgroundColor by ColorSetting("Background Color", Color(45, 45, 45), true, description = "Background color of the terminal solver.").withDependency { renderType == 0 && showColors }

    val customGuiColor by ColorSetting("Custom Gui Color", ColorUtil.moduleButtonColor.withAlpha(.8f), true, description = "Color of the custom gui.").withDependency { renderType == 3 && showColors }
    val textColor by ColorSetting("Text Color", Color(220, 220, 220), true, description = "Text color of the terminal solver.").withDependency { showColors }
    val panesColor by ColorSetting("Panes Color", Color(0, 170, 170), true, description = "Color of the panes terminal solver.").withDependency { showColors }

    val rubixColor1 by ColorSetting("Rubix Color 1", Color(0, 170, 170), true, description = "Color of the rubix terminal solver for 1 click.").withDependency { showColors }
    val rubixColor2 by ColorSetting("Rubix Color 2", Color(0, 100, 100), true, description = "Color of the rubix terminal solver for 2 click.").withDependency { showColors }
    val oppositeRubixColor1 by ColorSetting("Rubix Color -1", Color(170, 85, 0), true, description = "Color of the rubix terminal solver for -1 click.").withDependency { showColors }
    val oppositeRubixColor2 by ColorSetting("Rubix Color -2", Color(210, 85, 0), true, description = "Color of the rubix terminal solver for -2 click.").withDependency { showColors }

    val orderColor by ColorSetting("Order Color 1", Color(0, 170, 170, 1f), true, description = "Color of the order terminal solver for 1st item.").withDependency { showColors }
    val orderColor2 by ColorSetting("Order Color 2", Color(0, 100, 100, 1f), true, description = "Color of the order terminal solver for 2nd item.").withDependency { showColors }
    val orderColor3 by ColorSetting("Order Color 3", Color(0, 65, 65, 1f), true, description = "Color of the order terminal solver for 3rd item.").withDependency { showColors }

    val startsWithColor by ColorSetting("Starts With Color", Color(0, 170, 170), true, description = "Color of the starts with terminal solver.").withDependency { showColors }

    val selectColor by ColorSetting("Select Color", Color(0, 170, 170), true, description = "Color of the select terminal solver.").withDependency { showColors }

    val melodyColumColor by ColorSetting("Melody Column Color", Color.PURPLE.withAlpha(0.75f), true, description = "Color of the colum indicator for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyRowColor by ColorSetting("Melody Row Color", Color.GREEN.withAlpha(0.75f), true, description = "Color of the row indicator for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyPressColumColor by ColorSetting("Melody Press Column Color", Color.YELLOW.withAlpha(0.75f), true, description = "Color of the location for pressing for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyPressColor by ColorSetting("Melody Press Color", Color.CYAN.withAlpha(0.75f), true, description = "Color of the location for pressing for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyCorrectRowColor by ColorSetting("Melody Correct Row Color", Color.WHITE.withAlpha(0.75f), true, description = "Color of the whole row for melody.").withDependency { showColors && !cancelMelodySolver }

    private val zLevel get() = if (renderType == 1 && currentTerm.equalsOneOf(TerminalTypes.STARTS_WITH, TerminalTypes.SELECT)) 100f else 400f

    data class Terminal(var type: TerminalTypes, var solution: List<Int>, var items: MutableList<ItemStack>, var timeOpened: Long = 0L)
    var currentTerm = Terminal(TerminalTypes.NONE, listOf(), mutableListOf())
    private var lastRubixSolution: Int? = null
    private var lastTermOpened = TerminalTypes.NONE

    @SubscribeEvent
    fun onGuiLoad(event: GuiEvent.GuiLoadedEvent) {
        val newTerm = TerminalTypes.entries.find { event.name.startsWith(it.guiName) } ?: TerminalTypes.NONE
        val items = event.gui.inventory.subList(0, event.gui.inventory.size - 37)
        if (newTerm != currentTerm.type) {
            currentTerm = Terminal(newTerm, listOf(), items, System.currentTimeMillis())
            lastTermOpened = currentTerm.type
            lastRubixSolution = null
        }
        if (currentTerm.type == TerminalTypes.NONE) return leftTerm()
        currentTerm.solution = when (currentTerm.type) {
            TerminalTypes.PANES -> solvePanes(items)
            TerminalTypes.RUBIX -> solveColor(items)
            TerminalTypes.ORDER -> solveNumbers(items)
            TerminalTypes.STARTS_WITH -> {
                val letter = Regex("What starts with: '(\\w+)'?").find(event.name)?.groupValues?.get(1) ?: return modMessage("Failed to find letter, please report this!")
                solveStartsWith(items, letter)
            }
            TerminalTypes.SELECT -> {
                val colorNeeded = EnumDyeColor.entries.find { event.name.contains(it.name.replace("_", " ").uppercase()) }?.unlocalizedName ?: return modMessage("Failed to find color, please report this!")
                solveSelect(items, colorNeeded.lowercase())
            }
            TerminalTypes.MELODY -> solveMelody(items)
            else -> return
        }
        if (renderType == 3 && Loader.instance().activeModList.any { it.modId == "notenoughupdates" }) NEUApi.setInventoryButtonsToDisabled()
        TerminalOpenedEvent(currentTerm.type, currentTerm.solution).postAndCatch()
    }

    @SubscribeEvent
    fun onGuiRender(event: GuiEvent.DrawGuiContainerScreenEvent) {
        if (currentTerm.type == TerminalTypes.NONE || !enabled || !renderType.equalsOneOf(0,3) || event.container !is ContainerChest || (currentTerm.type == TerminalTypes.MELODY && cancelMelodySolver)) return
        if (renderType == 3) {
            CustomTermGui.render()
            event.isCanceled = true
            return
        }
        translate(event.guiLeft.toFloat(), event.guiTop.toFloat(), 399f)
        Gui.drawRect(7, 16, event.xSize - 7, event.ySize - 96, backgroundColor.rgba)
        translate(-event.guiLeft.toFloat(), -event.guiTop.toFloat(), -399f)
    }

    private fun getShouldBlockWrong(): Boolean {
        if (renderType.equalsOneOf(0, 3)) return true
        if (!removeWrong || renderType == 2) return false
        return when (currentTerm.type) {
            TerminalTypes.PANES -> removeWrongPanes
            TerminalTypes.RUBIX -> removeWrongRubix
            TerminalTypes.ORDER -> true
            TerminalTypes.STARTS_WITH -> removeWrongStartsWith
            TerminalTypes.SELECT -> removeWrongSelect
            TerminalTypes.MELODY -> removeWrongMelody
            else -> false
        }
    }

    @SubscribeEvent
    fun drawSlot(event: GuiEvent.DrawSlotEvent) {
        if (currentTerm.type == TerminalTypes.NONE || (currentTerm.type == TerminalTypes.MELODY && cancelMelodySolver) || renderType == 3) return
        if (event.slot.slotIndex !in currentTerm.solution && event.slot.slotIndex <= event.container.inventorySlots.size - 37 && enabled && getShouldBlockWrong() && event.slot.inventory !is InventoryPlayer) event.isCanceled = true
        if (event.slot.slotIndex !in currentTerm.solution || event.slot.slotIndex > event.container.inventorySlots.size - 37 || !enabled || event.slot.inventory is InventoryPlayer) return

        translate(0f, 0f, zLevel)
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        when (currentTerm.type) {
            TerminalTypes.PANES -> Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, panesColor.rgba)

            TerminalTypes.RUBIX -> {
                val needed = currentTerm.solution.count { it == event.slot.slotIndex }
                val text = if (needed < 3) needed else (needed - 5)
                val color = when {
                    needed < 3 && text == 2 -> rubixColor2
                    needed < 3 && text == 1 -> rubixColor1
                    text == -2 -> oppositeRubixColor2
                    else -> oppositeRubixColor1
                }

                Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, color.rgba)
                mcText(text.toString(), event.x + 8f - getMCTextWidth(text.toString()) / 2, event.y + 4.5, 1, textColor, shadow = textShadow, false)
            }
            TerminalTypes.ORDER -> {
                val index = currentTerm.solution.indexOf(event.slot.slotIndex)
                if (index < 3) {
                    val color = when (index) {
                        0 -> orderColor
                        1 -> orderColor2
                        else -> orderColor3
                    }.rgba
                    Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, color)
                    event.isCanceled = true
                }
                val amount = event.slot.stack?.stackSize ?: 0
                mcText(amount.toString(), event.x + 8.5f - getMCTextWidth(amount.toString()) / 2, event.y + 4.5f, 1, textColor, shadow = textShadow, false)
            }
            TerminalTypes.STARTS_WITH ->
                if (renderType != 1 || (renderType == 1 && !removeWrong)) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, startsWithColor.rgba)

            TerminalTypes.SELECT ->
                if (renderType != 1 || (renderType == 1 && !removeWrong)) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, startsWithColor.rgba)

            TerminalTypes.MELODY -> {
                if (renderType != 1 || (renderType == 1 && !removeWrong)) {
                    val colorMelody = when {
                        event.slot.stack?.metadata == 5 && Item.getIdFromItem(event.slot.stack.item) == 160 -> melodyRowColor
                        event.slot.stack?.metadata == 2 && Item.getIdFromItem(event.slot.stack.item) == 160 -> melodyColumColor
                        else -> melodyPressColor
                    }
                    Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, colorMelody.rgba)
                }
            }
            else -> {}
        }
        GlStateManager.enableLighting()
        translate(0f, 0f, -zLevel)
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (cancelToolTip && currentTerm.type != TerminalTypes.NONE && enabled) event.toolTip.clear()
    }

    @SubscribeEvent
    fun guiClick(event: GuiEvent.GuiMouseClickEvent) {
        val gui = event.gui as? GuiChest ?: return
        if (currentTerm.type == TerminalTypes.NONE || !enabled || (currentTerm.type == TerminalTypes.MELODY && cancelMelodySolver)) return
        if (renderType == 3) {
            CustomTermGui.mouseClicked(MouseUtils.mouseX.toInt(), MouseUtils.mouseY.toInt(), event.button)
            event.isCanceled = true
            return
        }

        if (blockIncorrectClicks && currentTerm.type != TerminalTypes.MELODY) {
            val needed = currentTerm.solution.count { it == gui.slotUnderMouse?.slotIndex }
            
            event.isCanceled = when {
                gui.slotUnderMouse?.slotIndex !in currentTerm.solution -> true
                currentTerm.type == TerminalTypes.RUBIX && ((needed < 3 && event.button != 0) || (needed >= 3 && event.button != 1)) -> true
                else -> false
            }
        }
    }

    @SubscribeEvent
    fun itemStack(event: GuiEvent.DrawSlotOverlayEvent) {
        if (currentTerm.type == TerminalTypes.ORDER && enabled && (event.stack?.item?.registryName ?: return) == "minecraft:stained_glass_pane") event.isCanceled = true
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END && mc.thePlayer?.openContainer is ContainerPlayer) leftTerm()
    }

    init {
        onMessage(Regex("(.{1,16}) (?:activated|completed) a (terminal|device|lever)! \\((\\d)/(\\d)\\)")) {
            Regex("(.{1,16}) (?:activated|completed) a (terminal|device|lever)! \\((\\d)/(\\d)\\)").find(it)?.let {
                val (playerName, deviceType, completionStatus, total) = it.destructured
                TerminalSolvedEvent(if (deviceType == "terminal") lastTermOpened else TerminalTypes.NONE, playerName, completionStatus.toIntOrNull() ?: return@let, total.toIntOrNull() ?: return@let).postAndCatch()
            }
        }

        onPacket(S2FPacketSetSlot::class.java, { currentTerm.type == TerminalTypes.MELODY }) { packet ->
            packet.func_149174_e()?.let {
                val index = packet.func_149173_d()
                if (index !in 0 until currentTerm.items.size) return@onPacket
                currentTerm.items[index] = it
                currentTerm.solution = solveMelody(currentTerm.items)
            }
        }
    }

    private fun leftTerm() {
        if (currentTerm.type == TerminalTypes.NONE && currentTerm.solution.isEmpty()) return
        TerminalClosedEvent(currentTerm.type).postAndCatch()
        currentTerm.type = TerminalTypes.NONE
        currentTerm.solution = emptyList()
    }

    private fun solvePanes(items: List<ItemStack?>): List<Int> =
        items.mapIndexedNotNull { index, item -> if (item?.metadata == 14) index else null }

    private val colorOrder = listOf(1, 4, 13, 11, 14)
    private fun solveColor(items: List<ItemStack?>): List<Int> {
        val panes = items.mapNotNull { item -> if (item?.metadata != 15 && Item.getIdFromItem(item?.item) == 160) item else null }
        var temp = List(100) { i -> i }
        if (lastRubixSolution != null && lockRubixSolution) {
            temp = panes.flatMap { pane ->
                if (pane.metadata != lastRubixSolution) {
                    Array(dist(colorOrder.indexOf(pane.metadata), colorOrder.indexOf(lastRubixSolution))) { pane }.toList()
                } else emptyList()
            }.map { items.indexOf(it) }
        } else {
            for (color in colorOrder) {
                val temp2 = panes.flatMap { pane ->
                    if (pane.metadata != color) {
                        Array(dist(colorOrder.indexOf(pane.metadata), colorOrder.indexOf(color))) { pane }.toList()
                    } else emptyList()
                }.map { items.indexOf(it) }
                if (getRealSize(temp2) < getRealSize(temp)) {
                    temp = temp2
                    lastRubixSolution = color
                }
            }
        }
        return temp
    }

    private fun getRealSize(list: List<Int>): Int {
        var size = 0
        list.distinct().forEach { pane ->
            val count = list.count { it == pane }
            size += if (count >= 3) 5 - count else count
        }
        return size
    }

    private fun dist(pane: Int, most: Int): Int =
        if (pane > most) (most + colorOrder.size) - pane else most - pane

    private fun solveNumbers(items: List<ItemStack?>): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item?.metadata == 14 && Item.getIdFromItem(item.item) == 160) index else null
        }.sortedBy { items[it]?.stackSize }
    }

    private fun solveStartsWith(items: List<ItemStack?>, letter: String): List<Int> =
        items.mapIndexedNotNull { index, item -> if (item?.unformattedName?.startsWith(letter, true) == true && !item.isItemEnchanted) index else null }

    private fun solveSelect(items: List<ItemStack?>, color: String): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item?.isItemEnchanted == false &&
                item.unlocalizedName?.contains(color, true) == true &&
                (color == "lightblue" || item.unlocalizedName?.contains("lightBlue", true) == false) && // color BLUE should not accept light blue items.
                Item.getIdFromItem(item.item) != 160
            ) index else null
        }
    }

    private fun solveMelody(items: List<ItemStack?>): List<Int> {
        val greenPane = items.indexOfLast { it?.metadata == 5 && Item.getIdFromItem(it.item) == 160 }.takeIf { it != -1 } ?: return emptyList()
        val magentaPane = items.indexOfFirst { it?.metadata == 2 && Item.getIdFromItem(it.item) == 160 }.takeIf { it != -1 } ?: return emptyList()
        val greenClay = items.indexOfFirst { it?.metadata == 5 && Item.getIdFromItem(it.item) == 159 }.takeIf { it != -1 } ?: return emptyList()
        return items.mapIndexedNotNull { index, item ->
            when {
                index == greenPane || item?.metadata == 2 && Item.getIdFromItem(item.item) == 160 -> index
                index == greenClay && greenPane % 9 == magentaPane % 9 -> index
                else -> null
            }
        }
    }
}