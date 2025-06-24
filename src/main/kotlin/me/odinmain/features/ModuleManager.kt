package me.odinmain.features

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.InputEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.impl.dungeon.*
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import me.odinmain.features.impl.floor7.TerminalSimulator
import me.odinmain.features.impl.floor7.TickTimers
import me.odinmain.features.impl.floor7.WitherDragons
import me.odinmain.features.impl.floor7.p3.*
import me.odinmain.features.impl.nether.*
import me.odinmain.features.impl.render.*
import me.odinmain.features.impl.skyblock.*
import me.odinmain.features.settings.impl.KeybindSetting
import net.minecraft.network.Packet
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * # Module Manager
 *
 * This object stores all [Modules][Module] and provides functionality to [HUDs][Module.HUD]
 */
object ModuleManager {

    data class PacketFunction<T : Packet<*>>(
        val type: Class<T>,
        val function: (T) -> Unit,
        val shouldRun: () -> Boolean,
    )

    data class MessageFunction(val filter: Regex, val shouldRun: () -> Boolean, val function: (String) -> Unit)

    data class TickTask(var ticksLeft: Int, val server: Boolean, val function: () -> Unit)

    val packetFunctions = mutableListOf<PacketFunction<Packet<*>>>()
    val messageFunctions = mutableListOf<MessageFunction>()
    val worldLoadFunctions = mutableListOf<() -> Unit>()
    val tickTasks = mutableListOf<TickTask>()

    val modules: ArrayList<Module> = arrayListOf(
        // dungeon
        DungeonRequeue, BlessingDisplay, PosMessages, ExtraStats, KeyHighlight, Mimic, TeammatesHighlight,
        TerracottaTimer, BloodCamp, SecretClicked, DungeonWaypoints, LeapMenu, PuzzleSolvers,
        WarpCooldown, MapInfo, SwapSound,

        // floor 7
        TerminalSolver, TerminalTimes, MelodyMessage, TickTimers, InactiveWaypoints, WitherDragons,
        TerminalSimulator, TerminalSounds, ArrowAlign,

        // render
        BPSDisplay, CustomHighlight, CPSDisplay, DragonHitboxes, GyroWand, NameChanger,
        PersonalDragon, RenderOptimizer, ServerHud, Waypoints, CanClip, Animations, SpaceHelmet,
        BlockOverlay, VisualWords, DVD, Sidebar, HideArmor, ClickGUI,

        //skyblock
        NoCursorReset, AutoSprint, BlazeAttunement, ChatCommands, DeployableTimer, DianaHelper, ArrowHit,
        RagnarokAxe, MobSpawn, Splits, WardrobeKeybinds, InvincibilityTimer, ItemsHighlight, PlayerDisplay,
        FarmKeys, PetKeybinds, CommandKeybinds, SpringBoots, AbilityTimers,

        // kuudra
        BuildHelper, FreshTimer, KuudraDisplay, NoPre, PearlWaypoints, RemovePerks, SupplyHelper, TeamHighlight,
        VanqNotifier, KuudraReminders, KuudraRequeue, EnrageDisplay
    )

    init {
        for (module in modules) {
            module.keybinding?.let {
                module.register(KeybindSetting("Keybind", it, "Toggles the module"))
            }
        }
    }

    fun addModules(vararg module: Module) {
        for (i in module) {
            modules.add(i)
            i.keybinding?.let { i.register(KeybindSetting("Keybind", it, "Toggles the module")) }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickTaskTick()
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onServerTick(event: ServerTickEvent) {
        tickTaskTick(true)
    }

    private fun tickTaskTick(server: Boolean = false) {
        tickTasks.removeAll {
            if (it.server != server) return@removeAll false
            if (it.ticksLeft <= 0) {
                it.function()
                return@removeAll true
            }
            it.ticksLeft--
            false
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onReceivePacket(event: PacketEvent.Receive) {
        packetFunctions.forEach {
            if (it.type.isInstance(event.packet) && it.shouldRun.invoke()) it.function(event.packet)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onSendPacket(event: PacketEvent.Send) {
        packetFunctions.forEach {
            if (it.type.isInstance(event.packet) && it.shouldRun.invoke()) it.function(event.packet)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatPacket(event: ChatPacketEvent) {
        messageFunctions.forEach {
            if (event.message matches it.filter && it.shouldRun()) it.function(event.message)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        worldLoadFunctions
            .forEach { it.invoke() }
    }

    @SubscribeEvent
    fun activateModuleKeyBinds(event: InputEvent.Keyboard) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key == event.keycode) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: InputEvent.Mouse) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key + 100 == event.keycode) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateFeatureList(): String {
        // todo, readd textwidth
        val sortedCategories = modules.sortedByDescending { it.name.length }.groupBy { it.category }.entries
            .sortedBy { Category.entries.associateWith { it.ordinal }[it.key] }

        val featureList = StringBuilder()
         for ((category, modulesInCategory) in sortedCategories) {
             featureList.appendLine("Category: ${category.displayName}")
             for (module in modulesInCategory) {
                 featureList.appendLine("- ${module.name}: ${module.description}")
             }
             featureList.appendLine()
         }
         return featureList.toString()
    }
}