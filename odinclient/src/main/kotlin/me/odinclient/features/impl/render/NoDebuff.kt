package me.odinclient.features.impl.render

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.utils.equalsOneOf
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoDebuff : Module(
    name = "No Debuff",
    description = "Removes various unwanted effects from the game."
) {
    private val antiBlind by BooleanSetting("No Blindness", false, desc = "Disables blindness.")
    private val antiPortal by BooleanSetting("No Portal Effect", false, desc = "Disables the nether portal overlay.")
    private val antiPumpkin by BooleanSetting("No Pumpkin Overlay", false, desc = "Disables the pumpkin overlay.")
    private val noShieldParticles by BooleanSetting("No Shield Particle", false, desc = "Removes purple particles and wither impact hearts.")
    private val antiWaterFOV by BooleanSetting("No Water FOV", false, desc = "Disable FOV change in water.")
    private val noFire by BooleanSetting("No Fire Overlay", false, desc = "Disable Fire overlay on screen.")
    private val seeThroughBlocks by BooleanSetting("See Through Blocks", false, desc = "Makes blocks transparent.")
    private val noNausea by BooleanSetting("No Nausea", false, desc = "Disables the nausea effect.")
    val noHurtCam by BooleanSetting("No Hurt Cam", false, desc = "Disables the hurt effect.")

    @JvmStatic
    val shouldIgnoreNausea get() = noNausea && enabled

    @SubscribeEvent
    fun onRenderFog(event: EntityViewRenderEvent.FogDensity) {
        if (!antiBlind) return
        event.density = 0f
        event.isCanceled = true
        GlStateManager.setFogStart(998f)
        GlStateManager.setFogEnd(999f)
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.PORTAL && antiPortal)
            event.isCanceled = true
        if (event.type == RenderGameOverlayEvent.ElementType.HELMET && antiPumpkin)
            event.isCanceled = true
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        val packet = event.packet as? S2APacketParticles ?: return
        if (noShieldParticles && packet.particleType.equalsOneOf(EnumParticleTypes.SPELL_WITCH, EnumParticleTypes.HEART))
            event.isCanceled = true
    }

    @SubscribeEvent
    fun onFOV(event: EntityViewRenderEvent.FOVModifier) {
        if (antiWaterFOV && event.block.material == Material.water)
            event.fov *= 7 / 6
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE && noFire)
            event.isCanceled = true
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.BLOCK && seeThroughBlocks)
            event.isCanceled = true
    }
}