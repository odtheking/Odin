package me.odinmain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.odinmain.commands.impl.*
import me.odinmain.commands.register
import me.odinmain.config.Config
import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.config.MiscConfig
import me.odinmain.config.WaypointConfig
import me.odinmain.events.EventDispatcher
import me.odinmain.features.ModuleManager
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.DevPlayers
import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

object OdinMain {
    val mc: Minecraft = Minecraft.getMinecraft()

    const val VERSION = "1.2.4"
    val scope = CoroutineScope(EmptyCoroutineContext)

    var display: GuiScreen? = null
    var onLegitVersion = true

    object MapColors {
        var bloodColor = Color.WHITE
        var miniBossColor = Color.WHITE
        var entranceColor = Color.WHITE
        var fairyColor = Color.WHITE
        var puzzleColor = Color.WHITE
        var rareColor = Color.WHITE
        var trapColor = Color.WHITE
        var mimicRoomColor = Color.WHITE
        var roomColor = Color.WHITE
        var bloodDoorColor = Color.WHITE
        var entranceDoorColor = Color.WHITE
        var openWitherDoorColor = Color.WHITE
        var witherDoorColor = Color.WHITE
        var roomDoorColor = Color.WHITE
    }

    fun init() {
        listOf(
            LocationUtils,
            ServerUtils,
            PlayerUtils,
            RenderUtils,
            DungeonUtils,
            EventDispatcher,
            Executor,
            ModuleManager,
            WaypointManager,
            DevPlayers,
            this
        ).forEach { MinecraftForge.EVENT_BUS.register(it) }

        register(
            DevCommand,
            OdinCommand,
            TermsimCommand,
            WaypointCommand,
            BlacklistCommand
        )
    }

    fun postInit() = scope.launch(Dispatchers.IO) {
        val config = File(mc.mcDataDir, "config/odin")
        if (!config.exists()) {
            config.mkdirs()
        }

        launch { MiscConfig.loadConfig() }
        launch { WaypointConfig.loadConfig() }
        launch { DungeonWaypointConfig.loadConfig() }
    }

    fun loadComplete() = runBlocking {
        runBlocking {
            launch {
                Config.loadConfig()

                ClickGUIModule.firstTimeOnVersion = ClickGUIModule.lastSeenVersion != VERSION
                ClickGUIModule.lastSeenVersion = VERSION
            }
        }
        ClickGUI.init()
    }

    fun onTick() {
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
    }
}
