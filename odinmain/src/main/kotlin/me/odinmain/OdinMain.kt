package me.odinmain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.odinmain.commands.impl.*
import me.odinmain.config.Config
import me.odinmain.config.MiscConfig
import me.odinmain.config.WaypointConfig
import me.odinmain.events.EventDispatcher
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
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
    modid = OdinMain.MOD_ID,
    name = OdinMain.NAME,
    version = OdinMain.VERSION,
    clientSideOnly = true
)
class OdinMain {

    @EventHandler
    fun init(event: FMLInitializationEvent) {

        listOf(
            LocationUtils,
            ChatUtils,
            ServerUtils,
            PlayerUtils,
            RenderUtils,
            DungeonUtils,

            EventDispatcher,

            Executor,
            ModuleManager,
            WaypointManager,,
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
        launch {
            miscConfig.loadConfig()
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
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
    }
    companion object {
        const val MOD_ID = "OdinMain"
        const val NAME = "OdinMain"
        const val VERSION = "1.1.1"

        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()

        // TODO: Remove
        val miscConfig = MiscConfig(File(mc.mcDataDir, "config/odin"))
        var display: GuiScreen? = null

        val scope = CoroutineScope(EmptyCoroutineContext)

        val commandList = arrayOf(
            OdinCommand,
            ESPCommand,
            WaypointCommand,
            BlacklistCommand,
            AutoSellCommand,
        )
    }

}
