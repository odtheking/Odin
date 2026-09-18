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
import com.odtheking.odin.features.impl.boss.*
import com.odtheking.odin.features.impl.dungeon.*
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.map.DungeonMap
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import com.odtheking.odin.features.impl.nether.*
import com.odtheking.odin.features.impl.render.*
import com.odtheking.odin.features.impl.skyblock.*
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
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
     * Map containing all modules under their category.
     */
    val modulesByCategory: HashMap<Category, ArrayList<Module>> = hashMapOf()

    /**
     * The module config of the Odin itself.
     */
    val odinModuleConfig: ModuleConfig = ModuleConfig(file = File(OdinMod.configFile, "odin-config.json"))

    /**
     * List of all configurations handled by Odin.
     */
    val configs: ArrayList<ModuleConfig> = arrayListOf()

    val keybindSettingsCache: ArrayList<KeybindSetting> = arrayListOf()
    val hudSettingsCache: ArrayList<HUDSetting> = arrayListOf()

    private val HUD_LAYER: Identifier = fromNamespaceAndPath(OdinMod.MOD_ID, "odin_hud")

    init {
        registerModules(config = odinModuleConfig,
            // dungeon
            PuzzleSolvers, BlessingDisplay, LeapMenu, SecretClicked, MapInfo, Mimic, DungeonQueue,
            DoorHighlight, BloodCamp, PositionalMessages, TerracottaTimer, BreakerDisplay, LividSolver,
            InvincibilityTimer, SpiritBear, DungeonWaypoints, ExtraStats, BetterPartyFinder, Croesus, MageBeam,
            SecretsCounter, DungeonMap, PuzzleHud, RoomClear,

            // boss
            TerminalSimulator, TerminalSolver, TerminalTimes, TerminalSounds, TickTimers, ArrowAlign,
            InactiveWaypoints, MelodyMessage, WitherDragons, SimonSays, KingRelics, ArrowsDevice, TerminalTitles,

            // render
            ClickGUIModule, Camera, Etherwarp, PlayerSize, PerformanceHUD, RenderOptimizer,
            PlayerDisplay, Waypoints, HidePlayers, Highlight, GyroWand,

            //skyblock
            ChatCommands, NoCursorReset, Ragnarock, SpringBoots, WardrobeKeybinds, PetKeybinds, AutoSprint,
            CommandKeybinds, SlotBinds, Splits, LoadoutKeybinds, QuiverDisplay,

            // nether
            SupplyHelper, BuildHelper, RemovePerks, NoPre, PearlWaypoints, FreshTools, KuudraInfo, Misc, Vesuvius,
            KuudraTracker,

            RenderTest,
        )

        // hashmap, but would need to keep track when setting values change
        on<InputEvent> {
            for (setting in keybindSettingsCache) {
                if (setting.boundKey.value == key.value) setting.onPress?.invoke()
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
            this.modulesByCategory.getOrPut(module.category) { arrayListOf() }.add(module)

            module.key?.let { keybind ->
                val setting = KeybindSetting("Keybind", keybind, "Toggles this module.")
                setting.onPress = module::onKeybind
                module.registerSetting(setting)
            }

            for ((_, setting) in module.settings) {
                when (setting) {
                    is KeybindSetting -> {
                        keybindSettingsCache.add(setting)
                        val keyMappingId = if (config == odinModuleConfig) {
                            module.name
                        } else {
                            // Separate by . instead of :, as options.txt is a properties file where value comes after :
                            "${config.namespace}.${module.name}"
                        }
                        setting.registerKeyMapping(keyMappingId)
                    }
                    is HUDSetting -> hudSettingsCache.add(setting)
                }
            }
        }
        configs.add(config)
        config.load()
    }

    /**
     * Gets a module by its name, or namespace and name (separated by :).
     *
     * If unspecified, namespace is assumed to be odin.
     */
    fun getModule(identifier: String): Module? {
        val normalized = identifier.replace("_", " ").lowercase()

        val separator = normalized.indexOf(':')
        if (separator >= 0) {
            val configId = normalized.substring(0, separator)
            val moduleName = normalized.substring(separator + 1)

            return configs
                .firstOrNull { it.namespace == configId }
                ?.modules
                ?.get(moduleName)
        }

        return odinModuleConfig.modules[normalized]
    }

    /**
     * Gets all module identifiers. Odin modules are ordered before any addon module in the returned list.
     */
    fun getModuleIdentifiers(): List<String> {
        return buildList {
            addAll(
                odinModuleConfig.modules.keys.map { it.replace(" ", "_") }
            )

            addAll(
                configs
                    .filter { it !== odinModuleConfig }
                    .flatMap { config ->
                        config.modules.keys.map { module ->
                            "${config.namespace}:${module.replace(" ", "_")}"
                        }
                    }
            )
        }
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
        KeybindSetting.saveOptionsIfChanged()
    }

    fun render(guiGraphics: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        if (mc.level == null || mc.player == null || mc.gui.screen() == HudManager) return

        for (hudSetting in hudSettingsCache) {
            if (hudSetting.isEnabled) hudSetting.hud.draw(guiGraphics, false)
        }
    }
}
