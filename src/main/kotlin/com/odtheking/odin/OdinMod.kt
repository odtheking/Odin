package com.odtheking.odin

import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.ui.Panel
import com.odtheking.odin.commands.*
import com.odtheking.odin.events.EventDispatcher
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.dungeon.map.DungeonScan
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.features.impl.render.Shenanigans
import com.odtheking.odin.utils.IrisCompatability
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.handlers.TickTasks
import com.odtheking.odin.utils.render.ItemStateRenderer
import com.odtheking.odin.utils.render.NoDepthFeatureRenderer
import com.odtheking.odin.utils.skyblock.*
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.odtheking.odin.utils.ui.widget.CustomGUIImpl
import kotlinx.coroutines.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

object OdinMod : ClientModInitializer {

    val logger: Logger = LogManager.getLogger("Odin")

    @JvmStatic
    val mc: Minecraft = Minecraft.getInstance()

    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)

    val configFile: File = File(mc.gameDirectory, "config/odin/").also { dir ->
        scope.launch(ioDispatcher) {
            try {
                if (dir.isFile) dir.delete() // Delete old bugged files that prevent creating the directory
                if (!dir.exists()) dir.mkdirs()
            } catch (e: Exception) {
                logger.error("Error initializing module config", e)
            }
        }
    }

    const val MOD_ID = "odin"

    val version: Version by lazy { FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata.version }

    val clickGUI: ClickGUI by lazy {
        ClickGUI(Category.categories.values.mapNotNull { category ->
            val modules = ModuleManager.odinConfig.modules.values.filter { it.category == category }.ifEmpty { return@mapNotNull null }
            val data = ClickGUIModule.panelSetting.getOrPut(category.name) { ClickGUIModule.PanelData(category.x, category.y) }
            Panel(category, modules, data)
        })
    }

    override fun onInitializeClient() {
        FeatureRendererRegistry.register(NoDepthFeatureRenderer.TYPE) { NoDepthFeatureRenderer() }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(
                mainCommand, devCommand, waypointCommand,
                soopyCommand, termSimCommand,
                dungeonWaypointsCommand, cataCommand
            ).forEach { commodore -> commodore.register(dispatcher) }
        }

        listOf(
            this, LocationUtils, TickTasks, KuudraUtils,
            ActionBarListener, ServerUtils, EventDispatcher,
            DungeonListener, PartyUtils, TerminalUtils,
            DungeonUtils, SplitsManager, DungeonScan,
            IrisCompatability,
            ModuleManager, CustomGUIImpl, Shenanigans,
        ).forEach { EventBus.subscribe(it) }

        PictureInPictureRendererRegistry.register { ItemStateRenderer() }
    }
}