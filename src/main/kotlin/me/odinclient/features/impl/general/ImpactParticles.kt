package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ReceivePacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.equalsOneOf
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ImpactParticles : Module(
    "No Shield Particle",
    category = Category.GENERAL,
    description = "Removes purple particles and wither impact hearts."
) {
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S2APacketParticles) return
        if (event.packet.particleType.equalsOneOf(EnumParticleTypes.SPELL_WITCH, EnumParticleTypes.HEART)) {
            val particlePos = event.packet.run { Vec3(xCoordinate, yCoordinate, zCoordinate) }
            if (particlePos.squareDistanceTo(mc.thePlayer.positionVector) <= 169) {
                event.isCanceled = true
            }
        }
    }
}