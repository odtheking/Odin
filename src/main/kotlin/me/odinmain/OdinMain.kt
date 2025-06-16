package me.odinmain

import kotlinx.coroutines.*
import me.odinmain.commands.CommandRegistry
import me.odinmain.config.Config
import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.config.PBConfig
import me.odinmain.events.EventDispatcher
import me.odinmain.features.ModuleManager
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.RandomPlayers
import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.font.OdinFont
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.SplitsManager
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils2D
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonListener
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.ScanUtils
import me.odinmain.utils.ui.clickgui.ClickGUI
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

object OdinMain {
    val mc: Minecraft = Minecraft.getMinecraft()

    const val VERSION = "@VER@"
    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)
    val logger: Logger = LogManager.getLogger("Odin")

    var display: GuiScreen? = null
    inline val isLegitVersion: Boolean
        get() = Loader.instance().activeModList.none { it.modId == "odclient" }

    fun init() {
        PBConfig.loadConfig()
        listOf(
            LocationUtils, ServerUtils, PlayerUtils,
            RenderUtils, Renderer, DungeonUtils, KuudraUtils,
            EventDispatcher, Executor, ModuleManager,
            WaypointManager, RandomPlayers, SkyblockPlayer,
            ScanUtils, HighlightRenderer, DungeonListener,
            SplitsManager, RenderUtils2D, ArrowTracker,
            this
        ).forEach { MinecraftForge.EVENT_BUS.register(it) }

        CommandRegistry.register()
        OdinFont.init()
        scope.launch(Dispatchers.IO) { RandomPlayers.preloadCapes() }
    }

    fun postInit() {
        File(mc.mcDataDir, "config/odin").takeIf { !it.exists() }?.mkdirs()
    }

    fun loadComplete() {
        runBlocking(Dispatchers.IO) {
            Config.load()
            ClickGUIModule.lastSeenVersion = VERSION
        }
        ClickGUI.init()

        val name = mc.session?.username?.takeIf { !it.matches(Regex("Player\\d{2,3}")) } ?: return
        scope.launch(Dispatchers.IO) {
            DungeonWaypointConfig.loadConfig()
            ClickGUIModule.latestVersionNumber = ClickGUIModule.checkNewerVersion(VERSION)
            sendDataToServer(body = """{"username": "$name", "version": "${if (isLegitVersion) "legit" else "cheater"} $VERSION"}""")
        }
    }

    fun onTick() {
        if (display == null) return
        mc.displayGuiScreen(display)
        display = null
    }
}
