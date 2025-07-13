package me.odinmain.features

import me.odinmain.OdinMain.mc
import me.odinmain.clickgui.HudManager
import me.odinmain.clickgui.settings.impl.HUDSetting
import me.odinmain.clickgui.settings.impl.KeybindSetting
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.InputEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.impl.dungeon.*
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import me.odinmain.features.impl.floor7.TerminalSimulator
import me.odinmain.features.impl.floor7.TickTimers
import me.odinmain.features.impl.floor7.WitherDragons
import me.odinmain.features.impl.floor7.p3.*
import me.odinmain.features.impl.nether.*
import me.odinmain.features.impl.render.*
import me.odinmain.features.impl.render.ClickGUIModule.hudChat
import me.odinmain.features.impl.skyblock.*
import me.odinmain.utils.logError
import me.odinmain.utils.profile
import me.odinmain.utils.ui.rendering.NVGRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Class that contains all Modules and huds
 * @author Aton, Bonsai
 */
object ModuleManager {
    data class PacketFunction<T : Packet<*>>(val type: Class<T>, val shouldRun: () -> Boolean, val function: (T) -> Unit)
    data class MessageFunction(val filter: Regex, val shouldRun: () -> Boolean, val function: (MatchResult) -> Unit)
    data class TickTask(var ticksLeft: Int, val server: Boolean, val function: () -> Unit)

    val packetFunctions = arrayListOf<PacketFunction<Packet<*>>>()
    val messageFunctions = arrayListOf<MessageFunction>()
    val worldLoadFunctions = arrayListOf<() -> Unit>()
    val tickTasks = CopyOnWriteArrayList<TickTask>()
    private val keybindSettingsCache = mutableListOf<KeybindSetting>()
    val hudSettingsCache = mutableListOf<HUDSetting>()

    val modules: ArrayList<Module> = arrayListOf(
        // dungeon
        DungeonRequeue, BlessingDisplay, PositionalMessages, ExtraStats, KeyHighlight, Mimic, TeammatesHighlight,
        TerracottaTimer, BloodCamp, SecretClicked, DungeonWaypoints, LeapMenu, PuzzleSolvers, /*MageBeam,*/
        WarpCooldown, MapInfo, SwapSound, LividSolver, SpiritBear,

        // floor 7
        TerminalSolver, TerminalTimes, MelodyMessage, TickTimers, InactiveWaypoints, WitherDragons,
        TerminalSimulator, TerminalSounds, ArrowAlign,

        // render
        BPSDisplay, ClickGUIModule, CustomHighlight, CPSDisplay, DragonHitboxes, GyroWand, NameChanger,
        PersonalDragon, RenderOptimizer, PerformanceHUD, Waypoints, CanClip, Animations, SpaceHelmet,
        BlockOverlay, VisualWords, DVD, HideArmor,

        //skyblock
        NoCursorReset, AutoSprint, BlazeAttunement, ChatCommands, DeployableTimer, DianaHelper, ArrowHit,
        Ragnarock, MobSpawn, Splits, WardrobeKeybinds, InvincibilityTimer, ItemsHighlight, PlayerDisplay,
        FarmKeys, PetKeybinds, CommandKeybinds, SpringBoots, AbilityTimers, SlotBinds,

        // kuudra
        BuildHelper, FreshTools, KuudraDisplay, NoPre, PearlWaypoints, RemovePerks, SupplyHelper, TeamHighlight,
        VanqNotifier, KuudraReminders, KuudraRequeue,
    )

    init {
        for (module in modules) {
            module.key?.let {
                module.register(KeybindSetting("Keybind", it, "Toggles the module").apply { value.onPress = { module.onKeybind() } })
            }
            for (setting in module.settings) {
                if (setting is KeybindSetting) keybindSettingsCache.add(setting)
                if (setting is HUDSetting) hudSettingsCache.add(setting)
            }
        }
    }

    fun addModules(vararg module: Module) {
        for (i in module) {
            modules.add(i)
            i.key?.let { i.register(KeybindSetting("Keybind", it, "Toggles the module")) }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickTaskTick(false)
    }

    private fun tickTaskTick(server: Boolean = false) {
        tickTasks.removeAll { tickTask ->
            if (tickTask.server != server) return@removeAll false
            if (tickTask.ticksLeft <= 0) {
                runCatching { tickTask.function() }.onFailure { logError(it, this) }
                return@removeAll true
            }
            tickTask.ticksLeft--
            false
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onReceivePacket(event: PacketEvent.Receive) {
        if (event.packet is S32PacketConfirmTransaction) tickTaskTick(true)
        packetFunctions.forEach {
            if (it.shouldRun() && it.type.isInstance(event.packet)) it.function(event.packet)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onSendPacket(event: PacketEvent.Send) {
        packetFunctions.forEach {
            if (it.shouldRun() && it.type.isInstance(event.packet)) it.function(event.packet)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatPacket(event: ChatPacketEvent) {
        messageFunctions.forEach {
            if (it.shouldRun()) it.function(it.filter.find(event.message) ?: return@forEach)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        worldLoadFunctions.forEach { it.invoke() }
    }

    @SubscribeEvent
    fun activateModuleKeyBinds(event: InputEvent.Keyboard) {
        for (keybindSetting in keybindSettingsCache) {
            if (keybindSetting.value.key == event.keycode) keybindSetting.value.onPress?.invoke()
        }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: InputEvent.Mouse) {
        for (keybindSetting in keybindSettingsCache) {
            if (keybindSetting.value.key + 100 == event.keycode) keybindSetting.value.onPress?.invoke()
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if ((mc.currentScreen != null && !hudChat) || event.type != RenderGameOverlayEvent.ElementType.ALL || mc.currentScreen == HudManager) return

        profile("Odin Hud") {
            GlStateManager.pushMatrix()
            val sr = ScaledResolution(mc)
            GlStateManager.scale(mc.displayWidth / 1920f, mc.displayHeight / 1080f, 1f)
            GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
            for (hudSettings in hudSettingsCache) {
                if (hudSettings.isEnabled) hudSettings.value.draw(false)
            }
            GlStateManager.popMatrix()
        }
    }

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateFeatureList(): String {
        val featureList = StringBuilder()

        for ((category, modulesInCategory) in modules.groupBy { it.category }.entries) {
            featureList.appendLine("Category: ${category.displayName}")
            for (module in modulesInCategory.sortedByDescending { NVGRenderer.textWidth(it.name, 18f, NVGRenderer.defaultFont) }) {
                featureList.appendLine("- ${module.name}: ${module.description}")
            }
            featureList.appendLine()
        }
        return featureList.toString()
    }
}