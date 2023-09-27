package me.odinclient

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.odinclient.commands.impl.*
import me.odinclient.config.Config
import me.odinclient.config.MiscConfig
import me.odinclient.config.WaypointConfig
import me.odinclient.dungeonmap.features.Dungeon
import me.odinclient.dungeonmap.features.MapRender
import me.odinclient.dungeonmap.features.Window
import me.odinclient.events.EventDispatcher
import me.odinclient.features.ModuleManager
import me.odinclient.features.impl.render.ClickGUIModule
import me.odinclient.features.impl.render.WaypointManager
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.clock.Executor
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("UNUSED_PARAMETER")
@Mod(
    modid = OdinClient.MOD_ID,
    name = OdinClient.NAME,
    version = OdinClient.VERSION,
    clientSideOnly = true
)
class OdinClient {

    @EventHandler
    fun init(event: FMLInitializationEvent) {

        window.init()

        listOf(
            LocationUtils,
            ChatUtils,
            ServerUtils,
            PlayerUtils,
            RenderUtils,
            DungeonUtils,

            Dungeon,
            MapRender,

            EventDispatcher,

            Executor,
            ModuleManager,
            WaypointManager,
            this
        ).forEach {
            MinecraftForge.EVENT_BUS.register(it)
        }

        for (command in commandList) {
            ClientCommandHandler.instance.registerCommand(command)
        }
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) = scope.launch(Dispatchers.IO) {

        val config = File(mc.mcDataDir, "config/odin")
        if (!config.exists()) {
            config.mkdirs()
        }

        launch {
            MiscConfig.loadConfig()
        }
        launch {
            WaypointConfig.loadConfig()
        }
    }

    @EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) = runBlocking {
        runBlocking {
            launch {
                Config.loadConfig()

                ClickGUIModule.firstTimeOnVersion = ClickGUIModule.lastSeenVersion != VERSION
                ClickGUIModule.lastSeenVersion = VERSION
            }
        }
        ClickGUI.init()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        if (window.isVisible != window.shouldShow) window.isVisible = window.shouldShow

        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
    }

    companion object {
        const val MOD_ID = "OdinClient"
        const val NAME = "OdinClient"
        const val VERSION = "1.1.2"

        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()

        var window = Window

        var display: GuiScreen? = null

        val scope = CoroutineScope(EmptyCoroutineContext)

        val commandList = arrayOf(
            mainCommand,
            termSimCommand,
            autoSellCommand,
            blacklistCommand,
            ESPCommand,
            WaypointCommand,
        )
    }
}
