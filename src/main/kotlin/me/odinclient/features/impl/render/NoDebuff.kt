package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ReceivePacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.Utils.equalsOneOf
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoDebuff : Module(
    "No Debuff",
    category = Category.RENDER,
    tag = TagType.NEW,
    description = "Disables certain debuffs."
) {

    private val antiBlind: Boolean by BooleanSetting("No Blindness", false, description = "Disables blindness")
    private val antiPortal: Boolean by BooleanSetting("No Portal Effect", false, description = "Disables the nether portal overlay.")
    private val noShieldParticles: Boolean by BooleanSetting("No Shield Particle", false, description = "Removes purple particles and wither impact hearts.")
    private val antiWaterFOV: Boolean by BooleanSetting("No Water FOV", false, description = "Disable FOV change in water.")

    @SubscribeEvent
    fun onRenderFog(event: EntityViewRenderEvent.FogDensity) {
        if (!antiBlind) return
        event.density = 0f
        GlStateManager.setFogStart(998f)
        GlStateManager.setFogEnd(999f)
        event.isCanceled = true
    }
    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (!antiPortal) return
        if (event.type == RenderGameOverlayEvent.ElementType.PORTAL) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (!noShieldParticles || event.packet !is S2APacketParticles) return
        if (event.packet.particleType.equalsOneOf(EnumParticleTypes.SPELL_WITCH, EnumParticleTypes.HEART)) {
            val particlePos = event.packet.run { Vec3(xCoordinate, yCoordinate, zCoordinate) }
            if (particlePos.squareDistanceTo(mc.thePlayer.positionVector) <= 169) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onFOV(event: EntityViewRenderEvent.FOVModifier) {
        if (!antiWaterFOV || event.block.material != Material.water) return
        event.fov = event.fov * 70F / 60F
    }
}
