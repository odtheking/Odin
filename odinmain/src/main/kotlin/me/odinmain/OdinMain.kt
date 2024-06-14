package me.odinmain

import kotlinx.coroutines.*
import me.odinmain.commands.impl.*
import me.odinmain.commands.registerCommands
import me.odinmain.config.*
import me.odinmain.events.EventDispatcher
import me.odinmain.features.ModuleManager
import me.odinmain.features.impl.render.*
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.util.shader.RoundedRect
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.ScanUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

object OdinMain {
    val mc: Minecraft = Minecraft.getMinecraft()

    const val VERSION = "@VER@"
    val scope = CoroutineScope(EmptyCoroutineContext)

    var display: GuiScreen? = null
    val isLegitVersion: Boolean
        get() = Loader.instance().activeModList.none { it.modId == "odclient" }

    fun init() {
        scope.launch(Dispatchers.IO) {
            PBConfig.loadConfig()
        }
        listOf(
            LocationUtils, ServerUtils, PlayerUtils,
            RenderUtils, Renderer, DungeonUtils, KuudraUtils,
            EventDispatcher, Executor, ModuleManager,
            WaypointManager, DevPlayers, SkyblockPlayer,
            ScanUtils, //HighlightRenderer, //OdinUpdater,
            this
        ).forEach { MinecraftForge.EVENT_BUS.register(it) }

        registerCommands(
            mainCommand, soopyCommand,
            termSimCommand, blacklistCommand,
            devCommand, highlightCommand,
            waypointCommand, dungeonWaypointsCommand,
            petCommand, visualWordsCommand
        )
        OdinFont.init()
    }

    fun postInit() = scope.launch(Dispatchers.IO) {
        val config = File(mc.mcDataDir, "config/odin")
        if (!config.exists()) config.mkdirs()
        launch { WaypointConfig.loadConfig() }
        launch { DungeonWaypointConfigCLAY.loadConfig() }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun loadComplete() = runBlocking {
        runBlocking {
            launch {
                Config.load()
                ClickGUIModule.firstTimeOnVersion = ClickGUIModule.lastSeenVersion != VERSION
                ClickGUIModule.lastSeenVersion = VERSION
            }
        }
        ClickGUI.init()
        RoundedRect.initShaders()
        GlobalScope.launch {
            val name = mc.session?.username ?: return@launch
            if (name.matches(Regex("Player\\d{2,3}"))) return@launch
            sendDataToServer(body = """{"username": "$name", "version": "${if (isLegitVersion) "legit" else "cheater"} $VERSION"}""")
        }
    }

    fun onTick() {
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
    }
}
