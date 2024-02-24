package me.odinclient

import me.odinclient.commands.impl.AutoSellCommand
import me.odinclient.commands.impl.ESPCommand
import me.odinclient.dungeonmap.features.Dungeon
import me.odinclient.dungeonmap.features.MapRender
import me.odinclient.dungeonmap.features.Window
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.floor7.DioriteFucker
import me.odinclient.features.impl.floor7.FreezeGame
import me.odinclient.features.impl.floor7.RelicAura
import me.odinclient.features.impl.floor7.p3.*
import me.odinclient.features.impl.render.*
import me.odinclient.features.impl.skyblock.*
import me.odinmain.OdinMain
import me.odinmain.commands.Commodore.Companion.registerCommands
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
        Window.init()

        OdinMain.init()
        MinecraftForge.EVENT_BUS.register(this)

        listOf(
            Dungeon,
            MapRender
        ).forEach(MinecraftForge.EVENT_BUS::register)

        registerCommands(
            ESPCommand,
            AutoSellCommand,
        )
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        OdinMain.postInit()
    }

    @EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        val modules: ArrayList<Module> = arrayListOf(
            AutoGFS, AutoIceFill, AutoSell, CancelInteract, CancelChestOpen, GhostPick, MapModule, SecretHitboxes, SwapStonk,
            Arrows, ArrowAlign, CancelWrongTerms, HoverTerms, LeversTriggerbot, SimonSays,
            DioriteFucker, RelicAura, Trajectories, Ghosts, NoCarpet, NoDebuff, LockCursor,
            CookieClicker, AutoExperiments, FarmingHitboxes, NoBlock, TermAC, Triggerbot, GhostBlock, FreezeGame, AbilityKeybind, EtherWarpHelper, ChestEsp, NoBreakReset,
            /*AutoTerms,*/
        )
        ModuleManager.modules.addAll(modules)
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
