package me.odinclient.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.InfoType
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.features.impl.dungeon.AutoSell
import me.odinclient.hud.*
import org.lwjgl.input.Keyboard


object OdinConfig : Config(Mod("OdinClient", ModType.SKYBLOCK, "/assets/odinclient/LogoSmall.png"), "odinclient.json") {

    fun init() {
        initialize()
    }


    // Dungeon Map

    @Switch(
        name = "Dungeon Map",
        description = "Displays a map of the dungeon",
        category = "Dungeon Map",
        subcategory = "Map",
        size = 2
    )
    var mapEnabled = false

    @DualOption(
        name = "Map Type",
        description = "The type of map to display, seperate window is mainly made for people with two screens",
        category = "Dungeon Map",
        subcategory = "Map",
        size = 2,
        left = "On Screen",
        right = "Seperate Window"
    )
    var mapWindow = false

    @Switch(
        name = "Hide in Boss",
        description = "Hides the map when you are in a boss room",
        category = "Dungeon Map",
        subcategory = "Map",
        size = 2
    )
    var mapHideInBoss = true

    @Switch(
        name = "Show Run Information",
        description = "Displays information about the run",
        category = "Dungeon Map",
        subcategory = "Map",
        size = 2
    )
    var mapShowRunInformation = true

    @Switch(
        name = "Auto Scan",
        description = "Automatically scans the dungeon map when you enter a dungeon",
        category = "Dungeon Map",
        subcategory = "Map",
        size = 2
    )
    var autoScan = true

    @Switch(
        name = "Nano Scan Time",
        description = "Displays the scan time in nanoseconds instead of milliseconds",
        category = "Dungeon Map",
        subcategory = "Map",
        size = 2
    )
    var nanoScanTime = false

    @Switch(
        name = "Scan Chat Info",
        description = "Displays information about the scan in chat",
        category = "Dungeon Map",
        subcategory = "Map",
        size = 2
    )
    var scanChatInfo = true

    @Switch(
        name = "Color Text",
        description = "Colors name and secret count based on room state.",
        category = "Dungeon Map",
        subcategory = "Display",
        size = 2
    )
    var mapColorText = false

    @Dropdown(
        name = "Show Player Heads",
        description = "When to show player heads on the map",
        category = "Dungeon Map",
        subcategory = "Display",
        size = 2,
        options = ["Off", "Holding Leap", "Always"]
    )
    var playerHeads = 1

    @Dropdown(
        name = "Checkmark Style",
        description = "The style of the checkmark",
        category = "Dungeon Map",
        subcategory = "Display",
        size = 2,
        options = ["None", "Default", "NEU"]
    )
    var mapCheckmark = 1

    @Dropdown(
        name = "Room Names",
        description = "Shows names of rooms on map.",
        category = "Dungeon Map",
        subcategory = "Display",
        options = ["None", "Puzzles / Trap", "All"]
    )
    var mapRoomNames = 1

    @Slider(
        name = "Map X",
        description = "The x position of the map",
        category = "Dungeon Map",
        subcategory = "Display",
        min = 0f,
        max = 1920f
    )
    var mapX = 0f

    @Slider(
        name = "Map Y",
        description = "The y position of the map",
        category = "Dungeon Map",
        subcategory = "Display",
        min = 0f,
        max = 1080f
    )
    var mapY = 0f

    @Slider(
        name = "Map Scale",
        description = "The scale of the map",
        category = "Dungeon Map",
        subcategory = "Display",
        min = 0.5f,
        max = 2f
    )
    var mapScale = 1f

    @Slider(
        name = "Text Scale",
        description = "The scale of the text",
        category = "Dungeon Map",
        subcategory = "Display",
        min = 0.5f,
        max = 2f
    )
    var textScale = 1f

    @Slider(
        name = "Player Head Scale",
        description = "The scale of the player heads",
        category = "Dungeon Map",
        subcategory = "Display",
        min = 0.5f,
        max = 2f
    )
    var playerHeadScale = 1f

    @Slider(
        name = "Map Border Width",
        description = "The width of the map border",
        category = "Dungeon Map",
        subcategory = "Display",
        min = 0f,
        max = 10f
    )
    var mapBorderWidth = 3f

    @Color(
        name = "Map Border Color",
        description = "The color of the map border",
        category = "Dungeon Map",
        subcategory = "Display",
        size = 2
    )
    var mapBorder = OneColor(0, 0, 0)

    @Color(
        name = "Map Background",
        description = "The color of the map background",
        category = "Dungeon Map",
        subcategory = "Display",
        size = 2
    )
    var mapBackground = OneColor(0, 0, 0, 100)

    @Color(
        name = "Blood Door Color",
        description = "The color of blood doors",
        category = "Dungeon Map",
        subcategory = "Doors",
        size = 2
    )
    var colorBloodDoor = OneColor(150, 0, 0)

    @Color(
        name = "Entrance Door Color",
        description = "The color of entrance doors",
        category = "Dungeon Map",
        subcategory = "Doors",
        size = 2
    )
    var colorEntranceDoor = OneColor(0, 150, 0)

    @Color(
        name = "Open Wither Door Color",
        description = "The color of open wither doors",
        category = "Dungeon Map",
        subcategory = "Doors",
        size = 2
    )
    var colorOpenWitherDoor = OneColor(255, 255, 0)

    @Color(
        name = "Wither Door Color",
        description = "The color of wither doors",
        category = "Dungeon Map",
        subcategory = "Doors",
        size = 2
    )
    var colorWitherDoor = OneColor(255, 0, 0)

    @Color(
        name = "Room Door Color",
        description = "The color of room doors",
        category = "Dungeon Map",
        subcategory = "Doors",
        size = 2
    )
    var colorRoomDoor = OneColor(0, 0, 150)

    @Color(
        name = "Blood Room Color",
        description = "The color of blood rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorBlood = OneColor(250, 0, 0)

    @Color(
        name = "Miniboss Room Color",
        description = "The color of miniboss rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorMiniboss = OneColor(200, 200, 0)

    @Color(
        name = "Entrance Room Color",
        description = "The color of entrance rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorEntrance = OneColor(0, 150, 0)

    @Color(
        name = "Fairy Room Color",
        description = "The color of fairy rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorFairy = OneColor(200, 40, 255)

    @Color(
        name = "Puzzle Room Color",
        description = "The color of puzzle rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorPuzzle = OneColor(90, 0, 120)

    @Color(
        name = "Rare Room Color",
        description = "The color of rare rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorRare = OneColor(100, 100, 0)

    @Color(
        name = "Trap Room Color",
        description = "The color of trap rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorTrap = OneColor(160, 90, 0)

    @Color(
        name = "Room Color",
        description = "The color of rooms",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorRoom = OneColor(90, 50, 0)

    @Color(
        name = "Room Mimic Color",
        description = "The color of rooms with mimics",
        category = "Dungeon Map",
        subcategory = "Rooms",
        size = 2
    )
    var colorRoomMimic = OneColor(90, 10, 0)


    // Dungeon

    @Switch(
        name = "Blood Triggerbot",
        description = "Hits blood mobs when they spawn if you are looking at them",
        category = "Dungeon",
        subcategory = "Triggerbots",
        size = 2
    )
    var bloodTriggerbot = false

    @Switch(
        name = "Spirit Bear Triggerbot",
        description = "Hits spirit bear when it spawns if you are looking at it",
        category = "Dungeon",
        subcategory = "Triggerbots",
        size = 2
    )
    var spiritBearTriggerbot = false


    @Button(
        name = "Add default dungeon items to your auto sell list",
        description = "",
        category = "Dungeon",
        subcategory = "Auto Sell",
        text = "Add default dungeon items",
        size = 2
    )
    fun addDefault() {
        AutoSell.defaultItems.forEach {
            if (!miscConfig.autoSell.contains(it)) {
                miscConfig.autoSell.add(it)
            }
        }
        miscConfig.saveAllConfigs()
    }


    @HUD(
        name = "Blessing Display HUD",
        category = "Dungeon",
        subcategory = "Blessing Display"
    )
    var powerDisplayHud: PowerDisplayHud = PowerDisplayHud()

    @HUD(
        name = "Can Clip Hud",
        category = "Dungeon",
        subcategory = "Can Clip"
    )
    var canClipHud: CanClipHud = CanClipHud()

    // General

    @Switch(
        name = "Personal Dragon",
        description = "Renders a personal dragon on your shoulder because awesome!",
        category = "General",
        subcategory = "Pet Dragon",
        size = 2
    )
    var personalDragon = false

    @Checkbox(
        name = "Render Personal Dragon Only in F5",
        description = "Only renders the personal dragon in f5 mode",
        category = "General",
        subcategory = "Pet Dragon",
        size = 2
    )
    var personalDragonOnlyF5 = true

    @Color(
        name = "Personal Dragon Colour",
        description = "Colours your personal dragon!",
        category = "General",
        subcategory = "Pet Dragon",
        size = 2
    )
    var personalDragonColor = OneColor(255, 255, 255)

    @Slider(
        name = "Horizontal Position",
        min = -10f,
        max = 10f,
        category = "General",
        subcategory = "Pet Dragon",
    )
    var personalDragonPosHorizontal = 0.6f

    @Slider(
        name = "Vertical Position",
        min = -10f,
        max = 10f,
        category = "General",
        subcategory = "Pet Dragon",
    )
    var  personalDragonPosVertical = 1.6f

    @Slider(
        name = "Dragon Scale",
        min = 0f,
        max = 50f,
        category = "General",
        subcategory = "Pet Dragon",
        step = 1
    )
    var personalDragonScale = 8f

    @Switch(
        name = "Attunement Colored",
        description = "Color the mobs based on their attunement, looks worse, but is more useful",
        category = "General",
        subcategory = "Blaze Slayer",
        size = 2
    )
    var atunementColored = false

    @Switch(
        name = "Attunement Outline",
        description = "Attunement Outline, looks better, but is harder to see",
        category = "General",
        subcategory = "Blaze Slayer",
        size = 2
    )
    var atunementOutline = false

    @Slider(
        name = "Attunement Outline Thickness",
        description = "Attunement Outline Thickness",
        category = "General",
        subcategory = "Blaze Slayer",
        min = 5f,
        max = 20f
    )
    var atunementOutlineThickness = 5f

    @Switch(
        name = "Attunement Cancel Hurt Effect",
        description = "Cancels the hurt effect of the entity, if this is disabled the outline will get discolored when the entity is hurt",
        category = "General",
        subcategory = "Blaze Slayer",
        size = 2
    )
    var atunementCancelHurt = true

    @Slider(
        name = "Arrow Trajectory Box Size",
        description = "Arrow Trajectory Box Size",
        category = "General",
        subcategory = "Arrow Trajectory",
        min = 0.5f,
        max = 3f
    )
    var arrowTrajectoryBoxSize = 1f



    @Slider(
        name = "ESP Thickness",
        description = "ESP Thickness",
        category = "General",
        subcategory = "ESP",
        min = 5f,
        max = 20f
    )
    var espThickness = 5f

    @Switch(
        name = "Cancel Hurt Effect",
        description = "Cancels the hurt effect of the entity, if this is disabled the outline will get discolored when the entity is hurt",
        category = "General",
        subcategory = "ESP",
        size = 2
    )
    var espCancelHurt = true

    @Button(
        name = "Adds the star mob star to your esp list so all star mobs are highlighted",
        description = "",
        category = "General",
        subcategory = "ESP",
        text = "Add star to esp list",
        size = 2
    )
    fun addStar() {
        if (miscConfig.espList.contains("✯")) return
        miscConfig.espList.add("✯")
        miscConfig.saveAllConfigs()
    }

    @Switch(
        name = "ESP Through Walls",
        description = "Shows through walls",
        category = "General",
        subcategory = "ESP",
    )
    var espThrough = false

    @Switch(
        name = "Custom guild commands use !help in guild chat",
        description = "Custom guild commands use !help in guild chat",
        category = "General",
        subcategory = "Chat commands",
        size = 2
    )
    var guildCommands = false

    @Switch(
        name = "Custom party commands use !help in party chat",
        description = "Custom party commands use !help in party chat",
        category = "General",
        subcategory = "Chat commands",
        size = 2
    )
    var partyCommands = false

    @Switch(
        name = "Responds to anyone in guild chat saying gm/gn",
        description = "Responds to anyone in guild chat saying gm/gn",
        category = "General",
        subcategory = "Chat commands",
        size = 2
    )
    var guildGM = false

    @Info(
        text = "/blacklist add {ign}, remove, list, clear",
        category = "General",
        subcategory = "Chat commands",
        type = InfoType.INFO
    )
    var blacklistInfo = ""


    @Switch(
        name = "Shows waypoints based on chat coords",
        description = "Shows waypoints based on chat coords you can use cmds",
        category = "General",
        subcategory = "Waypoints",
        size = 2
    )
    var waypoints = false

    @HUD(
        name = "Deployable HUD",
        category = "General",
        subcategory = "Deployable Timer"
    )
    var deployableHud: DeployableHud = DeployableHud()


    //M7

    @Switch(
        name = "Shows how long a terminal took to complete",
        description = "Shows how long a terminal took to complete",
        category = "M7",
        subcategory = "Terminal timer",
        size = 2
    )
    var termTimer = false

    @HUD(
        name = "Dragon Timer Hud",
        category = "M7",
        subcategory = "Dragon Timer"
    )
    var dragonTimerHud: DragonTimerHud = DragonTimerHud()


    // QOL


    @KeyBind(
        name = "AOTS Keybind",
        description = "Toggle GUI",
        category = "QOL",
        subcategory = "Item keybinds",
        size = 2
    )
    var aotsKeybind = OneKeyBind(Keyboard.KEY_NONE)

    @KeyBind(
        name = "Ice Spray Keybind",
        description = "Toggle GUI",
        category = "QOL",
        subcategory = "Item keybinds",
        size = 2
    )
    var iceSprayKeybind = OneKeyBind(Keyboard.KEY_NONE)

    @KeyBind(
        name = "Precursor Eye Keybind",
        description = "Toggle GUI",
        category = "QOL",
        subcategory = "Item keybinds",
        size = 2
    )
    var precursorKeybind = OneKeyBind(Keyboard.KEY_NONE)

    @KeyBind(
        name = "Fire Veil Keybind",
        description = "Toggle GUI",
        category = "QOL",
        subcategory = "Item keybinds",
        size = 2
    )
    var fireVeilKeybind = OneKeyBind(Keyboard.KEY_NONE)

    @KeyBind(
        name = "Wand of Atonement Keybind",
        description = "Toggle GUI",
        category = "QOL",
        subcategory = "Item keybinds",
        size = 2
    )
    var wandOfAtonementKeybind = OneKeyBind(Keyboard.KEY_NONE)

    @Switch(
        name = "Chat in portal",
        description = "Chat in portal",
        category = "QOL",
        subcategory = "Portal",
        size = 2
    )
    var portalFix = false

    @HUD(
        name = "Server HUD",
        category = "QOL",
        subcategory = "Server"
    )
    var serverHUD: ServerHud = ServerHud()
}
