package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Colors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

object PetDragon : Module(
    name = "Pet Dragon",
    description = "Renders your own pet dragon."
) {
    private val size by NumberSetting("Size", 1f, 0.1f, 2f, 0.01f, desc = "Sets the size of the dragon.")
    private val height by NumberSetting("Height", 1f, -10f, 10f, 0.01f, desc = "Sets the height at which the dragon hovers.")
    private val distance by NumberSetting("Distance", 2f, 0f, 10f, 0.01f, desc = "Sets the distance between you and the dragon.")
    private val direction by NumberSetting("Direction", 0f, 0f, 360f, increment = 1f, desc = "Sets the direction the dragon hovers at.")
    private val animationSpeed by NumberSetting("Animation Speed", 1f, 0.1f, 2f, 0.01f, desc = "Sets the speed of the dragon's animation.")
   // private val instantRotation by BooleanSetting("Instant Rotation", desc = "Instantly rotates the dragon to face the same direction as you.")
    private val onlyF5 by BooleanSetting("Only F5", desc = "Only shows the dragon when in third person.")
    private val color by ColorSetting("Color", default = Colors.WHITE, desc = "Sets the color of the dragon.")

    private var dragonEntity: CustomDragonEntity? = null

    private class CustomDragonEntity(world: World) : EntityDragon(world) {

        init {
            customNameTag = "Pet Dragon"
        }

        override fun onUpdate() {
            prevAnimTime = animTime
            animTime += 0.1f * animationSpeed

            if (ringBufferIndex < 0) {
                ringBuffer.forEach {
                    it[0] = rotationYaw.toDouble()
                    it[1] = posY
                }
            }

            ringBufferIndex = (ringBufferIndex + 1) % ringBuffer.size
            ringBuffer[ringBufferIndex][0] = rotationYaw.toDouble()
            ringBuffer[ringBufferIndex][1] = posY
        }

        override fun onLivingUpdate() {}

        override fun moveEntity(x: Double, y: Double, z: Double) {}
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if (dragonEntity == null) dragonEntity = CustomDragonEntity(world)

        val playerYawRadians = Math.toRadians(player.rotationYaw.toDouble() + direction)

        val targetX = player.posX + distance * cos(playerYawRadians)
        val targetY = player.posY + height
        val targetZ = player.posZ + distance * sin(playerYawRadians)

        dragonEntity?.setPositionAndRotation(targetX, targetY, targetZ, player.rotationYaw + 180f, 0f)
        dragonEntity?.onUpdate()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (onlyF5 && mc.gameSettings.thirdPersonView == 0) return

        GlStateManager.pushMatrix()
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
        GlStateManager.scale(size / 10, size / 10, size / 10)

        dragonEntity?.let { mc.renderManager?.renderEntitySimple(it, event.partialTicks) }

        mc.entityRenderer.disableLightmap()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.popMatrix()
    }

    @SubscribeEvent
    fun onRenderBossHealth(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH || BossStatus.bossName.noControlCodes != "Pet Dragon") return
        event.isCanceled = true
    }

    override fun onDisable() {
        super.onDisable()
        dragonEntity = null
    }
}