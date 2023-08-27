package me.odinclient.features

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.PreKeyInputEvent
import me.odinclient.events.impl.PreMouseInputEvent
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.floor7.*
import me.odinclient.features.impl.floor7.p3.ArrowAlign
import me.odinclient.features.impl.floor7.p3.Levers
import me.odinclient.features.impl.floor7.p3.SimonSays
import me.odinclient.features.impl.floor7.p3.TerminalSolver
import me.odinclient.features.impl.render.*
import me.odinclient.features.impl.skyblock.*
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.gui.nvg.drawNVG
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Class that contains all Modules and huds
 * @author Aton
 */
object ModuleManager {

    val huds = arrayListOf<HudElement>()

    val modules: ArrayList<Module> = arrayListOf(
        AutoIceFill,
        AutoLeap,
        AutoMask,
        AutoReady,
        AutoSell,
        AutoShield,
        AutoUlt,
        AutoWish,
        BlessingDisplay,
        GhostBlock,
        KeyESP,
        SecretHitboxes,
        SuperBoom,
        TeammatesOutline,
        Triggerbot,
        WatcherBar,
        MapModule,
        PersonalDragon,
        CustomEnd,
        EscrowFix,

        ArrowTrajectory,
        Camera,
        ClickGUIModule,
        ESP,
        CPSDisplay,
        LockCursor,
        VanqNotifier,
        PartyCommands,
        GuildCommands,
        PrivateCommands,
        AutoEdrag,
        DioriteFucker,
        DragonBoxes,
        DragonTimer,
        LeapHelper,
        NecronDropTimer,
        DecoyDeadMessage,
        AutoSprint,
        BrokenHype,
        CookieClicker,
        GhostPick,
        GyroRange,
        KuudraAlerts,
        NoBlock,
        NoCursorReset,
        Reminders,
        TermAC,
        Ghosts,
        BPS,
        PortalFix,
        TerminalTimes,
        Waypoints,
        Server,
        DeployableTimer,
        CanClip,
        NoRender,
        NoCarpet,
        RelicAura,
        RelicAnnouncer,
        CloseChest,
        EnchantingExperiments,
        ThornStun,
        NoDebuff,
        FarmingHitboxes,
        TerminalSolver,
        SimonSays,
        Levers,
        NickHider,
        DragonHitboxes,
        ArrowAlign,
        CrimsonSeaCreature
    )

    @SubscribeEvent
    fun activateModuleKeyBinds(event: PreKeyInputEvent) {
        modules.filter { it.keyCode == event.keycode }.forEach { it.onKeybind() }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: PreMouseInputEvent) {
        modules.filter { it.keyCode + 100 == event.button }.forEach { it.onKeybind() }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (mc.currentScreen != null || event.type != RenderGameOverlayEvent.ElementType.TEXT) return
        drawNVG {
            for (i in 0 until huds.size) {
                huds[i].draw(this, false)
            }
        }
    }

    fun getModuleByName(name: String): Module? = modules.firstOrNull { it.name.equals(name, true) }
}
