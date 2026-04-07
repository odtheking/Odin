@file:Suppress("unused")

package com.odtheking.odin.features

import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.impl.HUDSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.ModuleManager.configs
import com.odtheking.odin.features.impl.boss.*
import com.odtheking.odin.features.impl.dungeon.*
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.map.DungeonMapModule
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import com.odtheking.odin.features.impl.nether.*
import com.odtheking.odin.features.impl.render.*
import com.odtheking.odin.features.impl.skyblock.*
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.Identifier
import net.minecraft.resources.Identifier.fromNamespaceAndPath
import java.io.File

/**
 * # Module Manager
 *
 * This object stores all [Modules][Module] and provides functionality to [HUDs][Module.HUD]
 */
object ModuleManager {

    /**
     * Map containing all modules in Odin,
     * where the key is the modules name in lowercase.
     */
    val modules: HashMap<String, Module> = linkedMapOf()

    /**
     * Map containing all modules under their category.
     */
    val modulesByCategory: HashMap<Category, ArrayList<Module>> = hashMapOf()

    /**
     * List of all configurations handled by Odin.
     */
    val configs: ArrayList<ModuleConfig> = arrayListOf()

    val keybindSettingsCache: ArrayList<KeybindSetting> = arrayListOf()
    val hudSettingsCache: ArrayList<HUDSetting> = arrayListOf()

    private val HUD_LAYER: Identifier = fromNamespaceAndPath(OdinMod.MOD_ID, "odin_hud")

    init {
        registerModules(config = ModuleConfig(file = File(OdinMod.configFile, "odin-config.json")),
            // dungeon
            PuzzleSolvers, BlessingDisplay, LeapMenu, SecretClicked, MapInfo, Mimic, DungeonQueue,
            KeyHighlight, BloodCamp, PositionalMessages, TerracottaTimer, BreakerDisplay, LividSolver,
            InvincibilityTimer, SpiritBear, DungeonWaypoints, ExtraStats, BetterPartyFinder, Croesus, MageBeam, DungeonMap,
            DungeonMapModule,

            // floor 7
            TerminalSimulator, TerminalSolver, TerminalTimes, TerminalSounds, TickTimers, ArrowAlign,
            InactiveWaypoints, MelodyMessage, WitherDragons, SimonSays, KingRelics, ArrowsDevice,

            // render
            ClickGUIModule, Camera, Etherwarp, PlayerSize, PerformanceHUD, RenderOptimizer,
            PlayerDisplay, Waypoints, HidePlayers, Highlight, GyroWand,

            //skyblock
            ChatCommands, NoCursorReset, Ragnarock, SpringBoots, WardrobeKeybinds, PetKeybinds, AutoSprint,
            CommandKeybinds, SlotBinds, Splits,

            // nether
            SupplyHelper, BuildHelper, RemovePerks, NoPre, PearlWaypoints, FreshTools, KuudraInfo, Misc, Vesuvius
        )

        // hashmap, but would need to keep track when setting values change
        on<InputEvent> {
            for (setting in keybindSettingsCache) {
                if (setting.value.value == key.value) setting.onPress?.invoke()
            }
        }

        HudElementRegistry.attachElementBefore(VanillaHudElements.SLEEP, HUD_LAYER, ModuleManager::render)
    }

    /**
     * Registers modules to the [ModuleManager] and initializes them.
     *
     * @param config the config the [Module] is saved to,
     * it is recommended that each unique mod that uses this has its own config
     */
    fun registerModules(config: ModuleConfig, vararg modules: Module) {
        for (module in modules) {
            if (module.isDevModule && !FabricLoader.getInstance().isDevelopmentEnvironment) continue

            val lowercase = module.name.lowercase()
            config.modules[lowercase] = module
            this.modules[lowercase] = module
            this.modulesByCategory.getOrPut(module.category) { arrayListOf() }.add(module)

            module.key?.let { keybind ->
                val setting = KeybindSetting("Keybind", keybind, "Toggles this module.")
                setting.onPress = module::onKeybind
                module.registerSetting(setting)
            }

            for ((_, setting) in module.settings) {
                when (setting) {
                    is KeybindSetting -> keybindSettingsCache.add(setting)
                    is HUDSetting -> hudSettingsCache.add(setting)
                }
            }
        }
        configs.add(config)
        config.load()
    }

    /**
     * Loads all [configs] from disk, into the respective modules.
     */
    fun loadConfigurations() {
        for (config in configs) {
            config.load()
        }
    }

    /**
     * Saves all [configs] to disk, from the respective modules.
     */
    fun saveConfigurations() {
        for (config in configs) {
            config.save()
        }
    }

    fun render(guiGraphics: GuiGraphics, tickCounter: DeltaTracker) {
        if (mc.level == null || mc.player == null || mc.screen == HudManager || mc.options.hideGui) return

        guiGraphics.pose().pushMatrix()
        val sf = mc.window.guiScale
        guiGraphics.pose().scale(1f / sf, 1f / sf)
        for (hudSettings in hudSettingsCache) {
            if (hudSettings.isEnabled) hudSettings.value.draw(guiGraphics, false)
        }
        guiGraphics.pose().popMatrix()
    }
}