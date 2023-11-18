package me.odin

import me.odin.commands.impl.highlightCommand
import me.odin.features.impl.floor7.p3.ArrowAlign
import me.odin.features.impl.floor7.p3.SimonSays
import me.odin.features.impl.render.EtherWarpHelper
import me.odinmain.OdinMain
import me.odinmain.commands.impl.*
import me.odinmain.features.ModuleManager
import net.minecraft.command.ICommand
import net.minecraftforge.client.ClientCommandHandler
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
        commandList.forEach { ClientCommandHandler.instance.registerCommand(it as ICommand?) }
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
        const val MOD_ID = "Odin"
        const val NAME = "Odin"
        const val VERSION = OdinMain.VERSION
        const val PREFIX = "§3Odin §8»§r"

        val commandList = arrayOf(
            mainCommand,
            termSimCommand,
            blacklistCommand,
            WaypointCommand,
            highlightCommand,
            devCommand
        )
    }
}