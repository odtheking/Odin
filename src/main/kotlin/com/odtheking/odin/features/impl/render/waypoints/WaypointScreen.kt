package com.odtheking.odin.features.impl.render.waypoints

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.OdinScreen
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.label
import com.odtheking.odin.clickgui.ui.*
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DIALOG_BUTTON_HEIGHT
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DIALOG_PADDING
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.Dialog
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.centeredX
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.circle
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.ui.compose.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

class WaypointScreen(private val parent: Screen?) : OdinScreen(Component.literal("Waypoints")) {

    private class Form(val edit: Waypoint?)

    private fun Vec3.prettyString(separator: String) = "${x.prettyCoord()}$separator${y.prettyCoord()}$separator${z.prettyCoord()}"

    private class Corner(pos: Vec3?) {
        var x by mutableStateOf((pos?.x ?: 0.0).prettyCoord())
        var y by mutableStateOf((pos?.y ?: 0.0).prettyCoord())
        var z by mutableStateOf((pos?.z ?: 0.0).prettyCoord())

        fun set(pos: Vec3) {
            x = pos.x.prettyCoord()
            y = pos.y.prettyCoord()
            z = pos.z.prettyCoord()
        }

        fun toPos(): Vec3? {
            val px = x.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null
            val py = y.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null
            val pz = z.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null
            return Vec3((px * 100).roundToInt() / 100.0, (py * 100).roundToInt() / 100.0, Math.round(pz * 100) / 100.0)
        }
    }

    private var selectedArea by mutableStateOf(WaypointAreas.current()?.key ?: WaypointAreas.all.first().key)
    private var form by mutableStateOf<Form?>(null)
    private var status by mutableStateOf("")
    private var here: String? = null

    override val ui = UiHost {
        MainDialog()
        form?.let { current -> key(current) { FormDialog(current.edit) } }
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        here = WaypointAreas.current()?.key
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        ui.render(graphics, width, height, mouseX, mouseY)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (form == null || event.key != InputConstants.KEY_ESCAPE) return super.keyPressed(event)
        form = null
        return true
    }

    override fun onClose() {
        if (parent == null) return super.onClose()
        ui.dismiss()
        mc.setScreenAndShow(parent)
    }

    override fun removed() {
        super.removed()
        ModuleManager.saveConfigurations()
        ui.dispose()
    }

    private fun listHeight() = 230.coerceAtMost((mc.window.guiScaledHeight - LIST_Y - STATUS_HEIGHT - DIALOG_BUTTON_HEIGHT - DIALOG_PADDING * 2 - 20).coerceAtLeast(48))

    @Composable
    private fun MainDialog() {
        val footerY = { LIST_Y + listHeight() + STATUS_HEIGHT + DIALOG_PADDING }
        var search by remember { mutableStateOf("") }
        val counts = remember(Waypoints.revision) { Waypoints.waypoints.groupingBy { it.area }.eachCount() }
        val list = remember(Waypoints.revision, selectedArea) { Waypoints.waypoints.filter { it.area == selectedArea } }

        Dialog(DIALOG_WIDTH, { footerY() + DIALOG_BUTTON_HEIGHT + DIALOG_PADDING }) {
            Text({ "§lWaypoints" }, { GuiTheme.accent.rgba }).centeredX(DIALOG_WIDTH, DIALOG_PADDING)

            TextBox(search, { search = it }, placeholder = "Search areas...", maxLength = 24, centered = true)
                .size(AREA_WIDTH, SEARCH_HEIGHT).at(DIALOG_PADDING, SEARCH_Y)

            ScrollColumn(AREA_WIDTH, DIALOG_PADDING) {
                for (group in AreaGroup.entries) {
                    val areas = WaypointAreas.all.filter { it.group == group && it.label.contains(search.trim(), ignoreCase = true) }
                    if (areas.isEmpty()) continue
                    key(group) {
                        Text({ "§l${group.title}" }, { GuiTheme.accent.rgba }).offset({ 3 })
                        Gap(3)
                        Column {
                            areas.forEachIndexed { index, area ->
                                key(area.key) {
                                    val interaction = remember { InteractionSource() }
                                    val count = counts[area.key] ?: 0
                                    Box().size(AREA_WIDTH, 17)
                                        .hoverable(interaction)
                                        .clickable {
                                            selectedArea = area.key
                                        }
                                        .drawBehind { graphics ->
                                            val selected = selectedArea == area.key
                                            val textY = y + (height - 8) / 2
                                            if (selected) graphics.roundedRect(x + 3, y + 3, x + 5, bottom - 3, GuiTheme.accent.rgba, 1f)
                                            if (index != areas.lastIndex) graphics.fill(x + 8, bottom - 1, right - 8, bottom, Colors.MINECRAFT_DARK_GRAY.rgba)

                                            val color = if (selected || interaction.hovered) Colors.WHITE else Colors.MINECRAFT_GRAY
                                            graphics.text(mc.font, (if (here == area.key) "● " else "") + area.label, x + 8, textY, color.rgba, false)
                                            if (count > 0) graphics.text(mc.font, "$count", right - 6 - mc.font.width("$count"), textY, GuiTheme.accent.rgba, false)
                                        }
                                }
                            }
                        }.drawBehind { graphics -> graphics.roundedRect(x, y, right, bottom, GuiTheme.surface.rgba, GuiTheme.RADIUS) }
                        Gap(7)
                    }
                }
            }

            Text({ "${WaypointAreas.label(selectedArea)}${if (Waypoints.enabled) "" else " §c(module disabled)"}" }, { GuiTheme.accent.rgba })
                .at(LIST_X + 2, SEARCH_Y + (SEARCH_HEIGHT - 8) / 2)
            if (list.isEmpty()) Text({ "Nothing here yet, use Add Waypoint." }, { Colors.MINECRAFT_GRAY.rgba }).at(LIST_X + 2, LIST_Y + 6)

            ScrollColumn(LIST_WIDTH, LIST_X, 4) {
                for (wp in list) key(wp.id) {
                    val hover = remember { InteractionSource() }
                    var enabled by remember(wp) { mutableStateOf(wp.enabled) }
                    val info = "${wp.trigger.label} at ${wp.blockPos.prettyString(", ")}" +
                            (if (wp.endPos != wp.blockPos) " | to ${wp.endPos.prettyString(", ")}" else "") +
                            (if (wp.radius != null) " | radius ${wp.radius}" else "") +
                            (if (wp.command != null) " | /${wp.command}" else "")

                    Box {
                        Switch(enabled) {
                            Waypoints.toggle(wp)
                            enabled = wp.enabled
                        }.at(GuiTheme.PADDING, (ROW_HEIGHT - SWITCH_HEIGHT) / 2)
                        Canvas { graphics ->
                            val centerY = y + height / 2
                            val secondary = wp.blockPos.prettyString(" ")
                            val color = if (!enabled) Colors.MINECRAFT_DARK_GRAY else if (hover.hovered) GuiTheme.accent else Colors.WHITE

                            graphics.circle(x + 6, centerY, 3.5f, (if (enabled) wp.color else Colors.MINECRAFT_DARK_GRAY).rgba)
                            graphics.text(mc.font, secondary, right - mc.font.width(secondary) - 4, centerY - 4, Colors.MINECRAFT_DARK_GRAY.rgba, false)
                            graphics.text(mc.font, wp.label.ifBlank { wp.trigger.label }, x + 15, centerY - 4, color.rgba, false)
                        }.size(LIST_WIDTH - 36 - 26 - GuiTheme.PADDING - 4, ROW_HEIGHT).clip().hoverable(hover).clickable { form = Form(wp) }.tooltip(info).at(36, 0)
                        Button("§cX", 26, 18) { Waypoints.remove(wp) }.at(LIST_WIDTH - 26 - GuiTheme.PADDING, (ROW_HEIGHT - 18) / 2)
                    }.size(LIST_WIDTH, ROW_HEIGHT).drawBehind { graphics ->
                        graphics.roundedRect(x, y, right, bottom, GuiTheme.surface.rgba, GuiTheme.RADIUS)
                    }
                }
            }

            Text({ status }).offset({ DIALOG_PADDING }, { LIST_Y + listHeight() + 3 })
            Row(FOOTER_GAP) {
                Button("Add Waypoint", 100, DIALOG_BUTTON_HEIGHT) { form = Form(null) }
                Button("Import", 70, DIALOG_BUTTON_HEIGHT) { status = Waypoints.importFromClipboard() }
                Button("Export", 70, DIALOG_BUTTON_HEIGHT) { status = Waypoints.exportToClipboard() }
                Button("Done", 70, DIALOG_BUTTON_HEIGHT) { onClose() }
            }.offset({ (DIALOG_WIDTH - (100 + 70 * 3 + FOOTER_GAP * 3)) / 2 }, footerY)
        }.inert { form != null }
    }

    @Composable
    private fun ScrollColumn(width: Int, x: Int, spacing: Int = 0, content: @Composable () -> Unit) {
        val scroll = remember { ScrollState() }
        Box { Column(spacing) { content() }.offset(y = { scroll.offset }) }
            .width(width)
            .height {
                val viewport = listHeight()
                scroll.maxScroll = height - viewport
                viewport
            }
            .clip()
            .at(x, LIST_Y)
            .onScroll { amount -> scroll.by(amount); true }
    }

    @Composable
    private fun FormDialog(edit: Waypoint?) {
        val start = remember { mc.player?.blockPosition()?.let(Vec3::atLowerCornerOf) }
        val from = remember { Corner(edit?.blockPos ?: start) }
        val to = remember { Corner(edit?.endPos ?: start) }
        var inArea by remember { mutableStateOf(edit != null && edit.endPos != edit.blockPos) }
        var label by remember { mutableStateOf(edit?.label.orEmpty()) }
        var command by remember { mutableStateOf(edit?.command.orEmpty()) }
        var trigger by remember { mutableStateOf(edit?.trigger ?: Trigger.VISUAL) }
        var message by remember { mutableStateOf("") }
        val color = remember { ColorSetting("Color", (edit?.color ?: Colors.MINECRAFT_GREEN).copy(), true, "", true) }
        val radius = remember { NumberSetting("Radius", (edit?.radius ?: 0.1f), 0.1..10.0, 0.1, "", " blocks") }
        val areaKey = edit?.area ?: selectedArea

        fun submit() {
            val first = from.toPos()
            val second = if (inArea) to.toPos() else first
            if (first == null || second == null) return run { message = "§cCoordinates must be numbers." }

            val waypoint = Waypoint(
                area = areaKey, blockPos = first, endPos = second, label = label.trim(), color = color.value.copy(),
                trigger = trigger, command = command.trim().removePrefix("/").takeIf { trigger != Trigger.VISUAL && it.isNotEmpty() },
                radius = if (inArea) null else radius.value.takeIf { it > 0 }, enabled = edit?.enabled ?: true,
            )
            Waypoints.submit(waypoint, edit)?.let { return run { message = "§c$it" } }

            if (edit != null) {
                form = null
                return
            }
            message = "§aWaypoint added."
            label = ""
            start?.let {
                from.set(it)
                to.set(it)
            }
        }

        Canvas(size = { mc.window.guiScaledWidth to mc.window.guiScaledHeight }) { graphics ->
            graphics.fill(this.x, this.y, right, bottom, 0x80000000.toInt())
        }.clickable { form = null }

        Dialog(FORM_WIDTH, { FORM_HEIGHT }) {
            Text({ "§l${if (edit == null) "Add" else "Edit"} Waypoint" }, { GuiTheme.accent.rgba }).centeredX(FORM_WIDTH, 9)
            Text({ "§7${WaypointAreas.label(areaKey)}" }).centeredX(FORM_WIDTH, 22)

            Row(4) {
                ChoiceButton("At", "A single block, optionally grown by a radius.", (LEFT_W - 4) / 2, !inArea) { inArea = false }
                ChoiceButton("In", "Everything between two corners.", (LEFT_W - 4) / 2, inArea) { inArea = true }
            }.at(LEFT_X, 38)

            Section(if (inArea) "From" else "Position").at(LEFT_X, 62)
            CornerEditor(from).at(LEFT_X, 74)
            if (inArea) {
                Section("To").at(LEFT_X, SLOT_Y)
                CornerEditor(to).at(LEFT_X, SLOT_Y + 12)
            } else Box { radius.Content() }.at(LEFT_X + 8, SLOT_Y)

            Section("Label").at(LEFT_X, 132)
            TextBox(label, { label = it }, "Shown above the waypoint", 18, false).size(LEFT_W, FIELD_HEIGHT).at(LEFT_X, 144)

            Section("Trigger").at(LEFT_X, 166)
            Row(4) {
                for (type in Trigger.entries) {
                    val description = when (type) {
                        Trigger.VISUAL -> "Only drawn in the world, never runs anything."
                        Trigger.PROXIMITY -> "Runs the command once when you walk into the zone."
                        Trigger.CLICK -> "Runs the command when you left- or right-click a block in the zone."
                    }
                    ChoiceButton(type.label, description, (LEFT_W - 8) / 3, trigger == type) { trigger = type }
                }
            }.at(LEFT_X, 178)

            Section("Command").at(LEFT_X, 206)
            TextBox(command, { command = it }, "warp hub  (no /)", 128, false).size(LEFT_W, FIELD_HEIGHT).at(LEFT_X, 218)
            Canvas(size = { if (trigger == Trigger.VISUAL) LEFT_W + 4 to 32 else 0 to 0 }) { graphics ->
                graphics.roundedRect(x, y, right, bottom, GuiTheme.background.withAlpha(0.85f).rgba, GuiTheme.RADIUS)
                graphics.textCentered("Visual waypoints never run a command", x, y, right, bottom, Colors.MINECRAFT_GRAY.rgba)
            }.pointerInput { true }.at(LEFT_X - 2, 204)

            Section("Color", GuiTheme.ROW_WIDTH).at(RIGHT_X, 38)
            Box { color.Content() }.at(RIGHT_X, 50)

            Text({ message }).centeredX(FORM_WIDTH, FORM_HEIGHT - 42)
            Row(4) {
                Button(if (edit == null) "Add" else "Save", 100, DIALOG_BUTTON_HEIGHT) { submit() }
                Button(if (edit == null) "Close" else "Cancel", 100, DIALOG_BUTTON_HEIGHT) { form = null }
            }.at((FORM_WIDTH - 204) / 2, FORM_HEIGHT - DIALOG_PADDING - DIALOG_BUTTON_HEIGHT)
        }.onKey { event ->
            if (event.key != InputConstants.KEY_RETURN && event.key != InputConstants.KEY_NUMPADENTER) return@onKey false
            submit()
            true
        }
    }

    @Composable
    private fun CornerEditor(corner: Corner): UiNode = Row(6) {
        for ((axis, value, onChange) in listOf<Triple<String, String, (String) -> Unit>>(
            Triple("§cX", corner.x) { corner.x = it },
            Triple("§aY", corner.y) { corner.y = it },
            Triple("§9Z", corner.z) { corner.z = it },
        )) Row(2) {
            Text({ axis }).offset(y = { 4 })
            TextBox(value, onChange, "0", 7, true, filter = { text -> text.filter { it.isDigit() || it == '-' || it == '.' } }).size((LEFT_W - 6 * 2 - 10 * 3) / 3, FIELD_HEIGHT)
        }
    }

    @Composable
    private fun ChoiceButton(text: String, tooltip: String, width: Int, selected: Boolean, onClick: () -> Unit) {
        val interaction = remember { InteractionSource() }
        val hover = animateProgress { interaction.hovered }
        Box().size(width, 22).hoverable(interaction).clickable(onClick = onClick).tooltip(tooltip)
            .outlined { if (selected) GuiTheme.accent.withAlpha(0.35f).rgba else GuiTheme.surface.hoverTint(hover.value) }
            .drawBehind { graphics -> graphics.textCentered(text, x, y, right, bottom, if (selected) Colors.WHITE.rgba else Colors.MINECRAFT_GRAY.rgba) }
    }

    @Composable
    private fun Section(title: String, width: Int = LEFT_W): UiNode = Canvas(size = { width to 10 }) { graphics ->
        graphics.text(mc.font, title, x, y, GuiTheme.accent.rgba, false)
        graphics.fill(x + mc.font.width(title) + 4, y + 4, right, y + 5, GuiTheme.accent.withAlpha(0.35f).rgba)
    }

    private companion object {
        const val DIALOG_WIDTH = 440
        const val AREA_WIDTH = 140
        const val LIST_X = DIALOG_PADDING + AREA_WIDTH + DIALOG_PADDING
        const val LIST_WIDTH = DIALOG_WIDTH - LIST_X - DIALOG_PADDING
        const val SEARCH_Y = 24
        const val SEARCH_HEIGHT = 18
        const val LIST_Y = 48
        const val STATUS_HEIGHT = 14
        const val ROW_HEIGHT = 24
        const val FOOTER_GAP = 4

        const val FORM_WIDTH = 392
        const val FORM_HEIGHT = 322
        const val SLOT_Y = 96
        const val LEFT_X = 10
        const val LEFT_W = 176
        const val RIGHT_X = 206
        const val FIELD_HEIGHT = 16
    }
}