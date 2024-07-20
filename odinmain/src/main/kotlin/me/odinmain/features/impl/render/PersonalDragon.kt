package me.odinmain.features.impl.render

import com.github.stivais.ui.color.*
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.RenderUtils.renderX
import me.odinmain.utils.render.RenderUtils.renderY
import me.odinmain.utils.render.RenderUtils.renderZ
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
    description = "Spawns your own personal dragon."
) {
    private val onlyF5 by BooleanSetting(name = "Only F5", default = true)
    private val scale by NumberSetting(name = "Scale", 0.5f, 0.0f, 1.0f, 0.01f)
    private val horizontal by NumberSetting(name = "Horizontal", 0.0f, -10.0f, 10.0f, 0.1f)
    private val vertical by NumberSetting(name = "Vertical", 0.0f, -10.0f, 10.0f, 0.1f)
    private val degrees by NumberSetting(name = "Degrees", 0.0f, -180.0f, 180.0f, 1.0f)
    private val animationSpeed by NumberSetting(name = "Animation Speed", 0.5f, 0.0f, 1.0f, 0.01f)
    private val color by ColorSetting(name = "Color", Color.WHITE)

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
            dragon?.apply { setLocationAndAngles(player.renderX, player.renderY + 9999, player.renderZ, yaw, player.rotationPitch) }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (dragon == null || event.phase != TickEvent.Phase.END) return
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
