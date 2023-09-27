package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.SoopyGuessBurrow
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DianaHelper : Module(
    name = "Diana Helper",
    description = "Helps with Diana's event.",
    category = Category.SKYBLOCK,
    tag = TagType.NEW
) {

    var renderPos: Vec3? = null

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            SoopyGuessBurrow.handleSoundPacket(it)
        }

        onPacket(S2APacketParticles::class.java) {
            SoopyGuessBurrow.handleParticlePacket(it)
        }


    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {

        renderPos?.let {
            ChatUtils.modMessage(it )
            RenderUtils.renderCustomBeacon("Burrow", it, Color.WHITE)
        }
    }











}