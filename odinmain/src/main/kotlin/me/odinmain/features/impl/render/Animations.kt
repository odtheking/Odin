package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.isHolding
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.potion.Potion
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.*

/**
 * Parts taken from [Floppa Client](https://github.com/FloppaCoding/FloppaClient)
 */
object Animations : Module(
    name = "Animations",
    category = Category.RENDER,
    description = "Changes the appearance of the first-person view model."
) {
    val size: Float by NumberSetting("Size", 0.0f, -1.5, 1.5, 0.05, description = "Scales the size of your currently held item. Default: 0")
    val x: Float by NumberSetting("X", 0.0f, -2.5, 1.5, 0.05, description = "Moves the held item. Default: 0")
    val y: Float by NumberSetting("Y", 0.0f, -1.5, 1.5, 0.05, description = "Moves the held item. Default: 0")
    val z: Float by NumberSetting("Z", 0.0f, -1.5, 3.0, 0.05, description = "Moves the held item. Default: 0")
    private val yaw: Float by NumberSetting("Yaw", 0.0f, -180.0, 180.0, 1.0, description = "Rotates your held item. Default: 0")
    private val pitch: Float by NumberSetting("Pitch", 0.0f, -180.0, 180.0, 1.0, description = "Rotates your held item. Default: 0")
    private val roll: Float by NumberSetting("Roll", 0.0f, -180.0, 180.0, 1.0, description = "Rotates your held item. Default: 0")
    val speed: Float by NumberSetting("Speed", 0.0f, -2.0, 1.0, 0.05, description = "Speed of the swing animation.")
    val ignoreHaste: Boolean by BooleanSetting("Ignore Haste", false, description = "Makes the chosen speed override haste modifiers.")
    val blockHit: Boolean by BooleanSetting("Block Hit", false, description = "Visual 1.7 block hit animation")
    val noEquipReset: Boolean by BooleanSetting("No Equip Reset", false, description = "Disables the equipping animation when switching items")
    private val noSwing: Boolean by BooleanSetting("No Swing", false, description = "Prevents your item from visually swinging forward")
    private val noTermSwing: Boolean by BooleanSetting("No Terminator Swing", false, description = "Prevents your Terminator from swinging")
    val noBlock: Boolean by BooleanSetting("No Block", false, description = "Disables the visual block animation")

    val reset: () -> Unit by ActionSetting("Reset") {
        this.settings.forEach { it.reset() }
    }

    val shouldStopSwing get() = enabled && noSwing

    fun itemTransferHook(equipProgress: Float, swingProgress: Float): Boolean {
        if (!enabled) return false
        val newSize = (0.4 * exp(size)).toFloat()
        val newX = (0.56f * (1 + x))
        val newY = (-0.52f * (1 - y))
        val newZ = (-0.71999997f * (1 + z))
        GlStateManager.translate(newX, newY, newZ)
        GlStateManager.translate(0f, equipProgress * -.6f, 0f)

        //Rotation
        GlStateManager.rotate(pitch,     1f, 0f, 0f)
        GlStateManager.rotate(yaw + 45f, 0f, 1f, 0f)
        GlStateManager.rotate(roll,      0f, 0f, 1f)

        val f = sin(swingProgress * swingProgress * Math.PI.toFloat())
        val f1 = sin(sqrt(swingProgress) * Math.PI.toFloat())
        GlStateManager.rotate(f  * -20f, 0f, 1f, 0f)
        GlStateManager.rotate(f1 * -20f, 0f, 0f, 1f)
        GlStateManager.rotate(f1 * -80f, 1f, 0f, 0f)
        GlStateManager.scale(newSize, newSize, newSize)
        return true
    }

    fun getItemInUseCountHook(player: AbstractClientPlayer, itemToRender: ItemStack): Int {
        return if (this.noBlock && itemToRender.item is ItemSword && player.itemInUseDuration <= 7) 0
        else player.itemInUseCount
    }

    private fun getArmSwingAnimationEnd(player: EntityPlayerSP): Int {
        val length =
            if (ignoreHaste) 6
            else if (player.isPotionActive(Potion.digSpeed)) 6 - (1 + player.getActivePotionEffect(Potion.digSpeed).amplifier)
            else if (player.isPotionActive(Potion.digSlowdown)) 6 + (1 + player.getActivePotionEffect(Potion.digSlowdown).amplifier) * 2
            else 6
        return max((length * exp(-speed)),1.0f).toInt()
    }

    /**
    Taken from [Sk1erLLC's OldAnimations Mod](https://github.com/Sk1erLLC/OldAnimations) to enable block hitting
     */
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if ( event.phase != TickEvent.Phase.END) return
        val player = mc.thePlayer ?: return
        if (noTermSwing && isHolding("TERMINATOR")) {
            player.isSwingInProgress = false
            player.swingProgress = 0f
            player.swingProgressInt = -1
            return
        }

        if (!blockHit) return

        if (mc.gameSettings.keyBindAttack.isKeyDown && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit === MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!player.isSwingInProgress || player.swingProgressInt >= getArmSwingAnimationEnd(player) / 2 || player.swingProgressInt < 0) {
                player.isSwingInProgress = true
                player.swingProgressInt = -1
            }
        }
    }
}