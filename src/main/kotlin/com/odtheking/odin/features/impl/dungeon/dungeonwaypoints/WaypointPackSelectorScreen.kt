package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import androidx.compose.runtime.*
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.OdinScreen
import com.odtheking.odin.clickgui.ui.components.*
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.config.WaypointPackFileUtils
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.ui.compose.*
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class WaypointPackSelectorScreen(private val parent: Screen?) : OdinScreen(Component.literal("Waypoint Pack Manager")) {

    private class Prompt(val title: String, val onConfirm: (String) -> Unit)

    private var revision by mutableIntStateOf(0)
    private var packs by mutableStateOf(emptyList<String>())
    private var loading by mutableStateOf(false)
    private var prompt by mutableStateOf<Prompt?>(null)

    init {
        refreshAfter {}
    }

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

    private fun listHeight() = dialogListHeight(LIST_Y, 224)

    @Composable
    private fun PackPanel() {
        val footerY = { LIST_Y + listHeight() + DIALOG_PADDING }
        Dialog(PANEL_WIDTH, { footerY() + DIALOG_BUTTON_HEIGHT + DIALOG_PADDING }) {
            Text({ "§lWaypoint Pack Manager" }).centeredX(PANEL_WIDTH, DIALOG_PADDING)
            Row(8) {
                Button("Create Pack", 100, DIALOG_BUTTON_HEIGHT) {
                    prompt = Prompt("Create Pack") { name ->
                        if (name.isNotBlank()) refreshAfter { DungeonWaypoints.createPack(name) }
                    }
                }
                Button("Import", 80, DIALOG_BUTTON_HEIGHT) {
                    prompt = Prompt("Import as New Pack") { name ->
                        refreshAfter {
                            DungeonWaypointConfig.decodeWaypoints(name)
                                ?.takeIf { DungeonWaypoints.importPack(name, it) }
                                ?.let { modMessage("§aImported waypoints as pack '$name'!") }
                                ?: modMessage("§cFailed to decode waypoints from clipboard.")
                        }
                    }
                }
            }.centeredX(PANEL_WIDTH, ACTIONS_Y)
            if (loading) Text({ "§7Loading..." }).centeredX(PANEL_WIDTH, LIST_Y + listHeight() / 2 - 4) else PackList()
            Button("Done", 80, DIALOG_BUTTON_HEIGHT) { if (parent != null) mc.setScreenAndShow(parent) else mc.gui.setScreen(null) }
                .centeredX(PANEL_WIDTH, footerY)
        }
    }

    @Composable
    private fun PackList() {
        val selected = remember(revision) { DungeonWaypoints.selectedPackIds.toList() }
        val edit = remember(revision) { DungeonWaypoints.editPackId }

        ScrollColumn(ROW_WIDTH, ::listHeight, ROW_SPACING) {
            for (name in packs) key(name) {
                PackRow(name, name in selected, name == edit, packs.size > 1)
            }
        }.at(DIALOG_PADDING, LIST_Y)
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
            }.centeredY(ROW_HEIGHT, GuiTheme.PADDING)
            Box {
                Text({ (if (editing) "§e★ " else if (nameHover.hovered) "§f" else "§7") + name }).centeredY(ROW_HEIGHT, 0)
            }.size(150, ROW_HEIGHT).clip().hoverable(nameHover).clickable(onClick = {
                if (name !in DungeonWaypoints.selectedPackIds) DungeonWaypoints.selectedPackIds.add(name)
                DungeonWaypoints.editPackId = name
                revision++
                backgroundSave()
            }).at(36, 0)
            Text({
                val count = DungeonWaypoints.loadedPacks[name]?.values?.sumOf { it.size }
                if (count != null) "§a$count §7wp" else "§7? wp"
            }).centeredY(ROW_HEIGHT, 190)
            Button("§7✎", 26, 18) {
                prompt = Prompt("Rename Pack") { newName ->
                    if (newName.isNotBlank() && newName != name) refreshAfter { DungeonWaypoints.renamePack(name, newName) }
                }
            }.centeredY(ROW_HEIGHT, 260)
            Button(if (canDelete) "§cX" else "§8X", 26, 18) {
                if (canDelete) refreshAfter { DungeonWaypoints.deletePack(name) }
            }.centeredY(ROW_HEIGHT, 292)
        }.size(ROW_WIDTH, ROW_HEIGHT).let { if (editing) it.outlined() else it.background() }
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