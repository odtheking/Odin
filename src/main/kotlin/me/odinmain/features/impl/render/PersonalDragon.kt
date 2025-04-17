package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.render.RenderUtils.renderX
import me.odinmain.utils.render.RenderUtils.renderY
import me.odinmain.utils.render.RenderUtils.renderZ
import me.odinmain.utils.ui.Colors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

object PersonalDragon : Module(
    name = "Personal Dragon",
    desc = "Spawns your own personal dragon."
) {
    private val onlyF5 by BooleanSetting("Only F5", true, desc = "Only render the dragon when in F5 mode.")
    private val scale by NumberSetting("Scale", 0.5f, 0f, 1f, 0.01f, desc = "The scale of the dragon.")
    private val horizontal by NumberSetting("Horizontal", -1f, -10f, 10f, 0.1f, desc = "The horizontal offset of the dragon.")
    private val vertical by NumberSetting("Vertical", 0f, -10f, 10f, 0.1f, desc = "The vertical offset of the dragon.")
    private val degrees by NumberSetting("Degrees", 0f, -180f, 180f, 1f, desc = "The degrees of the dragon.")
    private val animationSpeed by NumberSetting("Animation Speed", 0.5f, 0.0f, 1f, 0.01f, desc = "The speed of the dragon's animation.")
    private val color by ColorSetting("Color", Colors.WHITE, desc = "The color of the dragon.")

    var dragon: EntityDragon? = null

    override fun onDisable() {
        dragon?.let {
            mc.theWorld?.removeEntityFromWorld(it.entityId)
            dragon = null
        }
        super.onDisable()
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        dragon?.let {
            mc.theWorld?.removeEntityFromWorld(it.entityId)
            dragon = null
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (dragon == null && mc.theWorld != null) {
            dragon = EntityDragon(mc.theWorld)
            dragon?.let { mc.theWorld?.addEntityToWorld(it.entityId, it) }
            return
        }
        mc.thePlayer?.let { player ->
            var yaw = player.rotationYaw
            if (yaw < 0) yaw += 180 else if (yaw > 0) yaw -= 180
            dragon?.apply { setLocationAndAngles(player.renderX, player.renderY + 8, player.renderZ, yaw, player.rotationPitch) }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        dragon?.apply {
            animTime -= (1 - animationSpeed) / 5
            isSilent = true
        }
    }

    @SubscribeEvent
    fun onRenderEntityPre(event: RenderLivingEvent.Pre<EntityDragon>) {
        dragon?.let {
            if (event.entity.entityId != it.entityId) return
            if (onlyF5 && mc.gameSettings.thirdPersonView == 0) {
                event.isCanceled = true
                return
            }
            val yawRadians = Math.toRadians(mc.thePlayer.rotationYaw.toDouble() + degrees)
            GlStateManager.pushMatrix()
            GlStateManager.translate(horizontal * cos(yawRadians), vertical.toDouble(), horizontal * sin(yawRadians))
            GlStateManager.scale(scale / 4, scale / 4, scale / 4)
            GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f)
        }
    }

    @SubscribeEvent
    fun onRenderEntityPost(event: RenderLivingEvent.Post<EntityDragon>) {
        dragon?.let {
            if (event.entity.entityId != it.entityId) return
            GlStateManager.popMatrix()
        }
    }
}
