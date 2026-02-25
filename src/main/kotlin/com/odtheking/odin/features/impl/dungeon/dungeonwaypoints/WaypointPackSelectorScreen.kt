package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.config.WaypointPackFileUtils
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ScrollableLayout
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class WaypointPackSelectorScreen(private val parent: Screen?) : Screen(Component.literal("Waypoint Pack Manager")) {

    private var allPacks = listOf<WaypointPackFileUtils.WaypointPack>()
    private var selectedPacks = mutableListOf<String>()
    private var activeEditPack = ""
    private var loading = false
    private var deleteConfirmPack: String? = null
    private var deleteConfirmTime = 0L
    private val deleteButtons = mutableMapOf<String, Button>()

    private lateinit var packListLayout: LinearLayout
    private lateinit var scrollableLayout: ScrollableLayout

    override fun init() {
        super.init()

        packListLayout = LinearLayout.vertical().spacing(4)
        packListLayout.defaultCellSetting().alignHorizontallyCenter()

        val scrollHeight = height - 120
        scrollableLayout = ScrollableLayout(mc, packListLayout, scrollHeight)
        scrollableLayout.setMinWidth(450)
        scrollableLayout.setMaxHeight(scrollHeight)

        loadPacks()
    }

    override fun repositionElements() {
        clearWidgets()

        val headerLayout = LinearLayout.vertical().spacing(8)
        headerLayout.defaultCellSetting().alignHorizontallyCenter()

        headerLayout.addChild(StringWidget(Component.literal("§6§lWaypoint Pack Manager"), font))

        val actionLayout = LinearLayout.horizontal().spacing(8)
        actionLayout.addChild(Button.builder(Component.literal("Create Pack")) { showCreateDialog() }.width(80).build())
        actionLayout.addChild(Button.builder(Component.literal("Import")) { importFromClipboard() }.width(80).build())
        headerLayout.addChild(actionLayout)

        headerLayout.arrangeElements()
        headerLayout.setPosition(width / 2 - headerLayout.width / 2, 10)

        val footerLayout = LinearLayout.horizontal().spacing(8)
        footerLayout.addChild(Button.builder(Component.literal("Done")) {
            savePacks()
            mc.setScreen(parent)
        }.width(80).build())
        footerLayout.addChild(Button.builder(Component.literal("Cancel")) {
            mc.setScreen(parent)
        }.width(80).build())

        footerLayout.arrangeElements()
        footerLayout.setPosition(width / 2 - footerLayout.width / 2, height - 30)

        val scrollTop = 10 + headerLayout.height + 10
        val scrollBottom = height - 30 - 10
        val scrollHeight = scrollBottom - scrollTop

        scrollableLayout.setMaxHeight(scrollHeight)
        scrollableLayout.arrangeElements()
        scrollableLayout.setPosition(width / 2 - scrollableLayout.width / 2, scrollTop)

        headerLayout.visitWidgets(this::addRenderableWidget)
        packListLayout.visitWidgets(this::addRenderableWidget)
        footerLayout.visitWidgets(this::addRenderableWidget)
    }

    private fun loadPacks() {
        loading = true
        scope.launch(Dispatchers.IO) {
            allPacks = WaypointPackFileUtils.getAllPacks()

            if (allPacks.isEmpty()) {
                WaypointPackFileUtils.createPack("default")
                allPacks = WaypointPackFileUtils.getAllPacks()
            }

            selectedPacks = DungeonWaypoints.activePacks.split(",").filter { it.isNotBlank() }.toMutableList()
            activeEditPack = DungeonWaypoints.activeEditPack

            if (activeEditPack.isNotBlank() && activeEditPack !in selectedPacks) {
                selectedPacks.add(activeEditPack)
            }

            if (selectedPacks.isEmpty() && allPacks.isNotEmpty()) {
                selectedPacks.add(allPacks.first().name)
                activeEditPack = allPacks.first().name
                DungeonWaypoints.activePacks = allPacks.first().name
                DungeonWaypoints.activeEditPack = allPacks.first().name
            }

            if (activeEditPack.isBlank() && selectedPacks.isNotEmpty()) {
                activeEditPack = selectedPacks.first()
                DungeonWaypoints.activeEditPack = activeEditPack
            }

            loading = false
            mc.execute {
                updatePackList()
                repositionElements()
            }
        }
    }

    private fun updatePackList() {
        deleteButtons.clear()

        packListLayout = LinearLayout.vertical().spacing(4)
        packListLayout.defaultCellSetting().alignHorizontallyCenter()

        allPacks.forEach { pack ->
            packListLayout.addChild(createPackRow(pack))
        }

        val scrollHeight = height - 120
        scrollableLayout = ScrollableLayout(mc, packListLayout, scrollHeight)
        scrollableLayout.setMinWidth(450)
        scrollableLayout.setMaxHeight(scrollHeight)
    }

    private fun createPackRow(pack: WaypointPackFileUtils.WaypointPack): LinearLayout {
        val rowLayout = LinearLayout.horizontal().spacing(4)

        val isSelected = pack.name in selectedPacks
        val isEditPack = pack.name == activeEditPack

        rowLayout.addChild(Button.builder(
            Component.literal(if (isSelected) "§a☑" else "§7☐")
        ) { togglePack(pack.name) }
            .size(24, 24)
            .tooltip(Tooltip.create(Component.literal(if (isSelected) "Enabled" else "Disabled")))
            .build())

        rowLayout.addChild(Button.builder(
            Component.literal((if (isEditPack) "§e★ " else "§7") + pack.name)
        ) { setEditPack(pack.name) }
            .size(280, 24)
            .tooltip(Tooltip.create(Component.literal(if (isEditPack) "Currently editing" else "Click to edit")))
            .build())

        val waypointCount = pack.waypoints.values.sumOf { it.size }
        val countWidget = StringWidget(Component.literal("§a$waypointCount §7wp"), font)
        countWidget.setWidth(70)
        rowLayout.addChild(countWidget)

        rowLayout.addChild(Button.builder(
            Component.literal("✎")
        ) { showRenameDialog(pack.name) }
            .size(30, 24)
            .tooltip(Tooltip.create(Component.literal("Rename")))
            .build())

        val isConfirmDelete = deleteConfirmPack == pack.name && (System.currentTimeMillis() - deleteConfirmTime) < 3000
        val deleteButton = Button.builder(Component.literal(if (isConfirmDelete) "§c?" else "§c✕")) { handleDelete(pack.name) }
            .size(30, 24)
            .tooltip(Tooltip.create(Component.literal(if (isConfirmDelete) "Confirm?" else "Delete")))
            .build()

        if (allPacks.size == 1) {
            deleteButton.active = false
            deleteButton.setTooltip(Tooltip.create(Component.literal("Cannot delete the only pack")))
        }

        deleteButtons[pack.name] = deleteButton

        rowLayout.addChild(deleteButton)
        return rowLayout
    }

    private fun savePacks() {
        DungeonWaypoints.activePacks = selectedPacks.joinToString(",")
        DungeonWaypoints.activeEditPack = activeEditPack

        scope.launch {
            DungeonWaypoints.loadWaypoints()
            DungeonUtils.currentRoom?.setWaypoints()
        }
        ModuleManager.saveConfigurations()
    }

    private fun importFromClipboard() {
        val clipboard = mc.keyboardHandler?.clipboard?.trim()?.trim { it == '\n' }
        if (clipboard.isNullOrBlank()) return modMessage("§cClipboard is empty!")

        mc.setScreen(TextPromptScreen("Import as New Pack").setCallback { name ->
            if (name.isNotBlank()) {
                scope.launch(Dispatchers.IO) {
                    DungeonWaypointConfig.decodeWaypoints(clipboard, clipboard.startsWith("{"))?.let { waypoints ->
                        if (WaypointPackFileUtils.createPack(name)) {
                            loading = true
                            WaypointPackFileUtils.savePack(name, waypoints)
                            modMessage("§aImported waypoints as pack '$name'!${if (!DungeonWaypoints.enabled) " §7(Enable DungeonWaypoints module)" else ""}")
                            loadPacks()
                            loading = false
                        }
                    } ?: modMessage("§cFailed to decode waypoints. §7Is the data valid?")
                }
            }
            mc.setScreen(this@WaypointPackSelectorScreen)
        })
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

        if (deleteConfirmPack != null && (System.currentTimeMillis() - deleteConfirmTime) >= 3000) {
            deleteButtons[deleteConfirmPack]?.let { button ->
                button.message = Component.literal("§c✕")
                button.setTooltip(Tooltip.create(Component.literal("Delete")))
            }
            deleteConfirmPack = null
        }
    }

    private fun togglePack(packName: String) {
        if (packName in selectedPacks) {
            if (selectedPacks.size == 1) return

            if (packName == activeEditPack) activeEditPack = selectedPacks.find { it != packName } ?: return

            selectedPacks.remove(packName)
        } else selectedPacks.add(packName)

        if (activeEditPack.isEmpty() && selectedPacks.isNotEmpty()) activeEditPack = selectedPacks.first()
        if (activeEditPack.isNotEmpty() && activeEditPack !in selectedPacks) selectedPacks.add(activeEditPack)

        savePacks()
        updatePackList()
        repositionElements()
    }

    private fun setEditPack(packName: String) {
        if (packName !in selectedPacks) selectedPacks.add(packName)
        activeEditPack = packName
        savePacks()
        updatePackList()
        repositionElements()
    }

    private fun showCreateDialog() {
        mc.setScreen(TextPromptScreen("Create Pack").apply {
            setCallback { name ->
                if (name.isNotBlank()) createPack(name)
                mc.setScreen(this@WaypointPackSelectorScreen)
            }
        })
    }

    private fun showRenameDialog(oldName: String) {
        mc.setScreen(TextPromptScreen("Rename Pack").apply {
            setCallback { newName ->
                if (newName.isNotBlank() && newName != oldName) renamePack(oldName, newName)
                mc.setScreen(this@WaypointPackSelectorScreen)
            }
        })
    }

    private fun handleDelete(packName: String) {
        val isConfirmed = deleteConfirmPack == packName && (System.currentTimeMillis() - deleteConfirmTime) < 3000

        if (isConfirmed) {
            deletePack(packName)
            deleteConfirmPack = null
        } else {
            deleteConfirmPack = packName
            deleteConfirmTime = System.currentTimeMillis()

            deleteButtons[packName]?.let { button ->
                button.message = Component.literal("§c✓?")
                button.setTooltip(Tooltip.create(Component.literal("Confirm?")))
            }
        }
    }

    private fun createPack(name: String) {
        loading = true
        scope.launch(Dispatchers.IO) {
            if (WaypointPackFileUtils.createPack(name)) {
                if (allPacks.isEmpty()) {
                    selectedPacks.clear()
                    selectedPacks.add(name)
                    activeEditPack = name
                    DungeonWaypoints.activePacks = name
                    DungeonWaypoints.activeEditPack = name
                }
                loadPacks()
            } else loading = false
        }
    }

    private fun deletePack(packName: String) {
        if (allPacks.size <= 1) {
            modMessage("§cCannot delete the only pack!")
            return
        }

        loading = true
        scope.launch(Dispatchers.IO) {
            if (WaypointPackFileUtils.deletePack(packName)) {
                if (packName in selectedPacks) {
                    selectedPacks.remove(packName)
                    if (selectedPacks.isEmpty()) {
                        val remainingPack = allPacks.firstOrNull { it.name != packName }?.name
                        if (remainingPack != null) selectedPacks.add(remainingPack)
                    }
                }
                if (packName == activeEditPack && selectedPacks.isNotEmpty())
                    activeEditPack = selectedPacks.first()
                savePacks()
                loadPacks()
            } else loading = false
        }
    }

    private fun renamePack(oldName: String, newName: String) {
        if (allPacks.any { it.name == newName }) {
            modMessage("§cPack '$newName' already exists!")
            return
        }

        loading = true
        scope.launch(Dispatchers.IO) {
            if (WaypointPackFileUtils.renamePack(oldName, newName)) {
                if (oldName in selectedPacks) selectedPacks[selectedPacks.indexOf(oldName)] = newName
                if (oldName == activeEditPack) activeEditPack = newName
                savePacks()
                loadPacks()
            } else loading = false
        }
    }

    override fun isPauseScreen() = false
}
