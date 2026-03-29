package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.capitalizeFirst
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.world.phys.AABB
import org.lwjgl.glfw.GLFW

/**
 * Custom Waypoints for Dungeons
 * @author Bonsai
 */
object DungeonWaypoints : Module(
    name = "Dungeon Waypoints",
    description = "Custom Waypoints for Dungeon Rooms."
) {
    var allowEdits by BooleanSetting("Allow Edits", false, desc = "Allows you to edit waypoints.")
    val allowTextEdit by BooleanSetting("Allow Text Edit", false, desc = "Allows you to set the text of a waypoint while sneaking.").withDependency { allowEdits }

    val titleScale by NumberSetting("Title Scale", 1f, 0.1f, 4f, increment = 0.1f, desc = "The scale of the titles of waypoints.")
    val disableDepth by BooleanSetting("Global Depth", false, desc = "Disables depth testing for all waypoints.")

    private val editorHud by HUD("Editor HUD", "Shows information about the waypoint you're placing or looking at.") {
        drawWaypointEditorHud(it)
    }

    private val settingsDropDown by DropdownSetting("Next Waypoint Settings")
    var waypointType by SelectorSetting("Waypoint Type", WaypointType.NONE.displayName, WaypointType.entries.map { it.displayName }, desc = "The type of waypoint you want to place.").withDependency { settingsDropDown }
    var color by ColorSetting("Color", Colors.MINECRAFT_GREEN, true, desc = "The color of the next waypoint you place.").withDependency { settingsDropDown }
    var filled by BooleanSetting("Filled", false, desc = "If the next waypoint you place should be 'filled'.").withDependency { settingsDropDown }
    var depthCheck by BooleanSetting("Depth check", false, desc = "Whether the next waypoint you place should have a depth check.").withDependency { settingsDropDown }
    var useBlockSize by BooleanSetting("Use block size", true, desc = "Use the size of the block you click for waypoint size.").withDependency { settingsDropDown }
    var sizeX by NumberSetting("Size X", 1.0, .1, 5.0, 0.01, desc = "The X size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }
    var sizeY by NumberSetting("Size Y", 1.0, .1, 5.0, 0.01, desc = "The Y size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }
    var sizeZ by NumberSetting("Size Z", 1.0, .1, 5.0, 0.01, desc = "The Z size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }

    private val editModeSettings by DropdownSetting("Edit Mode Settings")
    private var presetNone by ColorSetting("None Color", Colors.MINECRAFT_GREEN, true, "Color for \"None\" Waypoints").withDependency { editModeSettings }
    private var presetNormal by ColorSetting("Normal Color", Colors.MINECRAFT_RED, true, "Color for Normal Waypoints").withDependency { editModeSettings }
    private var presetSecret by ColorSetting("Secret Color", Colors.MINECRAFT_BLUE, true, "Color for cyclable preset 3.").withDependency { editModeSettings }
    private var presetEtherwarp by ColorSetting("Etherwarp Color", Colors.MINECRAFT_GOLD, true, "Color for cyclable preset 4.").withDependency { editModeSettings }
    private var cycleWaypointType by KeybindSetting("Cycle Waypoint", GLFW.GLFW_KEY_UNKNOWN, "Keybind to cycle the waypoint type.").withDependency { editModeSettings }
        .onPress {
            if (!allowEdits) return@onPress
            when (waypointType) {
                0 -> {
                    color = presetNormal
                    modMessage("§aWaypoint type changed to §cNormal§a.")
                    waypointType++
                }
                1 -> {
                    color = presetSecret
                    modMessage("§aWaypoint type changed to §cSecret§a.")
                    waypointType++
                }
                2 -> {
                    color = presetEtherwarp
                    modMessage("§aWaypoint type changed to §cEtherwarp§a.")
                    waypointType++
                }
                3 -> {
                    color = presetNone
                    modMessage("§aWaypoint type changed to §cNone§a.")
                    waypointType = 0
                }
            }
        }

    var selectedPackIds by ListSetting("Selected Waypoint Packs", mutableListOf<String>()).hide()
    var editPackId by StringSetting("Edit Waypoint Pack", "", length = 256, desc = "").hide()
    var loadedPacks: MutableMap<String, MutableMap<String, MutableList<DungeonWaypoint>>> = mutableMapOf()
    var allActiveWaypoints: MutableMap<String, MutableList<DungeonWaypoint>> = mutableMapOf()

    private val resetButton by ActionSetting("Reset Current Room", desc = "Resets the waypoints for the current room.") {
        val room = DungeonUtils.currentRoom ?: return@ActionSetting modMessage("§cRoom not found!")
        val waypoints = getEditableWaypoints(room)
        if (waypoints.isEmpty()) return@ActionSetting modMessage("§cCurrent room does not have any editable waypoints!")
        waypoints.clear()
        syncRoomToActive(room)
        OdinMod.scope.launch { saveWaypoints() }
        modMessage("§aSuccessfully reset current room!")
    }

    var lastEtherPos: BlockPos? = null
    var lastEtherTime = 0L

    init {
        OdinMod.scope.launch(Dispatchers.IO) {
            loadWaypoints()
        }

        onReceive<ClientboundPlayerPositionPacket> {
            SecretWaypoints.onEtherwarp(this)
        }

        on<SecretPickupEvent.Bat> {
            SecretWaypoints.onSecret(this)
        }

        on<SecretPickupEvent.Item> {
            SecretWaypoints.onSecret(this)
        }

        on<SecretPickupEvent.Interact> {
            SecretWaypoints.onSecret(this)
        }

        on<RoomEnterEvent> {
            room?.setWaypoints()
        }

        on<LevelEvent.Load> {
            resetClickedWaypoints()
            lastEtherPos = null
            lastEtherTime = 0L
        }

        on<RenderEvent.Extract> {
            renderWaypoints(this)
        }

        on<InputEvent> {
            handleEditorInput(this)
        }
    }

    enum class WaypointType {
        NONE, NORMAL, SECRET, ETHERWARP;

        inline val displayName get() = name.lowercase().capitalizeFirst()

        companion object {
            fun getByInt(i: Int) = entries.getOrNull(i).takeIf { it != NONE }
            fun getByName(name: String): WaypointType? = entries.find { it.name == name.uppercase() }
        }
    }

    data class DungeonWaypoint(
        val blockPos: BlockPos, val color: Color, val filled: Boolean,
        val depth: Boolean, val aabb: AABB, val title: String? = null,
        var type: WaypointType? = null, @Transient var isClicked: Boolean = false,
    ) {
        inline val isSecret: Boolean get() = type == WaypointType.SECRET
    }

    override fun onKeybind() {
        allowEdits = !allowEdits
        modMessage("Dungeon Waypoint editing ${if (allowEdits) "§aenabled" else "§cdisabled"}§r!")
    }
}