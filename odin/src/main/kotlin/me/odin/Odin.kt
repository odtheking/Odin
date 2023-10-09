package me.odin


import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent


@Suppress("UNUSED_PARAMETER")
@Mod(
    modid = Odin.MOD_ID,
    name = Odin.NAME,
    version = Odin.VERSION,
    clientSideOnly = true
)
class Odin {

    @EventHandler
    fun init(event: FMLInitializationEvent) {

        listOf(
            Utils,
            this
        ).forEach {
            MinecraftForge.EVENT_BUS.register(it)
        }


    }





    companion object {
        const val MOD_ID = "assets.odinmain"
        const val NAME = "Odin"
        const val VERSION = "1.1.1"

        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()

        // TODO: Remove

    }
}
