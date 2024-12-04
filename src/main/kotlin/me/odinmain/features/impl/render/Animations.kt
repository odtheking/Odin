package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.isHolding
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Parts taken from [Floppa Client](https://github.com/FloppaCoding/FloppaClient)
 */
object Animations : Module(
    name = "Animations",
    category = Category.RENDER,
    description = "Changes the appearance of the first-person view model."
) {
    val size by NumberSetting("Size", 0.0f, -1.5, 1.5, 0.05, description = "Scales the size of your currently held item. Default: 0")
    val x by NumberSetting("X", 0.0f, -2.5, 1.5, 0.05, description = "Moves the held item. Default: 0")
    val y by NumberSetting("Y", 0.0f, -1.5, 1.5, 0.05, description = "Moves the held item. Default: 0")
    val z by NumberSetting("Z", 0.0f, -1.5, 3.0, 0.05, description = "Moves the held item. Default: 0")
    private val yaw by NumberSetting("Yaw", 0.0f, -180.0, 180.0, 1.0, description = "Rotates your held item. Default: 0")
    private val pitch by NumberSetting("Pitch", 0.0f, -180.0, 180.0, 1.0, description = "Rotates your held item. Default: 0")
    private val roll by NumberSetting("Roll", 0.0f, -180.0, 180.0, 1.0, description = "Rotates your held item. Default: 0")
    val speed by NumberSetting("Speed", 0.0f, -2.0, 1.0, 0.05, description = "Speed of the swing animation.")
    val ignoreHaste by BooleanSetting("Ignore Haste", false, description = "Makes the chosen speed override haste modifiers.")
    val blockHit by BooleanSetting("Block Hit", false, description = "Visual 1.7 block hit animation.")
    private val noEquipReset by BooleanSetting("No Equip Reset", false, description = "Disables the equipping animation when switching items.")
    private val noSwing by BooleanSetting("No Swing", false, description = "Prevents your item from visually swinging forward.")
    private val noTermSwing by BooleanSetting("No Terminator Swing", false, description = "Prevents your Terminator from swinging.")

    val reset by ActionSetting("Reset", description = "Resets the settings to their default values.") {
        settings.forEach { it.reset() }
    }

    @JvmStatic
    val shouldNoEquipReset get() = enabled && noEquipReset

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

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        val player = mc.thePlayer ?: return
        if (noTermSwing && isHolding("TERMINATOR")) {
            player.isSwingInProgress = false
            player.swingProgress = 0f
            player.swingProgressInt = -1
            return
        }
    }
}