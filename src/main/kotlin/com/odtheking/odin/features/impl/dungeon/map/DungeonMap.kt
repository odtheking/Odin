package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.gui.GuiGraphicsExtractor

object DungeonMap : Module("Bad map", description = "Displays the dungeon map.") {

    private val disableBoss by BooleanSetting("Disable in Boss", true, desc = "Disables the map during boss fights.")

    val backgroundOutline by ColorSetting("Background Outline", Colors.BLACK, false, desc = "The color of the background border.")
    var backgroundColor by ColorSetting("Background Color", Colors.BLACK.withAlpha(0.5f), true, desc = "Background color of the map.")
    var textScaling by NumberSetting("Text Scaling", 0.45f, 0.1f, 1f, 0.05f, desc = "Scale of room name text.")

    private val playerDropdown by DropdownSetting("Player Settings")
    var playerNamesScaling by NumberSetting("Player Names Scaling", 0.75f, 0.1f, 2f, 0.05f, desc = "Scale of player name labels.").withDependency { playerDropdown }
    var playerNameColor by ColorSetting("Player Name Color", Color(70, 70, 70), false, desc = "Colour of player name labels.").withDependency { playerDropdown }
    var selfVanillaMarker by BooleanSetting("Self Vanilla Marker", true, desc = "Draw a direction arrow instead of your head for yourself.").withDependency { playerDropdown }

    private val doorDropdown by DropdownSetting("Door Settings")
    var normalDoorColor by ColorSetting("Normal Door", Color(107, 58, 17), desc = "Colour of normal doors.").withDependency { doorDropdown }
    var witherDoorColor by ColorSetting("Wither Door", Colors.BLACK, desc = "Colour of wither doors.").withDependency { doorDropdown }
    var bloodDoorColor by ColorSetting("Blood Door", Colors.MINECRAFT_RED, desc = "Colour of blood room doors.").withDependency { doorDropdown }
    var fairyDoorColor by ColorSetting("Fairy Door", Color(244, 19, 139),  desc = "Colour of fairy room doors.").withDependency { doorDropdown }

    private val roomDropdown by DropdownSetting("Room Settings")
    var normalRoomColor by ColorSetting("Normal Room", Color(107, 58, 17),  desc = "Colour of normal rooms.").withDependency { roomDropdown }
    var puzzleRoomColor by ColorSetting("Puzzle Room", Color(117, 0, 133),  desc = "Colour of puzzle rooms.").withDependency { roomDropdown }
    var trapRoomColor by ColorSetting("Trap Room", Color(216, 127, 51), desc = "Colour of trap rooms.").withDependency { roomDropdown }
    var bloodRoomColor by ColorSetting("Blood Room", Color(255, 0, 0),    desc = "Colour of blood rooms.").withDependency { roomDropdown }
    var entranceRoomColor by ColorSetting("Entrance Room", Color(20, 133, 0),   desc = "Colour of entrance rooms.").withDependency { roomDropdown }
    var fairyRoomColor by ColorSetting("Fairy Room", Color(244, 19, 139), desc = "Colour of fairy rooms.").withDependency { roomDropdown }
    var championRoomColor by ColorSetting("Champion Room", Color(254, 223, 0),  desc = "Colour of champion rooms.").withDependency { roomDropdown }
    var unknownRoomColor by ColorSetting("Unknown Room", Color(40, 40, 40),   desc = "Colour of unknown rooms hinted by a door with no discovered room on the other side.").withDependency { roomDropdown }

    private val mapHud by HUD("Dungeon Map", "Displays the dungeon map.", false) { example ->
        when {
            (!DungeonUtils.inDungeons || (disableBoss && DungeonUtils.inBoss)) && !example -> 0 to 0
            example -> renderExampleMap()
            else    -> renderDungeonMap()
        }
    }

    private const val MAP_PX = 128

    private fun GuiGraphicsExtractor.renderExampleMap(): Pair<Int, Int> {
        fill(0, 0, MAP_PX, MAP_PX, backgroundColor.rgba)
        hollowFill(0, 0, MAP_PX, MAP_PX, 1, backgroundOutline)
        centeredText(mc.font, "MAP", MAP_PX / 2, MAP_PX / 2 - mc.font.lineHeight / 2, 0xFFFFFFFF.toInt())
        return MAP_PX to MAP_PX
    }

    private fun GuiGraphicsExtractor.renderDungeonMap(): Pair<Int, Int> {
        fill(0, 0, MAP_PX, MAP_PX, backgroundColor.rgba)
        hollowFill(0, 0, MAP_PX, MAP_PX, 1, Colors.gray26)

        renderMap()

        return MAP_PX to MAP_PX
    }
}