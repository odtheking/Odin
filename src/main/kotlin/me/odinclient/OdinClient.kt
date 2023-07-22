package me.odinclient

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.odinclient.commands.impl.*
import me.odinclient.config.Config
import me.odinclient.config.MiscConfig
import me.odinclient.config.OdinConfig
import me.odinclient.config.WaypointConfig
import me.odinclient.events.ClientSecondEvent
import me.odinclient.features.ModuleManager
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.clock.AbstractExecutor
import me.odinclient.utils.render.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File

@Mod(
    modid = OdinClient.MOD_ID,
    name = OdinClient.NAME,
    version = OdinClient.VERSION,
    clientSideOnly = true
)
class OdinClient {

    @EventHandler
    fun init(event: FMLInitializationEvent) {

        config.init()

        listOf(
            LocationUtils,
            ChatUtils,
            ServerUtils,
            PlayerUtils,
            RenderUtils,
            DungeonUtils,

            AbstractExecutor,
            ModuleManager,
            this
        ).forEach {
            MinecraftForge.EVENT_BUS.register(it)
        }


        for (command in commandList) {
            ClientCommandHandler.instance.registerCommand(command)
        }
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) = runBlocking {
        launch(Dispatchers.IO) {
            miscConfig.loadConfig()
            waypointConfig.loadConfig()
        }
    }

    @EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) = runBlocking {
        runBlocking {
            launch {
                moduleConfig.loadConfig()
            }
        }
        ModuleManager.initializeModules()
        ClickGUI.init()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickRamp++

        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }

        if (tickRamp % 20 == 0) {
            if (mc.thePlayer != null) MinecraftForge.EVENT_BUS.post(ClientSecondEvent())
            tickRamp = 0
        }
    }

    @SubscribeEvent
    fun onWorldLoad(@Suppress("UNUSED_PARAMETER") event: WorldEvent.Load) {
        tickRamp = 18
    }

    companion object {
        const val MOD_ID = "OdinClient"
        const val NAME = "OdinClient"
        const val VERSION = "1.0.3"

        val mc: Minecraft = Minecraft.getMinecraft()

        val moduleConfig = Config(File(mc.mcDataDir, "config/odinclient"))

        var config = OdinConfig
        val miscConfig = MiscConfig(File(mc.mcDataDir, "config/odin"))
        val waypointConfig = WaypointConfig(File(mc.mcDataDir, "config/odin"))
        var display: GuiScreen? = null
        var tickRamp = 0

        val commandList = arrayOf(
            OdinCommand,
            ESPCommand,
            WaypointCommand,
            BlacklistCommand,
            AutoSellCommand,
        )
    }
}
