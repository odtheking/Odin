package me.odinclient.features.general

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.cos
import kotlin.math.sin

object DevPlayers {

    data class PlayerSize(val xScale: Float, val yScale: Float, val zScale: Float)

    private val devs = hashMapOf(
        "_Inton_" to PlayerSize(2f, .5f, 2f),
        "OdinClient" to PlayerSize(1f, 1f, 1f)
    )

    lateinit var entityDragon: EntityDragon

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        if (!config.personalDragon) return
        val dragon = EntityDragon(event.world)
        event.world.spawnEntityInWorld(dragon)
        entityDragon = dragon
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (mc.theWorld == null || mc.thePlayer == null || !::entityDragon.isInitialized) return
        var yaw = mc.thePlayer.rotationYaw - 180
        if (yaw < -180) {
            yaw += 360f
        } else if (yaw > 180) {
            yaw -= 360f
        }
        val yawRadians = mc.thePlayer.rotationYaw * Math.PI / 180
        entityDragon.setLocationAndAngles(
            mc.thePlayer.posX + 5 * cos(yawRadians), mc.thePlayer.posY - if (mc.thePlayer.isSneaking) 3.5 else 1.5, mc.thePlayer.posZ + 5 * sin(yawRadians),
            yaw, mc.thePlayer.rotationPitch
        )
        entityDragon.slowed = true
        entityDragon.isSilent = true
        if (mc.pointedEntity?.entityId == this.entityDragon.entityId) {
            mc.pointedEntity = null
        }
    }

    @SubscribeEvent
    fun onRenderLiving(event: RenderLivingEvent.Pre<EntityDragon>) {
        if (!::entityDragon.isInitialized || event.entity != entityDragon) return
        if (config.personalDragonOnlyF5 && mc.gameSettings.thirdPersonView == 0) {
            event.isCanceled = true
            return
        }
        GlStateManager.pushMatrix()
        val yawRadians = mc.thePlayer.rotationYaw * Math.PI / 180
        GlStateManager.translate(config.personalDragonPosX * cos(yawRadians), config.personalDragonPosY.toDouble(), config.personalDragonPosZ * sin(yawRadians))
        GlStateManager.scale(0.08, 0.08, 0.08)
        GlStateManager.color(config.personalDragonColor.red.toFloat() / 255, config.personalDragonColor.green.toFloat() / 255, config.personalDragonColor.blue.toFloat() / 255, 1f)
    }

    @SubscribeEvent
    fun onRenderLiving(event: RenderLivingEvent.Post<EntityDragon>) {
        if (!::entityDragon.isInitialized) return
        if (event.entity == entityDragon) GlStateManager.popMatrix()
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Pre) {
        if (!devs.containsKey(event.entity.name)) return
        val size = devs[event.entity.name]!!
        GlStateManager.pushMatrix()
        GlStateManager.scale(size.xScale, size.yScale, size.zScale)
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Post) {
        if (!devs.containsKey(event.entity.name)) return
        GlStateManager.popMatrix()
    }
}