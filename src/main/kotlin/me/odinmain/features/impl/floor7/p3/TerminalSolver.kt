package me.odinmain.features.impl.floor7.p3

import io.github.moulberry.notenoughupdates.NEUApi
import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termGUI.CustomTermGui
import me.odinmain.features.impl.floor7.p3.terminalhandler.*
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.util.MouseUtils
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.render.translate
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.EnumDyeColor
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7.",
    category = Category.FLOOR7
) {
    val renderType by SelectorSetting("Mode", "Odin", arrayListOf("Odin", "Skytils", "SBE", "Custom GUI"), description = "How the terminal solver should render.")
    val customGuiText by SelectorSetting("Custom Gui Title", "Top Left", arrayListOf("Top Left", "Middle", "Disabled"), description = "Where the custom gui text should be rendered.").withDependency { renderType == 3 }
    val customScale by NumberSetting("Custom Scale", 1f, .8f, 2.5f, .1f, description = "Size of the Custom Terminal Gui.").withDependency { renderType == 3 }
    private val cancelToolTip by BooleanSetting("Stop Tooltips", true, description = "Stops rendering tooltips in terminals.").withDependency { renderType != 3 }
    private val hideClicked by BooleanSetting("Hide Clicked", false, description = "Visually hides your first click before a gui updates instantly to improve perceived response time. Does not affect actual click time.")
    private val middleClickGUI by BooleanSetting("Middle Click GUI", true, description = "Replaces right click with middle click in terminals.").withDependency { renderType != 3 }
    private val blockIncorrectClicks by BooleanSetting("Block Incorrect Clicks", true, description = "Blocks incorrect clicks in terminals.").withDependency { renderType != 3 }
    private val cancelMelodySolver by BooleanSetting("Stop Melody Solver", false, description = "Stops rendering the melody solver.")
    val showNumbers by BooleanSetting("Show Numbers", true, description = "Shows numbers in the order terminal.")

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

    var currentTerm: TerminalHandler? = null
        private set
    var lastTermOpened: TerminalHandler? = null
    private val startsWithRegex = Regex("What starts with: '(\\w+)'?")
    private var previousWindowName = ""
    private var lastClickTime = 0L

    init {
        onPacket<S2DPacketOpenWindow> { packet ->
            previousWindowName = packet.windowTitle?.formattedText?.noControlCodes?.takeIf { it != previousWindowName } ?: return@onPacket
            val newTermType = TerminalTypes.entries.find { terminal -> previousWindowName.startsWith(terminal.guiName) }

            currentTerm = when (newTermType) {
                TerminalTypes.PANES -> PanesHandler()

                TerminalTypes.RUBIX -> RubixHandler()

                TerminalTypes.NUMBERS -> NumbersHandler()

                TerminalTypes.STARTS_WITH ->
                    StartsWithHandler(startsWithRegex.find(previousWindowName)?.groupValues?.get(1) ?: return@onPacket modMessage("Failed to find letter, please report this!"))

                TerminalTypes.SELECT ->
                    SelectAllHandler(EnumDyeColor.entries.find { previousWindowName.contains(it.name.replace("_", " ").uppercase()) }?.unlocalizedName ?: return@onPacket modMessage("Failed to find color, please report this!"))

                TerminalTypes.MELODY -> MelodyHandler()

                else -> null
            }

            currentTerm?.let {
                devMessage("§aNew terminal: §6${it.type.name}")
                TerminalEvent.Opened(it).postAndCatch()
                lastTermOpened = it

                if (renderType == 3 && Loader.isModLoaded("notenoughupdates")) NEUApi.setInventoryButtonsToDisabled()
            }
        }

        onPacket<C0DPacketCloseWindow> {
            leftTerm()
        }

        onPacket<S2EPacketCloseWindow> {
            leftTerm()
        }

        onPacket<C0EPacketClickWindow> {
            lastClickTime = System.currentTimeMillis()
        }

        execute(50) {
            if (System.currentTimeMillis() - lastClickTime >= 600) currentTerm?.let {
                it.solve(S2FPacketSetSlot(mc.thePlayer?.openContainer?.windowId ?: return@execute, it.type.windowSize - 1, null))
                it.isClicked = false
            }
        }

        onMessage(Regex("(.{1,16}) activated a terminal! \\((\\d)/(\\d)\\)")) { message ->
            if (message.groupValues[1] == mc.thePlayer.name) lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
        }
    }

    @SubscribeEvent
    fun onGuiRender(event: GuiEvent.DrawGuiBackground) {
        if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) return
        when (renderType) {
            0 -> {
                GlStateManager.translate(event.guiLeft.toFloat(), event.guiTop.toFloat(), 399f)
                Gui.drawRect(7, 16, event.xSize - 7, event.ySize - 96, backgroundColor.rgba)
                GlStateManager.translate(-event.guiLeft.toFloat(), -event.guiTop.toFloat(), -399f)
            }
            3 -> {
                CustomTermGui.render()
                event.isCanceled = true
            }
        }
    }

    private fun getShouldBlockWrong(): Boolean {
        if (renderType.equalsOneOf(0, 3)) return true
        if (!removeWrong || renderType == 2) return false
        return when (currentTerm?.type) {
            TerminalTypes.PANES -> removeWrongPanes
            TerminalTypes.RUBIX -> removeWrongRubix
            TerminalTypes.NUMBERS -> true
            TerminalTypes.STARTS_WITH -> removeWrongStartsWith
            TerminalTypes.SELECT -> removeWrongSelect
            TerminalTypes.MELODY -> removeWrongMelody
            else -> false
        }
    }

    @SubscribeEvent
    fun drawSlot(event: GuiEvent.DrawSlot) = with(currentTerm) {
        if (!enabled || renderType == 3 || this?.type == null || (type == TerminalTypes.MELODY && cancelMelodySolver)) return

        val slotIndex = event.slot.slotIndex
        val inventorySize = event.gui.inventorySlots?.inventorySlots?.size ?: 0

        if (slotIndex !in solution && slotIndex <= inventorySize - 37 && getShouldBlockWrong() && event.slot.inventory !is InventoryPlayer) event.isCanceled = true
        if (slotIndex !in solution || slotIndex > inventorySize - 37 || event.slot.inventory is InventoryPlayer) return

        val zLevel = if (renderType == 2 && type.equalsOneOf(TerminalTypes.STARTS_WITH, TerminalTypes.SELECT)) 0f else 400f

        translate(0f, 0f, zLevel)
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()

        when (type) {
            TerminalTypes.PANES -> if (renderType != 1) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, panesColor.rgba)

            TerminalTypes.STARTS_WITH, TerminalTypes.SELECT ->
                if (renderType != 1 || (renderType == 1 && !removeWrong)) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, startsWithColor.rgba)

            TerminalTypes.NUMBERS -> {
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
                val amount = event.slot.stack?.stackSize?.toString() ?: ""
                if (showNumbers) mcText(amount, event.x + 8.5f - getMCTextWidth(amount) / 2, event.y + 4.5f, 1, Color.WHITE, center = false)
            }

            TerminalTypes.RUBIX -> {
                val needed = solution.count { it == slotIndex }
                val text = if (needed < 3) needed else (needed - 5)
                if (text != 0) {
                    val color = when (text) {
                        2 -> rubixColor2
                        1 -> rubixColor1
                        -2 -> oppositeRubixColor2
                        else -> oppositeRubixColor1
                    }

                    if (renderType != 1) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, color.rgba)
                    mcText(text.toString(), event.x + 8f - getMCTextWidth(text.toString()) / 2, event.y + 4.5, 1, Color.WHITE, center = false)
                }
            }

            TerminalTypes.MELODY -> if (renderType != 1 || (renderType == 1 && !removeWrong)) {
                Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, when {
                    slotIndex / 9 == 0 || slotIndex / 9 == 5 -> melodyColumColor
                    (slotIndex % 9).equalsOneOf(1, 2, 3, 4, 5) -> melodyRowColor
                    else -> melodyPressColor
                }.rgba)
            }
        }
        GlStateManager.enableLighting()
        translate(0f, 0f, -zLevel)
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (cancelToolTip && enabled && currentTerm != null) event.toolTip.clear()
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onGuiClick(event: GuiEvent.MouseClick) = with(currentTerm) {
        if (!enabled || this == null) return

        if (renderType == 3 && !(type == TerminalTypes.MELODY && cancelMelodySolver)) {
            CustomTermGui.mouseClicked(MouseUtils.mouseX.toInt(), MouseUtils.mouseY.toInt(), event.button)
            event.isCanceled = true
            return
        }

        val slotIndex = (event.gui as? GuiChest)?.slotUnderMouse?.slotIndex ?: return

        if (blockIncorrectClicks && !canClick(slotIndex, event.button)) {
            event.isCanceled = true
            return
        }

        if (middleClickGUI) {
            click(slotIndex, if (event.button == 0) ClickType.Middle else ClickType.Right, hideClicked && !isClicked)
            event.isCanceled = true
            return
        }

        if (hideClicked && !isClicked) simulateClick(slotIndex, if (event.button == 0) ClickType.Middle else ClickType.Right)
    }

    @SubscribeEvent
    fun onGuiKeyPress(event: GuiEvent.KeyPress) {
        if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) return
        if (renderType == 3 && (Keyboard.isKeyDown(mc.gameSettings.keyBindDrop.keyCode) || (event.key in 2..10))) {
            CustomTermGui.mouseClicked(MouseUtils.mouseX.toInt(), MouseUtils.mouseY.toInt(), if (event.key == Keyboard.KEY_LCONTROL && event.key == mc.gameSettings.keyBindDrop.keyCode) 1 else 0)
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun itemStack(event: GuiEvent.DrawSlotOverlay) {
        if (enabled && currentTerm?.type == TerminalTypes.NUMBERS && (event.stack?.item?.registryName ?: return) == "minecraft:stained_glass_pane") event.isCanceled = true
    }

    private fun leftTerm() {
        currentTerm?.let {
            MinecraftForge.EVENT_BUS.unregister(it)
            devMessage("§cLeft terminal: §6${it.type.name}")
            TerminalEvent.Closed(it).postAndCatch()
            currentTerm = null
        }
    }
}