package com.odtheking.odin

import com.odtheking.odin.commands.*
import com.odtheking.odin.events.EventDispatcher
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.IrisCompatability
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.handlers.TickTasks
import com.odtheking.odin.utils.network.WebUtils.postData
import com.odtheking.odin.utils.render.ItemStateRenderer
import com.odtheking.odin.utils.render.RenderBatchManager
import com.odtheking.odin.utils.skyblock.*
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.odtheking.odin.utils.ui.rendering.NVGPIPRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry
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

    /**
     * Main config file location.
     * @see com.odtheking.odin.config.ModuleConfig
     * @see com.odtheking.odin.config.DungeonWaypointConfig
     */
    val configFile: File = File(mc.gameDirectory, "config/odin/").apply {
        try {
            if (isFile()) delete() // Delete old bugged files that prevent creating the directory
            if (!exists()) mkdirs()
        } catch (e: Exception) {
            println("Error initializing module config\n${e.message}")
            logger.error("Error initializing module config", e)
        }
    }

    const val MOD_ID = "odin"

    val version: Version by lazy { FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata.version }
    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)

    override fun onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(
                mainCommand, petCommand, devCommand, waypointCommand,
                soopyCommand, termSimCommand, posMsgCommand,
                dungeonWaypointsCommand, cataCommand
            ).forEach { commodore -> commodore.register(dispatcher) }
        }

        listOf(
            this, LocationUtils, TickTasks, KuudraUtils,
            SkyblockPlayer, ServerUtils, EventDispatcher,
            DungeonListener, PartyUtils, TerminalUtils,
            ScanUtils, DungeonUtils, SplitsManager,
            IrisCompatability, RenderBatchManager,
            ModuleManager
        ).forEach { EventBus.subscribe(it) }

        SpecialGuiElementRegistry.register { context ->
            NVGPIPRenderer(context.vertexConsumers())
        }

        SpecialGuiElementRegistry.register { context ->
            ItemStateRenderer(context.vertexConsumers())
        }

        val name = mc.user?.name?.takeIf { !it.matches(Regex("Player\\d{2,3}")) } ?: return
        scope.launch {
            postData("https://api.odtheking.com/tele/", """{"username": "$name", "version": "Fabric $version"}""")
        }
    }
}
