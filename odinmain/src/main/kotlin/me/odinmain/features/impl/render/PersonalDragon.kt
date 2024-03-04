package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.render.Color
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
    category = Category.RENDER,
    description = "Renders your own personal dragon."
) {
    private val onlyF5: Boolean by BooleanSetting(name = "Only F5", default = true)
    private val scale: Float by NumberSetting(name = "Scale", 0.5f, 0.0f, 1.0f, 0.01f)
    private val horizontal: Float by NumberSetting(name = "Horizontal", 0.0f, -10.0f, 10.0f, 0.1f)
    private val vertical: Float by NumberSetting(name = "Vertical", 0.0f, -10.0f, 10.0f, 0.1f)
    private val degrees: Float by NumberSetting(name = "Degrees", 0.0f, -180.0f, 180.0f, 1.0f)
    private val animationSpeed: Float by NumberSetting(name = "Animation Speed", 0.5f, 0.0f, 1.0f, 0.01f)
    private val color: Color by ColorSetting(name = "Color", default = Color.WHITE)

    var dragon: EntityDragon? = null

    override fun onDisable() {
        if (this.dragon == null) return
        mc.theWorld?.removeEntityFromWorld(this.dragon!!.entityId)
        this.dragon = null
        super.onDisable()
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        if (this.dragon != null) {
            mc.theWorld.removeEntityFromWorld(dragon!!.entityId)
            this.dragon = null
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (this.dragon == null && mc.theWorld != null) {
            dragon = EntityDragon(mc.theWorld)
            mc.theWorld.addEntityToWorld(dragon!!.entityId, dragon)
            return
        }
        if (mc.thePlayer == null) return
        var yaw = mc.thePlayer.rotationYaw
        if (yaw < 0) yaw += 180 else if (yaw > 0) yaw -= 180
        dragon?.apply {
            setLocationAndAngles(mc.thePlayer.renderX, mc.thePlayer.renderY + 6, mc.thePlayer.renderZ, yaw, mc.thePlayer.rotationPitch)
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
    fun onRenderEntityPre(event: RenderLivingEvent.Pre<EntityDragon>){
        if (this.dragon == null || event.entity.entityId != this.dragon!!.entityId) return
        if (onlyF5 && mc.gameSettings.thirdPersonView == 0) { event.isCanceled = true; return }
        val yawRadians = Math.toRadians(mc.thePlayer.rotationYaw.toDouble() + this.degrees)
        GlStateManager.pushMatrix()
        GlStateManager.translate(this.horizontal * cos(yawRadians), this.vertical.toDouble(), this.horizontal * sin(yawRadians))
        GlStateManager.scale(this.scale / 4, this.scale / 4, this.scale / 4)
        GlStateManager.color(color.r / 255f, color.g / 255f, color.b / 255f)
    }

    @SubscribeEvent
    fun onRenderEntityPost(event: RenderLivingEvent.Post<EntityDragon>) {
        if (this.dragon == null || event.entity.entityId != this.dragon!!.entityId) return
        GlStateManager.popMatrix()
    }

}