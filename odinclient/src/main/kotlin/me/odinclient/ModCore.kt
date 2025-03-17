package me.odinclient

import me.odinclient.commands.impl.OdinClientCommand
import me.odinclient.commands.impl.autoSellCommand
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.floor7.FreezeGame
import me.odinclient.features.impl.floor7.FuckDiorite
import me.odinclient.features.impl.floor7.RelicAura
import me.odinclient.features.impl.floor7.p3.*
import me.odinclient.features.impl.render.*
import me.odinclient.features.impl.skyblock.*
import me.odinmain.OdinMain
import me.odinmain.commands.CommandRegistry
import me.odinmain.features.ModuleManager
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@Mod(
    modid = ModCore.MOD_ID,
    name = ModCore.NAME,
    version = ModCore.VERSION,
    clientSideOnly = true
)
class ModCore {

    @EventHandler
    fun init(event: FMLInitializationEvent) {
        CommandRegistry.add(
            autoSellCommand,
            OdinClientCommand
        )

        OdinMain.init()
        MinecraftForge.EVENT_BUS.register(this)
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
            EscrowFix, TerminalAura, AutoTerms, Camera, DungeonAbilities, QueueTerms, HidePlayers
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
