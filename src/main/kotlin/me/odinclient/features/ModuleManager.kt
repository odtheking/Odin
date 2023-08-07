package me.odinclient.features

import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.PreKeyInputEvent
import me.odinclient.events.impl.PreMouseInputEvent
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.general.*
import me.odinclient.features.impl.m7.*
import me.odinclient.features.impl.qol.*
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.nvg.drawNVG
import me.odinclient.utils.render.gui.nvg.rect
import me.odinclient.utils.render.gui.nvg.text
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object ModuleManager {

    val hud = arrayListOf<HudElement>()

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
        NoBlockAnimation,
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
        TerracottaTimer,
        NoRender,
        NoCarpet,
        RelicAura,
        RelicAnnouncer,

        CloseChest,
        EnchantingExperiments
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
            for (i in 0 until hud.size) {
                hud[i].draw(this, false)
            }
        }
    }

    fun getModuleByName(name: String): Module? = modules.firstOrNull { it.name.equals(name, true) }
}
