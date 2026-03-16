package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.config.WaypointPackFileUtils
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ScrollableLayout
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class WaypointPackSelectorScreen(private val parent: Screen?) : Screen(Component.literal("Waypoint Pack Manager")) {

    private var loading = false
    private var needsRefresh = false
    private var deleteConfirmPack: String? = null
    private var deleteConfirmTime = 0L
    private val deleteButtons = mutableMapOf<String, Button>()

    private fun packNames() = WaypointPackFileUtils.packsFolder
        .listFiles { f -> f.extension == "json" }
        ?.map { it.nameWithoutExtension }
        ?.sorted() ?: emptyList()

    private fun packWaypointCount(packName: String): Int =
        DungeonWaypoints.loadedPacks[packName]?.values?.sumOf { it.size }
            ?: runCatching { runBlocking { WaypointPackFileUtils.loadPack(packName).values.sumOf { it.size } } }.getOrDefault(0)

    override fun init() {
        super.init()
        repositionElements()
    }

    override fun repositionElements() {
        clearWidgets()
        deleteButtons.clear()
        if (loading) return

        val headerLayout = LinearLayout.vertical().spacing(8)
        headerLayout.defaultCellSetting().alignHorizontallyCenter()
        headerLayout.addChild(StringWidget(Component.literal("§6§lWaypoint Pack Manager"), font))
        val actionLayout = LinearLayout.horizontal().spacing(8)
        actionLayout.addChild(Button.builder(Component.literal("Create Pack")) { showCreateDialog() }.width(100).build())
        actionLayout.addChild(Button.builder(Component.literal("Import")) { importFromClipboard() }.width(80).build())
        headerLayout.addChild(actionLayout)
        headerLayout.arrangeElements()
        headerLayout.setPosition(width / 2 - headerLayout.width / 2, 10)

        val packListLayout = LinearLayout.vertical().spacing(4)
        packListLayout.defaultCellSetting().alignHorizontallyCenter()
        packNames().forEach { packListLayout.addChild(createPackRow(it)) }
        packListLayout.arrangeElements()
        val scrollTop = 10 + headerLayout.height + 10
        val scrollBottom = height - 30 - 10
        val scrollHeight = (scrollBottom - scrollTop).coerceAtLeast(40)
        val scrollableLayout = ScrollableLayout(mc, packListLayout, scrollHeight)
        scrollableLayout.setMinWidth(450)
        scrollableLayout.setMaxHeight(scrollHeight)
        scrollableLayout.arrangeElements()
        scrollableLayout.setPosition(width / 2 - scrollableLayout.width / 2, scrollTop)

        val footerLayout = LinearLayout.horizontal().spacing(8)
        footerLayout.addChild(Button.builder(Component.literal("Done")) { mc.setScreen(parent) }.width(80).build())
        footerLayout.arrangeElements()
        footerLayout.setPosition(width / 2 - footerLayout.width / 2, height - 30)

        headerLayout.visitWidgets(this::addRenderableWidget)
        scrollableLayout.visitWidgets(this::addRenderableWidget)
        footerLayout.visitWidgets(this::addRenderableWidget)
    }

    private fun createPackRow(packName: String): LinearLayout {
        val row = LinearLayout.horizontal().spacing(4)
        val isSelected = packName in DungeonWaypoints.selectedPackIds
        val isEdit = packName == DungeonWaypoints.editPackId

        row.addChild(Button.builder(Component.literal(if (isSelected) "§a☑" else "§7☐")) { togglePack(packName) }
            .size(24, 24).tooltip(Tooltip.create(Component.literal(if (isSelected) "Enabled" else "Disabled"))).build())

        row.addChild(Button.builder(Component.literal((if (isEdit) "§e★ " else "§7") + packName)) { setEditPack(packName) }
            .size(280, 24).tooltip(Tooltip.create(Component.literal(if (isEdit) "Currently editing" else "Click to edit"))).build())

        val count = packWaypointCount(packName)
        row.addChild(StringWidget(Component.literal("§a$count §7wp"), font).apply { setWidth(70) })

        row.addChild(Button.builder(Component.literal("✎")) { showRenameDialog(packName) }
            .size(30, 24).tooltip(Tooltip.create(Component.literal("Rename"))).build())

        val isConfirm = deleteConfirmPack == packName && (System.currentTimeMillis() - deleteConfirmTime) < 3000
        val deleteBtn = Button.builder(Component.literal(if (isConfirm) "§c✓?" else "§c✕")) { handleDelete(packName) }
            .size(30, 24).tooltip(Tooltip.create(Component.literal(if (isConfirm) "Confirm?" else "Delete"))).build()
        if (packNames().size == 1) {
            deleteBtn.active = false
            deleteBtn.setTooltip(Tooltip.create(Component.literal("Cannot delete the only pack")))
        }
        deleteButtons[packName] = deleteBtn
        row.addChild(deleteBtn)
        row.arrangeElements()
        return row
    }

    private fun togglePack(packName: String) {
        if (packName in DungeonWaypoints.selectedPackIds) {
            if (DungeonWaypoints.selectedPackIds.size == 1) return
            if (packName == DungeonWaypoints.editPackId)
                DungeonWaypoints.editPackId = DungeonWaypoints.selectedPackIds.first { it != packName }
            DungeonWaypoints.selectedPackIds.remove(packName)
        } else DungeonWaypoints.selectedPackIds.add(packName)
        repositionElements()
        backgroundSave()
    }

    private fun setEditPack(packName: String) {
        if (packName !in DungeonWaypoints.selectedPackIds) DungeonWaypoints.selectedPackIds.add(packName)
        DungeonWaypoints.editPackId = packName
        repositionElements()
        backgroundSave()
    }

    private fun backgroundSave() {
        val selected = DungeonWaypoints.selectedPackIds.toList()
        val edit = DungeonWaypoints.editPackId
        OdinMod.scope.launch { DungeonWaypoints.savePackSelection(selected, edit) }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (loading) {
            context.drawCenteredString(font, "§7Loading...", width / 2, height / 2, Colors.WHITE.rgba)
            return
        }
        super.render(context, mouseX, mouseY, delta)
    }

    override fun tick() {
        super.tick()
        if (needsRefresh) {
            needsRefresh = false
            loading = false
            repositionElements()
        }
        if (deleteConfirmPack != null && (System.currentTimeMillis() - deleteConfirmTime) >= 3000) {
            deleteButtons[deleteConfirmPack]?.let { it.message = Component.literal("§c✕"); it.setTooltip(Tooltip.create(Component.literal("Delete"))) }
            deleteConfirmPack = null
        }
    }

    private fun showCreateDialog() {
        mc.setScreen(TextPromptScreen("Create Pack").apply {
            setCallback { name ->
                if (name.isNotBlank()) {
                    loading = true
                    OdinMod.scope.launch {
                        DungeonWaypoints.createPack(name)
                        needsRefresh = true
                    }
                }
                mc.setScreen(this@WaypointPackSelectorScreen)
            }
        })
    }

    private fun showRenameDialog(oldName: String) {
        mc.setScreen(TextPromptScreen("Rename Pack").apply {
            setCallback { newName ->
                if (newName.isNotBlank() && newName != oldName) {
                    loading = true
                    OdinMod.scope.launch {
                        DungeonWaypoints.renamePack(oldName, newName)
                        needsRefresh = true
                    }
                }
                mc.setScreen(this@WaypointPackSelectorScreen)
            }
        })
    }

    private fun handleDelete(packName: String) {
        if (deleteConfirmPack == packName && (System.currentTimeMillis() - deleteConfirmTime) < 3000) {
            loading = true
            deleteConfirmPack = null
            OdinMod.scope.launch {
                DungeonWaypoints.deletePack(packName)
                needsRefresh = true
            }
        } else {
            deleteConfirmPack = packName
            deleteConfirmTime = System.currentTimeMillis()
            deleteButtons[packName]?.let { it.message = Component.literal("§c✓?"); it.setTooltip(Tooltip.create(Component.literal("Confirm?"))) }
        }
    }

    private fun importFromClipboard() {
        val clipboard = mc.keyboardHandler.clipboard.trim().trim { it == '\n' }
        if (clipboard.isBlank()) return modMessage("§cClipboard is empty!")
        mc.setScreen(TextPromptScreen("Import as New Pack").setCallback { name ->
            if (name.isNotBlank()) {
                loading = true
                OdinMod.scope.launch {
                    val ok = DungeonWaypointConfig.decodeWaypoints(clipboard, clipboard.startsWith("{"))
                        ?.takeIf { DungeonWaypoints.importPack(name, it) }
                        ?.let { modMessage("§aImported waypoints as pack '$name'!"); true }
                        ?: false
                    if (!ok) {
                        modMessage("§cFailed to decode waypoints from clipboard.")
                        needsRefresh = true
                    } else {
                        needsRefresh = true
                    }
                }
            }
            mc.setScreen(this@WaypointPackSelectorScreen)
        })
    }

    override fun isPauseScreen() = false
}