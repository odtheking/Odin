package me.odinclient.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.InfoType
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.features.dungeon.AutoSell
import me.odinclient.hud.*
import org.lwjgl.input.Keyboard


object OdinConfig : Config(Mod("OdinClient", ModType.SKYBLOCK, "/assets/odinclient/LogoSmall.png"), "odinclient.json") {

    fun init() {
        initialize()
    }


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

    @Switch(
        name = "Automatically completes ice fill puzzle",
        description = "Automatically completes ice fill puzzle",
        category = "Dungeon",
        subcategory = "Auto Ice Fill",
        size = 2
    )
    var autoIceFill = false

    @Switch(
        name = "Automatically leaps to a player when writing !tp in party chat",
        description = "Automatically leaps to a player when writing !tp in party chat",
        category = "Dungeon",
        subcategory = "Auto Leap",
        size = 2
    )
    var autoLeap = false

    @Switch(
        name = "Automatically swaps between bonzo and spirit mask",
        description = "Automatically swaps between bonzo and spirit mask",
        category = "Dungeon",
        subcategory = "Auto Mask",
        size = 2
    )
    var autoMask = false

    @Switch(
        name = "Automatically clicks in the ready GUI",
        description = "Automatically clicks in the ready GUI",
        category = "Dungeon",
        subcategory = "Auto Ready",
        size = 2,
    )
    var autoReady = false

    @Switch(
        name = "Automatically gets in range of mort and opens his GUI",
        description = "Automatically gets in range of mort and opens his GUI",
        category = "Dungeon",
        subcategory = "Auto Ready",
        size = 2,
    )
    var autoMort = false

    @Switch(
        name = "Automatically uses wither shield when not full hp",
        description = "Automatically uses wither shield when not full hp",
        category = "Dungeon",
        subcategory = "Auto Shield",
        size = 2
    )
    var autoShield = false

    @Checkbox(
        name = "Make it only work in boss",
        description = "Make it only work in boss",
        category = "Dungeon",
        subcategory = "Auto Shield",
        size = 2
    )
    var inBoss = false

    @Switch(
        name = "Automatically uses class ultimate on certain times",
        description = "Automatically uses class ultimate on certain times",
        category = "Dungeon",
        subcategory = "Auto Ult",
        size = 2
    )
    var autoUlt = false

    @Switch(
        name = "Automatically uses wish when a teammate is low HP",
        description = "Automatically uses wish when a teammate is low HP",
        category = "Dungeon",
        subcategory = "Auto Wish",
        size = 2
    )
    var autoWish = false

    @Slider(
        name = "hp% to wish at",
        description = "hp% to wish at",
        category = "Dungeon",
        subcategory = "Auto Wish",
        min = 5f,
        max = 100f
    )
    var healthPrecentage = 30f

    @Switch(
        name = "Dungeon key esp",
        description = "Dungeon key esp",
        category = "Dungeon",
        subcategory = "KeyESP",
        size = 2
    )
    var keyESP = false

    @Switch(
        name = "Automatically uses Superboom when clicking on crypts or boom-able walls",
        description = "Automatically uses Superboom when clicking on crypts or boom-able walls",
        category = "Dungeon",
        subcategory = "Superboom",
        size = 2
    )
    var superBoom = false

    @Dropdown(
        name = "Behaviour",
        options = ["Place", "Switch"],
        description = "Automatically places or switches to Superboom",
        category = "Dungeon",
        subcategory = "Superboom"
    )
    var superBoomBehaviour = 0

    @Switch(
        name = "Highlights your dungeon teammates",
        description = "Highlights your dungeon teammates",
        category = "Dungeon",
        subcategory = "Teammate Highlight",
        size = 2
    )
    var teammatesOutline = false

    @Checkbox(
        name = "In Boss",
        description = "Outlines teammates in boss as well",
        category = "Dungeon",
        subcategory = "Teammate Highlight",
        size = 2
    )
    var teammatesOutlineInBoss = false

    @Checkbox(
        name = "When Visible",
        description = "Outlines teammates while they're visible as well",
        category = "Dungeon",
        subcategory = "Teammate Highlight",
        size = 2
    )
    var teammatesOutlineWhenVisible = true

    @Slider(
        name = "Teammate Thickness",
        description = "Teammate Thickness",
        category = "Dungeon",
        subcategory = "Teammate Highlight",
        min = 5f,
        max = 20f
    )
    var teammateThickness = 5f

    @Switch(
        name = "Shows how many blood mobs are left",
        description = "Shows how many blood mobs are left",
        category = "Dungeon",
        subcategory = "Watcher Bar",
        size = 2
    )
    var watcherBar = false

    @Switch(
        name = "Auto Sell",
        description = "Automatically sells items in trades menu or booster cookie (/autosell to configure)",
        category = "Dungeon",
        subcategory = "Auto Sell",
        size = 2
    )
    var autoSell = false

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

    @Checkbox(
        name = "Power Blessing",
        description = "Displays the power blessing",
        category = "Dungeon",
        subcategory = "Blessing Display",
        size = 2
    )
    var powerBlessing = true

    @Checkbox(
        name = "Time Blessing",
        description = "Displays the time blessing",
        category = "Dungeon",
        subcategory = "Blessing Display",
        size = 2
    )
    var timeBlessing = true

    @Checkbox(
        name = "Stone Blessing",
        description = "Displays the stone blessing",
        category = "Dungeon",
        subcategory = "Blessing Display",
        size = 2
    )
    var stoneBlessing = false

    @Checkbox(
        name = "Life Blessing",
        description = "Displays the life blessing",
        category = "Dungeon",
        subcategory = "Blessing Display",
        size = 2
    )
    var lifeBlessing = false

    @Checkbox(
        name = "Wisdom Blessing",
        description = "Displays the wisdom blessing",
        category = "Dungeon",
        subcategory = "Blessing Display",
        size = 2
    )
    var wisdomBlessing = false

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

    @Checkbox(
        name = "Front Camera",
        description = "Turns on or off the front camera in f5 mode",
        category = "General",
        subcategory = "Camera",
        size = 2
    )
    var frontCamera = true

    @Slider(
        name = "Camera Distance",
        description = "Camera Distance in f5 mode",
        category = "General",
        subcategory = "Camera",
        min = 3f,
        max = 12f
    )
    var cameraDistance = 4f

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

    @Switch(
        name = "Arrow Trajectory",
        description = "Shows arrow trajectory",
        category = "General",
        subcategory = "Arrow Trajectory",
        size = 2
    )
    var arrowTrajectory = false

    @Slider(
        name = "Arrow Trajectory Thickness",
        description = "Arrow Trajectory Thickness",
        category = "General",
        subcategory = "Arrow Trajectory",
        min = 5f,
        max = 20f
    )
    var arrowTrajectoryThickness = 6f

    @Color(
        name = "Arrow Trajectory Color",
        description = "Arrow Trajectory Color",
        category = "General",
        subcategory = "Arrow Trajectory"
    )
    var arrowTrajectoryColor = OneColor(255, 0, 0)

    @Slider(
        name = "Arrow Trajectory Box Size",
        description = "Arrow Trajectory Box Size",
        category = "General",
        subcategory = "Arrow Trajectory",
        min = 0.5f,
        max = 3f
    )
    var arrowTrajectoryBoxSize = 1f

    @Switch(
        name = "You can add whatever mob you want into the list /esp",
        description = "You can add whatever mob you want into the list /esp",
        category = "General",
        subcategory = "ESP",
        size = 2
    )
    var esp = false

    @Slider(
        name = "ESP Thickness",
        description = "ESP Thickness",
        category = "General",
        subcategory = "ESP",
        min = 5f,
        max = 20f
    )
    var espThickness = 5f

    @Color(
        name = "ESP Color",
        description = "ESP Color",
        category = "General",
        subcategory = "ESP"
    )
    var espColor = OneColor(255, 255, 255)

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
        name = "Improves ur frames",
        description = "Improves ur frames",
        category = "General",
        subcategory = "FPS",
        size = 2
    )
    var fps = false

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
        name = "Alerts you when a vanquisher spawns",
        description = "Alerts you when a vanquisher spawns",
        category = "General",
        subcategory = "Vanq Notifier",
        size = 2
    )
    var vanqNotifier = false

    @Checkbox(
        name = "Send message in all chat",
        category = "General",
        subcategory = "Vanq Notifier",
        size = 1
    )
    var vanqNotifierAC = false

    @Checkbox(
        name = "Send message in party chat",
        category = "General",
        subcategory = "Vanq Notifier",
        size = 1
    )
    var vanqNotifierPC = false

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
        name = "Automatically equips edrag after relics",
        description = "Automatically equips edrag after relics",
        category = "M7",
        subcategory = "Auto Edrag",
        size = 2
    )
    var autoEdrag = false

    @Switch(
        name = "Replaces diorite with glass in p2 ",
        description = "Replaces diorite with glass in p2 (don't use connected blocks)",
        category = "M7",
        subcategory = "FUCK DIORITE",
        size = 2
    )
    var fuckDiorite = false

    @Switch(
        name = "Creates custom and decently accurate boxes in p5",
        description = "Creates custom and decently accurate boxes in p5",
        category = "M7",
        subcategory = "Dragon Boxes",
        size = 2
    )
    var dragonBoxes = false


    @Slider(
        name = "Line Width",
        min = 1f,
        max = 5f,
        category = "M7",
        subcategory = "Dragon Boxes"
    )
    var dragonBoxesLineWidth = 2f

    @Switch(
        name = "Shows how long a terminal took to complete",
        description = "Shows how long a terminal took to complete",
        category = "M7",
        subcategory = "Terminal timer",
        size = 2
    )
    var termTimer = false

    @Switch(
        name = "Shows when a M7 dragon will spawn",
        description = "Shows when a M7 dragon will spawn",
        category = "M7",
        subcategory = "Dragon Timer",
        size = 2
    )
    var dragonTimer = false

    @HUD(
        name = "Dragon Timer Hud",
        category = "M7",
        subcategory = "Dragon Timer"
    )
    var dragonTimerHud: DragonTimerHud = DragonTimerHud()


    // QOL

    @Switch(
        name = "No Cursor Reset",
        description = "Prevents your cursor from resetting when you open a gui",
        category = "QOL",
        subcategory = "No Cursor Reset",
        size = 2
    )
    var noCursorReset = false

    @Switch(
        name = "Gyro Range",
        description = "Renders a circle for the range of your gyro",
        category = "QOL",
        subcategory = "Gyro Range",
        size = 2
    )
    var gyroRange = false

    @Slider(
        name = "Gyro Range Thickness",
        min = 0f,
        max = 10f,
        category = "QOL",
        subcategory = "Gyro Range"
    )
    var gyroThickness = 10f

    @Slider(
        name = "Gyro Range Steps",
        category = "QOL",
        subcategory = "Gyro Range",
        min = 20f,
        max = 80f,
        step = 1
    )
    var gyroSteps = 40f

    @Color(
        name = "Gyro Range Color",
        category = "QOL",
        subcategory = "Gyro Range"
    )
    var gyroColor = OneColor(192, 64, 192, 128)

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

    @Switch(
        name = "Toggle Sprint",
        description = "Toggle Sprint",
        category = "QOL",
        subcategory = "Auto Sprint",
        size = 2
    )
    var autoSprint = false

    @Switch(
        name = "Automatically alerts when your hype is broken",
        description = "Automatically alerts when your hype is broken",
        category = "QOL",
        subcategory = "Broken Hype",
        size = 2
    )
    var brokenHype = false

    @Checkbox(
        name = "Play Sound",
        category = "QOL",
        subcategory = "Broken Hype",
        size = 1
    )
    var brokenHypePlaySound = false

    @Checkbox(
        name = "Display Title",
        category = "QOL",
        subcategory = "Broken Hype",
        size = 1
    )
    var brokenHypeShowTitle = false

    @Switch(
        name = "Spams the cookie while in the cookie clicker menu",
        description = "Spams the cookie while in the cookie clicker menu",
        category = "QOL",
        subcategory = "Cookie Clicker",
        size = 2
    )
    var cookieClicker = false

    @KeyBind(
        name = "Ghost Pick",
        description = "Keybind to toggle ghostpick",
        category = "QOL",
        subcategory = "Ghost Pick",
        size = 2
    )
    var ghostPickKeybind = OneKeyBind(Keyboard.KEY_NONE)

    @Slider(
        name = "Slot to set ghostpick to",
        description = "Slot to set ghostpick to",
        category = "QOL",
        subcategory = "Ghost Pick",
        min = 1f,
        max = 9f,
        step = 1
    )
    var ghostPickSlot = 1f

    @Switch(
        name = "Pre ghost blocks clip spots in m7/f7 boss fight",
        description = "Pre ghost blocks clip spots in m7/f7 boss fight",
        category = "QOL",
        subcategory = "Ghost Block",
        size = 2
    )
    var preGhostBlock = false

    @KeyBind(
        name = "Keybind to ghostblock",
        description = "Keybind to ghostblock",
        category = "QOL",
        subcategory = "Ghost Block",
        size = 2
    )
    var ghostBlockBind = OneKeyBind(Keyboard.KEY_NONE)

    @Switch(
        name = "Shows messages on screen on specific events in kuudra",
        description = "Shows messages on screen on specific events in kuudra",
        category = "QOL",
        subcategory = "Kuudra Alerts",
        size = 2
    )
    var kuudraAlerts = false

    @Switch(
        name = "Disables Sword Block Animation",
        description = "Disables Sword Block Animation",
        category = "QOL",
        subcategory = "No Block Animation",
        size = 2
    )
    var noBlockAnimation = false

    @Switch(
        name = "Automatically picks up relics that are in range",
        description = "Automatically picks up relics that are in range",
        category = "QOL",
        subcategory = "Relic Aura",
        size = 2
    )
    var relicAura = false

    @Switch(
        name = "Clicks the server's tps while holding term and right click",
        description = "Clicks the server's tps while holding term and right click",
        category = "QOL",
        subcategory = "Terminator AC",
        size = 2
    )
    var terminatorAC = false

    @Checkbox(
        name = "Reminder to ready up when dungeon starts",
        description = "Reminder to ready up when dungeon starts",
        category = "QOL",
        subcategory = "Reminders",
        size = 2
    )
    var readyReminder = false

    @Checkbox(
        name = "Reminder to equip edrag when p5 starts",
        description = "Reminder to equip edrag when p5 starts",
        category = "QOL",
        subcategory = "Reminders",
        size = 2
    )
    var dragonReminder = false

    @Checkbox(
        name = "Reminder to use ult in  m6 and m7 for healer and tank",
        description = "Reminder to use ult in  m6 and m7 for healer and tank",
        category = "QOL",
        subcategory = "Reminders",
        size = 2
    )
    var ultReminder = false

    @HUD(
        name = "Server HUD",
        category = "QOL",
        subcategory = "Server"
    )
    var serverHUD: ServerHud = ServerHud()
}
