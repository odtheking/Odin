package me.odinclient

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinclient.commands.impl.autoSellCommand
import me.odinclient.features.impl.dungeon.*
import me.odinclient.features.impl.dungeon.AutoSell.sellList
import me.odinclient.features.impl.floor7.DioriteFucker
import me.odinclient.features.impl.floor7.FreezeGame
import me.odinclient.features.impl.floor7.RelicAura
import me.odinclient.features.impl.floor7.p3.*
import me.odinclient.features.impl.render.*
import me.odinclient.features.impl.skyblock.*
import me.odinclient.mixin.accessors.IEntityRendererAccessor
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.commands.registerCommands
import me.odinmain.config.utils.ConfigFile
import me.odinmain.features.ModuleManager
import me.odinmain.ui.util.shader.FramebufferShader
import me.odinmain.utils.render.RenderUtils
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
        OdinMain.init()
        MinecraftForge.EVENT_BUS.register(this)

        registerCommands(
            autoSellCommand
        )
        FramebufferShader.setupCameraTransform =
            { (mc.entityRenderer as? IEntityRendererAccessor)?.invokeSetupCameraTransform(RenderUtils.partialTicks, 0) }
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        // here temporarily for mgiration
        val autoSellConfigFile = ConfigFile("autoSell-config")
        if (autoSellConfigFile.exists()) {
            with(autoSellConfigFile.bufferedReader().use { it.readText() }) {
                if (this != "") {
                    val temp = GsonBuilder().setPrettyPrinting().create().fromJson<MutableList<String>>(this, object : TypeToken<MutableList<String>>() {}.type)
                    sellList.addAll(temp)
                }
            }
        }

        OdinMain.postInit()
    }

    @EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        ModuleManager.addModules(
            AutoGFS, /*AutoIceFill,*/ AutoSell, CancelInteract, CancelChestOpen, GhostPick, SecretHitboxes,
            SwapStonk, Arrows, ArrowAlign, CancelWrongTerms, HoverTerms, LightsDevice, SimonSays,
            DioriteFucker, RelicAura, Trajectories, Ghosts, NoCarpet, NoDebuff, LockCursor,
            ChocolateFactory, AutoExperiments, FarmingHitboxes, NoBlock, TermAC, Triggerbot, GhostBlocks, FreezeGame,
            AbilityKeybind, EtherWarpHelper, ChestEsp, NoBreakReset, EscrowFix, TerminalMove, NoPush, SeeThroughBlocks,
            TerminalAura, AutoTerms, QueueTerms, Camera, AutoUlt
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
