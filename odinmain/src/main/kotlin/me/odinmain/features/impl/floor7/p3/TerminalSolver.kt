package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termGUI.CustomTermGui
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.render.translate
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution of terminals in f7/m7",
    category = Category.FLOOR7
) {

    private val lockRubixSolution: Boolean by BooleanSetting("Lock Rubix Solution", false, description = "Locks the 'correct' color of the rubix terminal to the one that was scanned first, should make the solver less 'jumpy'.")
    private val cancelToolTip: Boolean by BooleanSetting("Stop Tooltips", default = true, description = "Stops rendering tooltips in terminals")
    private val renderType: Int by SelectorSetting("Mode", "Odin", arrayListOf("Odin", "Skytils", "SBE", "Custom GUI"))
    val customScale: Float by NumberSetting("Custom Scale", 1f, .8f, 2.5f, .1f, description = "Size of the Custom Terminal Gui").withDependency { renderType == 3 }
    private val textShadow: Boolean by BooleanSetting("Shadow", true, description = "Adds a shadow to the text")


    private val showRemoveWrongSettings: Boolean by DropdownSetting("Render Wrong Settings").withDependency { renderType.equalsOneOf(1,2) }
    private val removeWrong: Boolean by BooleanSetting("Stop Rendering Wrong").withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings }
    private val removeWrongPanes: Boolean by BooleanSetting("Stop Panes", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings }
    private val removeWrongRubix: Boolean by BooleanSetting("Stop Rubix", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings }
    private val removeWrongStartsWith: Boolean by BooleanSetting("Stop Starts With", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings }
    private val removeWrongSelect: Boolean by BooleanSetting("Stop Select", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings }

    private val showColors: Boolean by DropdownSetting("Color Settings")
    private val wrongColor: Color by ColorSetting("Wrong Color", Color(45, 45, 45), true).withDependency { renderType == 0 && showColors }
    private val textColor: Color by ColorSetting("Text Color", Color(220, 220, 220), true).withDependency { showColors }
    val panesColor: Color by ColorSetting("Panes Color", Color(0, 170, 170), true).withDependency { showColors }
    val rubixColor: Color by ColorSetting("Rubix Color", Color(0, 170, 170), true).withDependency { showColors }
    val oppositeRubixColor: Color by ColorSetting("Negative Rubix Color", Color(170, 85, 0), true).withDependency { showColors }
    val orderColor: Color by ColorSetting("Order Color 1", Color(0, 170, 170, 1f), true).withDependency { showColors }
    val orderColor2: Color by ColorSetting("Order Color 2", Color(0, 100, 100, 1f), true).withDependency { showColors }
    val orderColor3: Color by ColorSetting("Order Color 3", Color(0, 65, 65, 1f), true).withDependency { showColors }
    val startsWithColor: Color by ColorSetting("Starts With Color", Color(0, 170, 170), true).withDependency { showColors }
    val selectColor: Color by ColorSetting("Select Color", Color(0, 170, 170), true).withDependency { showColors }

    private var lastRubixSolution: Int? = null
    private val zLevel get() = if (renderType == 1 && currentTerm.equalsOneOf(3,4)) 100f else 400f
    var openedTerminalTime = 0L
    var clicksNeeded = -1


    var currentTerm = TerminalTypes.NONE
    var solution = listOf<Int>()

    init {
        onPacket(S2DPacketOpenWindow::class.java) {
            if (!enabled) return@onPacket

            handlePacket(it.windowTitle.siblings.firstOrNull()?.unformattedText ?: return@onPacket)
        }
    }

    fun handlePacket(windowName: String) {
        terminalNames.indexOfFirst { term ->
            windowName.startsWith(term) }
                .takeIf { it != -1 } ?: return
    }

    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        val newTerm = terminalNames.indexOfFirst { event.name.startsWith(it) }
        if (newTerm != currentTerm) {
            currentTerm = newTerm
            openedTerminalTime = System.currentTimeMillis()
            lastRubixSolution = null
        }
        if (currentTerm == -1) return leftTerm()
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
        clicksNeeded = solution.size
        TerminalOpenedEvent(currentTerm, solution).postAndCatch()
    }

    @SubscribeEvent
    fun onGuiRender(event: DrawGuiContainerScreenEvent) {
        if (currentTerm == TerminalTypes.NONE || !enabled || !renderType.equalsOneOf(0,3) || event.container !is ContainerChest) return
        if (renderType == 3) {
            CustomTermGui.render()
            event.isCanceled = true
            return
        }
        translate(event.guiLeft.toFloat(), event.guiTop.toFloat(), 999f)
        Gui.drawRect(7, 16, event.xSize - 7, event.ySize - 96, wrongColor.rgba)
        translate(-event.guiLeft.toFloat(), -event.guiTop.toFloat(), -999f)
        GlStateManager.pushMatrix()
        translate(event.guiLeft.toFloat(), event.guiTop.toFloat(), 999)
        solution.forEach { slotIndex ->
            val slot = event.container.inventorySlots[slotIndex]
            val x = slot.xDisplayPosition
            val y = slot.yDisplayPosition
            when (currentTerm) {
                TerminalTypes.PANES -> Gui.drawRect(x, y, x + 16, y + 16, panesColor.rgba)
                TerminalTypes.COLOR -> {
                    val needed = solution.count { it == slot.slotIndex }
                    val text = if (needed < 3) needed.toString() else (needed - 5).toString()
                    Gui.drawRect(x, y, x + 16, y + 16, if (needed < 3) rubixColor.rgba else oppositeRubixColor.rgba)
                    mcText(text, x + 8f - getMCTextWidth(text) / 2, y + 4.5, 1, textColor, shadow = textShadow, false)
                }
                TerminalTypes.ORDER -> {
                    val index = solution.indexOf(slot.slotIndex)
                    if (index < 3) {
                        val color = when (index) {
                            0 -> orderColor
                            1 -> orderColor2
                            else -> orderColor3
                        }.rgba
                        Gui.drawRect(x, y, x + 16, y + 16, color)
                    }
                    val amount = slot.stack?.stackSize ?: 0
                    mcText(amount.toString(), x + 8.5f - getMCTextWidth(amount.toString()) / 2, y + 4.5f, 1, textColor, shadow = textShadow, false)

                }
                TerminalTypes.STARTS_WITH -> Gui.drawRect(x, y, x + 16, y + 16, startsWithColor.rgba)
                TerminalTypes.SELECT -> Gui.drawRect(x, y, x + 16, y + 16, selectColor.rgba)
                TerminalTypes.NONE -> return@forEach
            }
        }
        translate(0f, 0f, -999)
        GlStateManager.popMatrix()
    }

    private fun getShouldBlockWrong(): Boolean {
        if (!removeWrong) return false
        return when (currentTerm) {
            TerminalTypes.PANES -> removeWrongPanes
            TerminalTypes.COLOR -> removeWrongRubix
            TerminalTypes.ORDER -> true
            TerminalTypes.STARTS_WITH -> removeWrongStartsWith
            TerminalTypes.SELECT -> removeWrongSelect
            else -> false
        }
    }

    @SubscribeEvent
    fun drawSlot(event: DrawSlotEvent) {
        if ((removeWrong || renderType == 0) && enabled && getShouldBlockWrong() && event.slot.slotIndex <= event.container.inventorySlots.size - 37 && event.slot.slotIndex !in solution)  {
            event.isCanceled = true
            return
        }
        if (event.slot.slotIndex !in solution || event.slot.inventory == mc.thePlayer.inventory || !enabled || !renderType.equalsOneOf(1,2)) return
        translate(0f, 0f, zLevel)
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        when (currentTerm) {
            TerminalTypes.COLOR -> {
                val needed = solution.count { it == event.slot.slotIndex }
                val text = if (needed < 3) needed.toString() else (needed - 5).toString()
                mcText(text, event.x + 8f - getMCTextWidth(text) / 2, event.y + 4.5, 1, textColor, shadow = textShadow, false)
            }
            TerminalTypes.ORDER -> {
                val index = solution.indexOf(event.slot.slotIndex)
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
            TerminalTypes.STARTS_WITH -> Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, startsWithColor.rgba)
            TerminalTypes.SELECT -> Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, selectColor.rgba)
            TerminalTypes.NONE -> {}
        }
        GlStateManager.enableLighting()
        translate(0f, 0f, -zLevel)
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (!cancelToolTip || currentTerm == TerminalTypes.NONE || !enabled) return
        event.toolTip.clear()
    }

    @SubscribeEvent
    fun guiClick(event: PreGuiClickEvent) {
        if (renderType != 3 || currentTerm == -1 || !enabled) return
        CustomTermGui.mouseClicked(MouseUtils.mouseX.toInt(), MouseUtils.mouseY.toInt())
        event.isCanceled = true
    }

    @SubscribeEvent
    fun itemStack(event: DrawSlotOverlayEvent) {
        val stack = event.stack?.item?.registryName ?: return
        if (currentTerm != TerminalTypes.ORDER || !enabled || stack != "minecraft:stained_glass_pane") return
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (mc.thePlayer?.openContainer is ContainerPlayer || currentTerm == TerminalTypes.NONE) leftTerm()
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val match = Regex("(.+) (?:activated|completed) a terminal! \\((\\d)/(\\d)\\)").find(event.message) ?: return
        if (match.groups[1]?.value != mc.thePlayer.name) return
        leftTerm()
    }

    @SubscribeEvent
    fun onGateBroken(event: ChatPacketEvent) {
        val match = Regex("(.+) (?:activated|completed) a (?:terminal|lever)! \\((\\d)/(\\d)\\)").find(event.message) ?: return
        if (match.groups[2]?.value == "(7/7)" || match.groups[2]?.value == "(8/8)") leftTerm()
    }

    private fun leftTerm() {
        TerminalClosedEvent(currentTerm).postAndCatch()
        currentTerm = TerminalTypes.NONE
        solution = emptyList()
    }

    private fun solvePanes(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 }.map { items.indexOf(it) }
    }

    private val colorOrder = listOf(1, 4, 13, 11, 14)
    private fun solveColor(items: List<ItemStack?>) {
        val panes = items.filter { it?.metadata != 15 && Item.getIdFromItem(it?.item) == 160 }.filterNotNull()
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

    private fun dist(pane: Int, most: Int): Int =
            if (pane > most) (most + colorOrder.size) - pane else most - pane

    private fun solveNumbers(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 && Item.getIdFromItem(it.item) == 160 }.filterNotNull().sortedBy { it.stackSize }.map { items.indexOf(it) }
    }

    private fun solveStartsWith(items: List<ItemStack?>, letter: String) {
        solution = items.filter { it?.unformattedName?.startsWith(letter, true) == true && !it.isItemEnchanted }.map { items.indexOf(it) }
    }

    private fun solveSelect(items: List<ItemStack?>, color: String) {
        solution = items.filter {
            it?.isItemEnchanted == false &&
            it.unlocalizedName?.contains(color, true) == true &&
            (color == "LIGHT BLUE" || it.unlocalizedName?.contains("Light Blue", true) == false) && // color BLUE should not accept light blue items.
            Item.getIdFromItem(it.item) != 160
        }.map { items.indexOf(it) }
    }
}

enum class TerminalTypes(val guiName: String) {
    PANES("Correct all the panes!"),
    COLOR("Change all to same color!"),
    ORDER("Click in order!"),
    STARTS_WITH("What starts with:"),
    SELECT("Select all the"),
    NONE("None")
}