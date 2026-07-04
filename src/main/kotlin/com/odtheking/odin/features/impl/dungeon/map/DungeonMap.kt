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

    val backgroundOutline by ColorSetting("Background Outline", Colors.BLACK, true, desc = "The color of the background border.")
    val backgroundColor by ColorSetting("Background Color", Colors.BLACK.withAlpha(0.2f), true, desc = "Background color of the map.")
    val textScaling by NumberSetting("Text Scaling", 0.45f, 0.1f, 1f, 0.05f, desc = "Scale of room name text.")

    private val playerDropdown by DropdownSetting("Player Settings")
    val playerNamesScaling by NumberSetting("Player Names Scaling", 0.75f, 0.1f, 2f, 0.05f, desc = "Scale of player name labels.").withDependency { playerDropdown }
    val playerNameColor by ColorSetting("Player Name Color", Color(70, 70, 70), true, desc = "Color of player name labels.").withDependency { playerDropdown }

    private val doorDropdown by DropdownSetting("Door Settings")
    val normalDoorColor by ColorSetting("Normal Door", Color(107, 58, 17), desc = "Color of normal doors.").withDependency { doorDropdown }
    val witherDoorColor by ColorSetting("Wither Door", Colors.BLACK, true, desc = "Color of wither doors.").withDependency { doorDropdown }
    val bloodDoorColor by ColorSetting("Blood Door", Color(231, 0, 0), true, desc = "Color of blood room doors.").withDependency { doorDropdown }
    val fairyDoorColor by ColorSetting("Fairy Door", Color(224, 0, 255), true, desc = "Color of fairy room doors.").withDependency { doorDropdown }

    private val roomDropdown by DropdownSetting("Room Settings")
    val normalRoomColor by ColorSetting("Normal Room", Color(107, 58, 17), true, desc = "Color of normal rooms.").withDependency { roomDropdown }
    val puzzleRoomColor by ColorSetting("Puzzle Room", Color(117, 0, 133), true, desc = "Color of puzzle rooms.").withDependency { roomDropdown }
    val trapRoomColor by ColorSetting("Trap Room", Color(216, 127, 51), true, desc = "Color of trap rooms.").withDependency { roomDropdown }
    val bloodRoomColor by ColorSetting("Blood Room", Color(255, 0, 0), true, desc = "Color of blood rooms.").withDependency { roomDropdown }
    val entranceRoomColor by ColorSetting("Entrance Room", Color(20, 133, 0), true, desc = "Color of entrance rooms.").withDependency { roomDropdown }
    val fairyRoomColor by ColorSetting("Fairy Room", Color(224, 0, 255), true, desc = "Color of fairy rooms.").withDependency { roomDropdown }
    val championRoomColor by ColorSetting("Champion Room", Color(254, 223, 0), true, desc = "Color of champion rooms.").withDependency { roomDropdown }
    val unknownRoomColor by ColorSetting("Unknown Room", Color(40, 40, 40), true, desc = "Color of unknown rooms hinted by a door with no discovered room on the other side.").withDependency { roomDropdown }

    val disablePred by BooleanSetting("Disable Prediction", false, desc = "Disables special-column room type prediction.")

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
        centeredText(mc.font, "MAP", MAP_PX / 2, MAP_PX / 2 - mc.font.lineHeight / 2, Colors.WHITE.rgba)
        return MAP_PX to MAP_PX
    }

    private fun GuiGraphicsExtractor.renderDungeonMap(): Pair<Int, Int> {
        fill(0, 0, MAP_PX, MAP_PX, backgroundColor.rgba)
        hollowFill(0, 0, MAP_PX, MAP_PX, 1, Colors.gray26)

        renderMap()

        return MAP_PX to MAP_PX
    }
}