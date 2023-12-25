package me.odinclient

import me.odinclient.commands.impl.AutoSellCommand
import me.odinclient.commands.impl.ESPCommand
import me.odinclient.dungeonmap.features.Window
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.floor7.AutoEdrag
import me.odinclient.features.impl.floor7.DioriteFucker
import me.odinclient.features.impl.floor7.FreezeGame
import me.odinclient.features.impl.floor7.RelicAura
import me.odinclient.features.impl.floor7.p3.*
import me.odinclient.features.impl.render.Ghosts
import me.odinclient.features.impl.render.NoCarpet
import me.odinclient.features.impl.render.NoDebuff
import me.odinclient.features.impl.render.Trajectories
import me.odinclient.features.impl.skyblock.*
import me.odinmain.OdinMain
import me.odinmain.commands.register
import me.odinmain.features.Module
import me.odinmain.features.ModuleManager
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
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
        ModuleManager.modules.addAll(modules)

        Window.init()
        OdinMain.onLegitVersion = false
        OdinMain.init()
        MinecraftForge.EVENT_BUS.register(this)

        register(
            ESPCommand,
            AutoSellCommand,
        )
    }

    private val modules: ArrayList<Module> = arrayListOf(
        AutoGFS,
        AutoIceFill,
        AutoLeap,
        AutoMask,
        AutoSell,
        AutoShield,
        AutoUlt,
        AutoWish,
        CancelInteract,
        CancelChestOpen,
        GhostPick,
        MapModule,
        SecretHitboxes,
        SuperBoom,
        SwapStonk,

        Arrows,
        ArrowAlign,
        CancelWrongTerms,
        HoverTerms,
        LeversTriggerbot,
        SimonSays,

        AutoEdrag,
        DioriteFucker,
        RelicAura,
        //RelicPlacer,

        Trajectories,
        Ghosts,
        NoCarpet,
        NoDebuff,

        CookieClicker,
        AutoExperiments,
        FarmingHitboxes,
        LimboLeave,
        NoBlock,
        TermAC,
        Triggerbot,
        GhostBlock,
        FreezeGame,
        AbilityKeybind,
        TerminalTriggerbot,
        EtherWarpHelper,
        ChestEsp,
        NoBreakReset,
        //AutoTerms
    )

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        OdinMain.postInit()
    }

    @EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        OdinMain.loadComplete()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (Window.isVisible != Window.shouldShow) Window.isVisible = Window.shouldShow
        OdinMain.onTick()
    }

    companion object {
        const val MOD_ID = "odclient"
        const val NAME = "OdinClient"
        const val VERSION = OdinMain.VERSION
    }
}
