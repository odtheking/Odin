package me.odinclient.features.impl.render

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.equalsOneOf
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.event.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoDebuff : Module(
    name = "No Debuff",
    category = Category.RENDER,
    description = "Removes various unwanted effects from the game."
) {
    private val antiBlind by BooleanSetting("No Blindness", false, description = "Disables blindness.")
    private val antiPortal by BooleanSetting("No Portal Effect", false, description = "Disables the nether portal overlay.")
    private val antiPumpkin by BooleanSetting("No Pumpkin Overlay", false, description = "Disables the pumpkin overlay.")
    private val noShieldParticles by BooleanSetting("No Shield Particle", false, description = "Removes purple particles and wither impact hearts.")
    private val antiWaterFOV by BooleanSetting("No Water FOV", false, description = "Disable FOV change in water.")
    private val noFire by BooleanSetting("No Fire Overlay", false, description = "Disable Fire overlay on screen.")
    private val seeThroughBlocks by BooleanSetting("See Through Blocks", false, description = "Makes blocks transparent.")
    private val noNausea by BooleanSetting("No Nausea", false, description = "Disables the nausea effect.")

    @JvmStatic
    val shouldIgnoreNausea get() = noNausea && enabled

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
            event.fov *= 70F / 60F
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE && noFire)
            event.isCanceled = true
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.BLOCK && seeThroughBlocks)
            event.isCanceled = true
    }
}