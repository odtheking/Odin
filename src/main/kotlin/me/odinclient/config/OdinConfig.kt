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
