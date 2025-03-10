package me.odinmain

import kotlinx.coroutines.*
import me.odinmain.commands.CommandRegistry
import me.odinmain.config.Config
import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.config.PBConfig
import me.odinmain.events.EventDispatcher
import me.odinmain.features.ModuleManager
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.DevPlayers
import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.util.shader.RoundedRect
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.SplitsManager
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils2D
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.SkyblockPlayer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.ScanUtils
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
            WaypointManager, DevPlayers, SkyblockPlayer,
            ScanUtils, HighlightRenderer, //OdinUpdater,
            SplitsManager, RenderUtils2D,
            this
        ).forEach { MinecraftForge.EVENT_BUS.register(it) }

        CommandRegistry.register()
        OdinFont.init()
        scope.launch(Dispatchers.IO) { DevPlayers.preloadCapes() }
    }

    fun postInit() {
        File(mc.mcDataDir, "config/odin").takeIf { !it.exists() }?.mkdirs()
        scope.launch(Dispatchers.IO) { DungeonWaypointConfig.loadConfig() }
    }

    fun loadComplete() {
        runBlocking(Dispatchers.IO) {
            launch {
                Config.load()
                ClickGUIModule.firstTimeOnVersion = ClickGUIModule.lastSeenVersion != VERSION
                ClickGUIModule.lastSeenVersion = VERSION
            }.join() // Ensure Config.load() and version checks are complete before proceeding
        }
        ClickGUI.init()
        RoundedRect.initShaders()

        val name = mc.session?.username?.takeIf { !it.matches(Regex("Player\\d{2,3}")) } ?: return
        scope.launch(Dispatchers.IO) {
            sendDataToServer(body = """{"username": "$name", "version": "${if (isLegitVersion) "legit" else "cheater"} $VERSION"}""")
        }
    }

    fun onTick() {
        if (display == null) return
        mc.displayGuiScreen(display)
        display = null
    }
}
