package me.odin

import me.odin.commands.impl.HighlightCommand
import me.odin.features.impl.floor7.p3.ArrowAlign
import me.odin.features.impl.floor7.p3.SimonSays
import me.odin.features.impl.render.EtherWarpHelper
import me.odinmain.OdinMain
import me.odinmain.commands.Commodore.Companion.registerCommands
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
        OdinMain.init()
        MinecraftForge.EVENT_BUS.register(this)

        registerCommands(
            HighlightCommand
        )
    }

    private val modules = arrayListOf(
        ArrowAlign,
        SimonSays,
        EtherWarpHelper,
    )

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        OdinMain.onTick()
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        OdinMain.postInit()
    }

    @EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        OdinMain.loadComplete()
    }


    companion object {
        const val MOD_ID = "od"
        const val NAME = "Odin"
        const val VERSION = OdinMain.VERSION
    }
}
