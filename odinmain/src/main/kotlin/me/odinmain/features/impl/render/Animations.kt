package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper
import kotlin.math.exp

/*
/*
 * From Floppa Client
 * https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/java/floppaclient/mixins/render/EntityRendererMixin.java
 */
 */

object Animations : Module(
    name = "Animations",
    category = Category.RENDER,
    description = "Changes the appearance of the first-person view model"
) {

    val size: Float by NumberSetting("Size", 0.0f, -1.5, 1.5, 0.05, description = "Scales the size of your currently held item. Default: 0")
    val x: Float by NumberSetting("X", 0.0f, -2.5, 1.5, 0.05, description = "Moves the held item. Default: 0")
    val y: Float by NumberSetting("Y", 0.0f, -1.5, 1.5, 0.05, description = "Moves the held item. Default: 0")
    val z: Float by NumberSetting("Z", 0.0f, -1.5, 3.0, 0.05, description = "Moves the held item. Default: 0")
    val yaw: Float by NumberSetting("Yaw", 0.0f, -180.0, 180.0, 5.0, description = "Rotates your held item. Default: 0")
    val pitch: Float by NumberSetting("Pitch", 0.0f, -180.0, 180.0, 5.0, description = "Rotates your held item. Default: 0")
    val roll: Float by NumberSetting("Roll", 0.0f, -180.0, 180.0, 5.0, description = "Rotates your held item. Default: 0")
    val speed: Float by NumberSetting("Speed", 0.0f, -2.0, 1.0, 0.05, description = "Speed of the swing animation.")
    val ignoreHaste: Boolean by BooleanSetting("Ignore Haste", false, description = "Makes the chosen speed override haste modifiers.")
    val noEquipReset: Boolean by BooleanSetting("No Equip Reset", default = false, description = "Makes items instantly appear in your hand after switching")
    val noSwing: Boolean by BooleanSetting("No Swing", false, description = "Prevents your item from visually swinging forward")
    val blockHit: Boolean by BooleanSetting("Block Hit", false, description = "Visual 1.7 block hit animation")

    val reset: () -> Unit by ActionSetting("Reset") {
        this.settings.forEach { it.reset() }
    }

    fun itemTransferHook(equipProgress: Float, swingProgress: Float): Boolean {
        if (!this.enabled) return false
        val newSize = (0.4 * exp(size)).toFloat()
        val newX = (0.56f * (1 + x))
        val newY = (-0.52f * (1 - y))
        val newZ = (-0.71999997f * (1 + z))
        GlStateManager.translate(newX, newY, newZ)
        GlStateManager.translate(0.0f, equipProgress * -0.6f, 0.0f)

        //Rotation
        GlStateManager.rotate(pitch, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(yaw, 0.0f, 1f, 0f)
        GlStateManager.rotate(roll, 0f, 0f, 1f)

        GlStateManager.rotate(45f, 0.0f, 1f, 0f)

        val f = MathHelper.sin(swingProgress * swingProgress * Math.PI.toFloat())
        val f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * Math.PI.toFloat())
        GlStateManager.rotate(f * -20.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(f1 * -20.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.rotate(f1 * -80.0f, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(newSize, newSize, newSize)
        return true
    }

    fun swingItemHook() {
        val player = mc.thePlayer
        val stack: ItemStack = player.heldItem
        if (stack != null && stack.item != null && stack.item.onEntitySwing(player,stack)) {
            return
        }
        if (!player.isSwingInProgress || player.swingProgressInt >= this.getArmSwingAnimationEnd(player) / 2 || player.swingProgressInt < 0) {
            player.swingProgressInt = -1
            player.isSwingInProgress = true
        }
    }

    private fun getArmSwingAnimationEnd(player: EntityPlayerSP): Int {
        return if (player.isPotionActive(Potion.digSpeed)) 6 - (1 + player.getActivePotionEffect(Potion.digSpeed)
            .amplifier) * 1 else if (player.isPotionActive(
                Potion.digSlowdown
            )
        ) 6 + (1 + player.getActivePotionEffect(Potion.digSlowdown).amplifier) * 2 else 6
    }

}