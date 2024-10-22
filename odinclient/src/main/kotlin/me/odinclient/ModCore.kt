package me.odinclient

import me.odinclient.commands.impl.OdinClientCommand
import me.odinclient.commands.impl.autoSellCommand
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.floor7.*
import me.odinclient.features.impl.floor7.p3.*
import me.odinclient.features.impl.render.*
import me.odinclient.features.impl.skyblock.*
import me.odinclient.mixin.accessors.IEntityRendererAccessor
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.commands.registerCommands
import me.odinmain.features.ModuleManager
import me.odinmain.ui.util.shader.FramebufferShader
import me.odinmain.utils.render.RenderUtils
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@Suppress("UNUSED_PARAMETER")
@Mod(
    modid = ModCore.MOD_ID,
    name = ModCore.NAME,
    version = ModCore.VERSION,
    clientSideOnly = true
)
class ModCore {

    @EventHandler
    fun init(event: FMLInitializationEvent) {
        OdinMain.init()
        listOf(
            this
        ).forEach { MinecraftForge.EVENT_BUS.register(it) }

        registerCommands(
            autoSellCommand,
            OdinClientCommand
        )

        FramebufferShader.setupCameraTransform =
            { (mc.entityRenderer as? IEntityRendererAccessor)?.invokeSetupCameraTransform(RenderUtils.partialTicks, 0) }
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        OdinMain.postInit()
    }

    @EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        ModuleManager.addModules(
            AutoGFS, /*AutoIceFill,*/ AutoSell, CancelInteract, CloseChest, SecretHitboxes,
            HoverTerms, LightsDevice, SimonSays, ArrowsDevice, FuckDiorite, RelicAura,
            Trajectories, Ghosts, NoDebuff, ChocolateFactory, AutoExperiments, AutoHarp,
            FarmingHitboxes, NoBlock, AutoClicker, Triggerbot, GhostBlocks, FreezeGame, EtherWarpHelper, ChestEsp,
            EscrowFix, TerminalAura, AutoTerms, Camera, DungeonAbilities/*, QueueTerms*/, HidePlayers,

        )
        OdinMain.loadComplete()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        OdinMain.onTick()
    }

    companion object {
        const val MOD_ID = "odclient"
        const val NAME = "OdinClient"
        const val VERSION = OdinMain.VERSION
    }
}
