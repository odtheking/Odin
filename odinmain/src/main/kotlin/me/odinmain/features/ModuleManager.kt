package me.odinmain.features

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.*
import me.odinmain.features.impl.dungeon.*
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import me.odinmain.features.impl.floor7.NecronDropTimer
import me.odinmain.features.impl.floor7.WitherDragons
import me.odinmain.features.impl.floor7.p3.*
import me.odinmain.features.impl.kuudra.*
import me.odinmain.features.impl.render.*
import me.odinmain.features.impl.render.ClickGUIModule.hudChat
import me.odinmain.features.impl.skyblock.*
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.profile
import me.odinmain.utils.render.getTextWidth
import net.minecraft.network.Packet
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Class that contains all Modules and huds
 * @author Aton, Bonsai
 */
object ModuleManager {
    data class PacketFunction<T : Packet<*>>(
        val type: Class<T>,
        val function: (T) -> Unit,
        val shouldRun: () -> Boolean,
    )

    data class MessageFunction(val filter: Regex, val shouldRun: () -> Boolean, val function: (String) -> Unit)
    data class MessageFunctionCancellable(val filter: Regex, val shouldRun: () -> Boolean, val function: (ChatPacketEvent) -> Unit)

    data class TickTask(var ticksLeft: Int, val function: () -> Unit)

    val packetFunctions = mutableListOf<PacketFunction<Packet<*>>>()
    val messageFunctions = mutableListOf<MessageFunction>()
    val cancellableMessageFunctions = mutableListOf<MessageFunctionCancellable>()
    val worldLoadFunctions = mutableListOf<() -> Unit>()
    val tickTasks = mutableListOf<TickTask>()
    val huds = arrayListOf<HudElement>()
    val executors = ArrayList<Pair<Module, Executor>>()

    val modules: ArrayList<Module> = arrayListOf(
        DungeonRequeue,
        BlessingDisplay,
        ExtraStats,
        KeyHighlight,
        MimicMessage,
        TeammatesHighlight,
        TerracottaTimer,
        WatcherBar,
        TerminalSolver,
        TerminalTimes,
        MelodyMessage,
        NecronDropTimer,
        BPSDisplay,
        Camera,
        ClickedSecrets,
        ClickGUIModule,
        CustomHighlight,
        CPSDisplay,
        DragonHitboxes,
        GyroWand,
        NameChanger,
        NoCursorReset,
        PersonalDragon,
        RenderOptimizer,
        ServerDisplay,
        Waypoints,
        AutoSprint,
        BlazeAttunement,
        CanClip,
        ChatCommands,
        DeployableTimer,
        DianaHelper,
        Reminders,
        Animations,
        SpaceHelmet,
        DungeonWaypoints,
        SecretChime,
        LeapMenu,
        PuzzleSolvers,
        ArrowHit,
        InactiveWaypoints,
        Ragaxe,
        MobSpawn,
        WitherDragons,
        BuildHelper,
        FreshTimer,
        KuudraDisplay,
        NoPre,
        PearlWaypoints,
        RemovePerks,
        SupplyWaypoints,
        TeamHighlight,
        VanqNotifier,
        KuudraReminders,
        KuudraSplits,
        //Splits,
        WardrobeKeybinds,
        InvincibilityTimer,
        KuudraRequeue,
        EnrageDisplay,
        BlockOverlay,
        //ItemsHighlight,
        GoldorTimer,
        VisualWords,
        HidePlayers,
        WarpCooldown,
        CopyChat,
        DVD,
        PlayerDisplay
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

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        tickTasks.removeAll {
            if (it.ticksLeft <= 0) {
                it.function()
                return@removeAll true
            }
            it.ticksLeft--
            false
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketReceivedEvent) {
        packetFunctions
            .filter { it.type.isInstance(event.packet) && it.shouldRun.invoke() }
            .forEach { it.function(event.packet) }
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketSentEvent) {
        packetFunctions
            .filter { it.type.isInstance(event.packet) && it.shouldRun.invoke() }
            .forEach { it.function(event.packet) }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        messageFunctions
            .filter { event.message matches it.filter && it.shouldRun() }
            .forEach { it.function(event.message) }

        cancellableMessageFunctions
            .filter { event.message matches it.filter && it.shouldRun() }
            .forEach { it.function(event) }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        worldLoadFunctions
            .forEach { it.invoke() }
    }

    @SubscribeEvent
    fun activateModuleKeyBinds(event: PreKeyInputEvent) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key == event.keycode) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: PreMouseInputEvent) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key + 100 == event.button) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if ((mc.currentScreen != null && !hudChat) || event.type != RenderGameOverlayEvent.ElementType.ALL || mc.currentScreen == EditHUDGui) return

        mc.mcProfiler.startSection("Odin Hud")

        for (i in 0 until huds.size) {
            huds[i].draw(false)
        }

        mc.mcProfiler.endSection()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        profile("Executors") {
            executors.removeAll {
                if (!it.first.enabled && !it.first.alwaysActive) return@removeAll false // pls test i cba
                it.second.run()
            }
        }
    }

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateReadme(): String {
        val moduleList = modules.sortedByDescending { getTextWidth(it.name, 18f) }
        val categories = moduleList.groupBy { it.category }
        val readme = StringBuilder()

        for ((category, modulesInCategory) in categories) {
            val displayName = category.name.capitalizeFirst()
            readme.appendLine("Category: ${if (displayName == "Floor7") "Floor 7" else displayName}")
            for (module in modulesInCategory) {
                readme.appendLine("- ${module.name}: ${module.description}")
            }
            readme.appendLine()
        }
        return readme.toString()
    }
}