package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.Color
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin


object PersonalDragon : Module(
    name = "Personal Dragon",
    category = Category.RENDER,
    description = "Renders your own personal dragon."
) {
    private val onlyF5: Boolean by BooleanSetting("Only F5", true)
    private val color: Color by ColorSetting("Color", Color(255, 255, 255))
    private val horizontalPos: Double by NumberSetting("Horizontal Position", 0.6, -10.0, 10.0, 0.1)
    private val verticalPos: Double by NumberSetting("Vertical Position", 1.6, -10.0, 10.0, 0.1)
    private val scale: Double by NumberSetting("Dragon Scale", 8.0, 0.0, 50.0, 1.0)

    var entityDragon: EntityDragon? = null
    private var dragonBoundingBox: AxisAlignedBB = AxisAlignedBB(-1.0, -1.0, -1.0, 1.0, 1.0, 1.0)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.theWorld == null) return
        if (entityDragon == null)
        {
            val dragon = EntityDragon(mc.theWorld)
            mc.theWorld.spawnEntityInWorld(dragon)
            entityDragon = dragon
        }
        else
        {
            var yaw = mc.thePlayer.rotationYaw - 180f
            if (yaw < -180f) {
                yaw += 360f
            } else if (yaw > 180f) {
                yaw -= 360f
            }
            val yawRadians = mc.thePlayer.rotationYaw * Math.PI / 180
            entityDragon!!.setPosition(mc.thePlayer.posX + 5.0 * cos(yawRadians), mc.thePlayer.posY - if (mc.thePlayer.isSneaking) 3.5 else 1.5, mc.thePlayer.posZ + 5.0 * sin(yawRadians))
            entityDragon!!.prevRotationYaw = yaw.also { entityDragon!!.rotationYaw = it }
            entityDragon!!.rotationYaw = yaw
            entityDragon!!.slowed = true
            entityDragon!!.isSilent = true
            entityDragon!!.entityBoundingBox = dragonBoundingBox
        }
    }

    override fun onDisable() {
        entityDragon?.setDead()
        entityDragon = null
        super.onDisable()
    }

    @SubscribeEvent
    fun onRenderLiving(event: RenderLivingEvent.Pre<EntityDragon>) {
        if (entityDragon == null || event.entity != entityDragon) return
        if (onlyF5 && mc.gameSettings.thirdPersonView == 0) {
            event.isCanceled = true
            return
        }
        GlStateManager.pushMatrix()
        val yawRadians = mc.thePlayer.rotationYaw * Math.PI / 180
        GlStateManager.translate(horizontalPos * cos(yawRadians), verticalPos, horizontalPos * sin(yawRadians))
        GlStateManager.scale(scale / 100.0, scale / 100.0, scale / 100.0)
        GlStateManager.color(color.r.toFloat() / 255, color.g.toFloat() / 255, color.b.toFloat() / 255, 1f)
    }

    @SubscribeEvent
    fun onRenderLiving(event: RenderLivingEvent.Post<EntityDragon>) {
        if (entityDragon == null) return
        if (event.entity == entityDragon) GlStateManager.popMatrix()
    }
}