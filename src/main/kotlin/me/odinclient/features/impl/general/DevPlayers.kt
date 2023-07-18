package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.AxisAlignedBB
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
        "OdinClient" to PlayerSize(1f, 1f, 1f),
        "Odtheking" to PlayerSize(1f, 1f, 1f)

    )

    lateinit var entityDragon: EntityDragon
    private var dragonBoundingBox: AxisAlignedBB = AxisAlignedBB(-1.0, -1.0, -1.0, 1.0, 1.0, 1.0)

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
        var yaw = mc.thePlayer.rotationYaw - 180f
        if (yaw < -180f) {
            yaw += 360f
        } else if (yaw > 180f) {
            yaw -= 360f
        }
        val yawRadians = mc.thePlayer.rotationYaw * Math.PI / 180
        entityDragon.setPosition(mc.thePlayer.posX + 5.0 * cos(yawRadians), mc.thePlayer.posY - if (mc.thePlayer.isSneaking) 3.5 else 1.5, mc.thePlayer.posZ + 5.0 * sin(yawRadians))
        entityDragon.prevRotationYaw = yaw.also { entityDragon.rotationYaw = it }
        entityDragon.rotationYaw = yaw
        entityDragon.slowed = true
        entityDragon.isSilent = true
        entityDragon.entityBoundingBox = dragonBoundingBox
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
        GlStateManager.translate(config.personalDragonPosHorizontal * cos(yawRadians), config.personalDragonPosVertical.toDouble(), config.personalDragonPosHorizontal.toDouble() * sin(yawRadians))
        GlStateManager.scale(config.personalDragonScale / 100.0, config.personalDragonScale / 100.0, config.personalDragonScale / 100.0)
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