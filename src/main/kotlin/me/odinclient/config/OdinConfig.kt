package me.odinclient.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.features.impl.dungeon.AutoSell
import me.odinclient.hud.*


object OdinConfig : Config(Mod("OdinClient", ModType.SKYBLOCK, "/assets/odinclient/LogoSmall.png"), "odinclient.json") {

    fun init() {
        initialize()
    }

    // Dungeon



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

    @HUD(
        name = "Deployable HUD",
        category = "General",
        subcategory = "Deployable Timer"
    )
    var deployableHud: DeployableHud = DeployableHud()




    @HUD(
        name = "Dragon Timer Hud",
        category = "M7",
        subcategory = "Dragon Timer"
    )
    var dragonTimerHud: DragonTimerHud = DragonTimerHud()



    @HUD(
        name = "Server HUD",
        category = "QOL",
        subcategory = "Server"
    )
    var serverHUD: ServerHud = ServerHud()
}
