package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color

// Used to keep track of all dungeon map settings
object MapModule : Module(
    name = "Dungeon Map",
    description = "A dungeon map that scans the dungeon, heavily inspired by Funnymap.",
    category = Category.DUNGEON,
) {
    val mapWindow: Boolean by DualSetting("Is Window", "On Screen", "Window")
    val hideInBoss: Boolean by BooleanSetting("Hide in Boss Room", true)
    val showRunInfo: Boolean by BooleanSetting("Show Run Info", true)
    val autoScan: Boolean by BooleanSetting("Auto Scan", true)
    val scanChatInfo: Boolean by BooleanSetting("Scan Chat Info", true)
    val mimicMessage: Boolean by BooleanSetting("Mimic Message", true)
    val colorText: Boolean by BooleanSetting("Color Room Text", false)
    val playerHeads: Int by SelectorSetting("Player Heads", "Holding Leap", arrayListOf("Never", "Holding Leap", "Always"))
    val checkMarkStyle: Int by SelectorSetting("Check Mark Style", "Default", arrayListOf("None", "Default", "Neu"))
    val roomNames: Int by SelectorSetting("Room Names", "Puzzles & Trap", arrayListOf("None", "Puzzles & Trap", "All"))
    val mapX: Float by NumberSetting("X", 0f, 0f, 1920f)
    val mapY: Float by NumberSetting("Y", 0f, 0f, 1080f)
    val mapScale: Float by NumberSetting("Scale", 1f, 0.5f, 2f, .1f)
    val textScale: Float by NumberSetting("Text Scale", 1f, 0.5f, 2f, .1f)
    val playerHeadScale: Float by NumberSetting("Player Head Scale", 1f, 0.5f, 2f, .1f)
    val borderWidth: Double by NumberSetting("Border Width", 3.0, 0.0, 10.0, .5)

    val borderColor: Color by ColorSetting("Border Color", Color(0, 0, 0), true)
    val backgroundColor: Color by ColorSetting("Background Color", Color(0, 0, 0, 100f), true)
    val bloodDoorColor: Color by ColorSetting("Blood Door Color", Color(150, 0, 0))
    val entranceDoorColor: Color by ColorSetting("Entrance Door Color", Color(0, 150, 0))
    val openWitherDoorColor: Color by ColorSetting("Open Wither Door Color", Color(250, 250, 0))
    val witherDoorColor: Color by ColorSetting("Wither Door Color", Color(250, 0, 0))
    val roomDoorColor: Color by ColorSetting("Room Door Color", Color(0, 0, 150))
    val bloodColor: Color by ColorSetting("Blood Room Color", Color(150, 0, 0))
    val miniBossColor: Color by ColorSetting("Mini Boss Room Color", Color(200, 200, 0))
    val entranceColor: Color by ColorSetting("Entrance Room Color", Color(0, 150, 0))
    val fairyColor: Color by ColorSetting("Fairy Room Color", Color(200, 40, 250))
    val puzzleColor: Color by ColorSetting("Puzzle Room Color", Color(90, 0, 120))
    val rareColor: Color by ColorSetting("Rare Room Color", Color(100, 100, 0))
    val trapColor: Color by ColorSetting("Trap Room Color", Color(150, 90, 0))
    val roomColor: Color by ColorSetting("Room Color", Color(90, 50, 0))
    val mimicRoomColor: Color by ColorSetting("Mimic Room Color", Color(90, 10, 0))
}