package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import androidx.compose.runtime.*
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.OdinScreen
import com.odtheking.odin.clickgui.ui.Button
import com.odtheking.odin.clickgui.ui.SWITCH_HEIGHT
import com.odtheking.odin.clickgui.ui.Switch
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.config.WaypointPackFileUtils
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedRectOutlined
import com.odtheking.odin.utils.ui.compose.*
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class WaypointPackSelectorScreen(private val parent: Screen?) : OdinScreen(Component.literal("Waypoint Pack Manager")) {

    private class Prompt(val title: String, val onConfirm: (String) -> Unit)

    private var revision by mutableIntStateOf(0)
    private var packs by mutableStateOf(WaypointPackFileUtils.listPackNames())
    private var loading by mutableStateOf(false)
    private var prompt by mutableStateOf<Prompt?>(null)

    override val ui = UiHost {
        PackPanel()
        prompt?.let { current ->
            key(current) {
                PromptDialog(current.title, { text -> prompt = null; current.onConfirm(text) }, { prompt = null })
            }
        }
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        ui.render(graphics, width, height, mouseX, mouseY)
    }

    override fun removed() {
        super.removed()
        ui.dispose()
    }

    private fun listHeight() = 224.coerceAtMost((mc.window.guiScaledHeight - LIST_Y - DIALOG_BUTTON_HEIGHT - DIALOG_PADDING * 2 - 20).coerceAtLeast(48))

    @Composable
    private fun PackPanel() {
        val footerY = { LIST_Y + listHeight() + DIALOG_PADDING }
        Dialog(PANEL_WIDTH, { footerY() + DIALOG_BUTTON_HEIGHT + DIALOG_PADDING }) {
            Text({ "§lWaypoint Pack Manager" }).centeredX(PANEL_WIDTH, DIALOG_PADDING)
            Button("Create Pack", 100, DIALOG_BUTTON_HEIGHT) {
                prompt = Prompt("Create Pack") { name ->
                    if (name.isNotBlank()) refreshAfter { DungeonWaypoints.createPack(name) }
                }
            }.at(76, ACTIONS_Y)
            Button("Import", 80, DIALOG_BUTTON_HEIGHT) {
                prompt = Prompt("Import as New Pack") { name ->
                    refreshAfter {
                        DungeonWaypointConfig.decodeWaypoints(name)
                            ?.takeIf { DungeonWaypoints.importPack(name, it) }
                            ?.let { modMessage("§aImported waypoints as pack '$name'!") }
                            ?: modMessage("§cFailed to decode waypoints from clipboard.")
                    }
                }
            }.at(184, ACTIONS_Y)
            if (loading) Text({ "§7Loading..." }).centeredX(PANEL_WIDTH, LIST_Y + listHeight() / 2 - 4) else PackList()
            Button("Done", 80, DIALOG_BUTTON_HEIGHT) { if (parent != null) mc.setScreenAndShow(parent) else mc.gui.setScreen(null) }
                .offset({ (PANEL_WIDTH - 80) / 2 }, footerY)
        }
    }

    @Composable
    private fun PackList() {
        val selected = remember(revision) { DungeonWaypoints.selectedPackIds.toList() }
        val edit = remember(revision) { DungeonWaypoints.editPackId }
        val scroll = remember { ScrollState() }

        Box {
            Column(ROW_SPACING) {
                for (name in packs) key(name) {
                    PackRow(name, name in selected, name == edit, packs.size > 1)
                }
            }.offset(y = { scroll.offset })
        }
            .width(ROW_WIDTH)
            .height {
                val viewport = listHeight()
                scroll.maxScroll = height - viewport
                viewport
            }
            .clip()
            .at(DIALOG_PADDING, LIST_Y)
            .onScroll { amount -> scroll.by(amount); true }
    }

    @Composable
    private fun PackRow(name: String, enabled: Boolean, editing: Boolean, canDelete: Boolean) {
        val nameHover = remember { InteractionSource() }

        Box {
            Switch(enabled) {
                if (name in DungeonWaypoints.selectedPackIds) {
                    if (DungeonWaypoints.selectedPackIds.size == 1) return@Switch
                    if (name == DungeonWaypoints.editPackId)
                        DungeonWaypoints.editPackId = DungeonWaypoints.selectedPackIds.first { it != name }
                    DungeonWaypoints.selectedPackIds.remove(name)
                } else DungeonWaypoints.selectedPackIds.add(name)
                revision++
                backgroundSave()
            }.at(GuiTheme.PADDING, (ROW_HEIGHT - SWITCH_HEIGHT) / 2)
            Box {
                Text({ (if (editing) "§e★ " else if (nameHover.hovered) "§f" else "§7") + name }).at(0, (ROW_HEIGHT - 8) / 2)
            }.size(150, ROW_HEIGHT).clip().hoverable(nameHover).clickable(onClick = {
                if (name !in DungeonWaypoints.selectedPackIds) DungeonWaypoints.selectedPackIds.add(name)
                DungeonWaypoints.editPackId = name
                revision++
                backgroundSave()
            }).at(36, 0)
            Text({
                val count = DungeonWaypoints.loadedPacks[name]?.values?.sumOf { it.size }
                if (count != null) "§a$count §7wp" else "§7? wp"
            }).at(190, (ROW_HEIGHT - 8) / 2)
            Button("§7✎", 26, 18) {
                prompt = Prompt("Rename Pack") { newName ->
                    if (newName.isNotBlank() && newName != name) refreshAfter { DungeonWaypoints.renamePack(name, newName) }
                }
            }.at(260, (ROW_HEIGHT - 18) / 2)
            Button(if (canDelete) "§cX" else "§8X", 26, 18) {
                if (canDelete) refreshAfter { DungeonWaypoints.deletePack(name) }
            }.at(292, (ROW_HEIGHT - 18) / 2)
        }.size(ROW_WIDTH, ROW_HEIGHT).drawBehind { graphics ->
            if (editing) graphics.roundedRectOutlined(x, y, right, bottom, GuiTheme.surface.rgba, GuiTheme.accent.rgba, 1f, GuiTheme.RADIUS)
            else graphics.roundedRect(x, y, right, bottom, GuiTheme.surface.rgba, GuiTheme.RADIUS)
        }
    }

    private fun backgroundSave() {
        OdinMod.scope.launch { DungeonWaypoints.savePackSelection(DungeonWaypoints.selectedPackIds.toList(), DungeonWaypoints.editPackId) }
    }

    private fun refreshAfter(work: suspend () -> Unit) {
        loading = true
        OdinMod.scope.launch {
            try {
                work()
            } finally {
                packs = WaypointPackFileUtils.listPackNames()
                revision++
                loading = false
            }
        }
    }

    private companion object {
        const val PANEL_WIDTH = 340
        const val ROW_WIDTH = PANEL_WIDTH - DIALOG_PADDING * 2
        const val ROW_HEIGHT = 24
        const val ROW_SPACING = 4
        const val ACTIONS_Y = 24
        const val LIST_Y = 52
    }
}