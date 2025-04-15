package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
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
    desc = "Changes the appearance of the first-person view model."
) {
    private val size by NumberSetting("Size", 0.0f, -1.5, 1.5, 0.05, desc = "Scales the size of your currently held item. Default: 0")
    private val scaleSwing by BooleanSetting("Scale Swing", true, desc = "Scales the swing animation.").withDependency { !noSwing }
    private val x by NumberSetting("X", 0.0f, -2.5, 1.5, 0.05, desc = "Moves the held item. Default: 0")
    private val y by NumberSetting("Y", 0.0f, -1.5, 1.5, 0.05, desc = "Moves the held item. Default: 0")
    private val z by NumberSetting("Z", 0.0f, -1.5, 3.0, 0.05, desc = "Moves the held item. Default: 0")
    private val yaw by NumberSetting("Yaw", 0.0f, -180.0, 180.0, 1.0, desc = "Rotates your held item. Default: 0")
    private val pitch by NumberSetting("Pitch", 0.0f, -180.0, 180.0, 1.0, desc = "Rotates your held item. Default: 0")
    private val roll by NumberSetting("Roll", 0.0f, -180.0, 180.0, 1.0, desc = "Rotates your held item. Default: 0")
    val speed by NumberSetting("Speed", 0.0f, -2.0, 1.0, 0.05, desc = "Speed of the swing animation.")
    val ignoreHaste by BooleanSetting("Ignore Haste", false, desc = "Makes the chosen speed override haste modifiers.")
    private val noEquipReset by BooleanSetting("No Equip Reset", false, desc = "Disables the equipping animation when switching items.")
    private val noSwing by BooleanSetting("No Swing", false, desc = "Prevents your item from visually swinging forward.")
    private val noTermSwing by BooleanSetting("No Terminator Swing", false, desc = "Prevents your Terminator from swinging.")

    private val reset by ActionSetting("Reset", desc = "Resets the settings to their default values.") {
        settings.forEach { it.reset() }
    }

    @JvmStatic
    val shouldNoEquipReset get() = enabled && noEquipReset

    @JvmStatic
    val shouldStopSwing get() = enabled && noSwing

    @JvmStatic
    fun itemTransferHook(equipProgress: Float, swingProgress: Float): Boolean {
        if (!enabled) return false
        val newSize = 0.4f * exp(size)
        GlStateManager.translate(0.56f * (1 + x), -0.52f * (1 - y), -0.71999997f * (1 + z))
        GlStateManager.translate(0f, equipProgress * -.6f, 0f)

        GlStateManager.rotate(pitch,     1f, 0f, 0f)
        GlStateManager.rotate(yaw + 45f, 0f, 1f, 0f)
        GlStateManager.rotate(roll,      0f, 0f, 1f)

        val f1 = sin(sqrt(swingProgress) * Math.PI.toFloat())
        GlStateManager.rotate(sin(swingProgress * swingProgress * Math.PI.toFloat())  * -20f, 0f, 1f, 0f)
        GlStateManager.rotate(f1 * -20f, 0f, 0f, 1f)
        GlStateManager.rotate(f1 * -80f, 1f, 0f, 0f)
        GlStateManager.scale(newSize, newSize, newSize)
        return true
    }

    @JvmStatic
    fun scaledSwing(swingProgress: Float): Boolean {
        if (!scaleSwing || !enabled) return false
        val scale = exp(size)
        val f = -0.4f * sin(sqrt(swingProgress) * Math.PI.toFloat()) * scale
        val f1 = 0.2f * sin(sqrt(swingProgress) * Math.PI.toFloat() * 2.0f) * scale
        val f2 = -0.2f * sin(swingProgress * Math.PI.toFloat()) * scale
        GlStateManager.translate(f, f1, f2)
        return true
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || !(noTermSwing && isHolding("TERMINATOR"))) return

        mc.thePlayer?.let {
            it.isSwingInProgress = false
            it.swingProgress = 0f
            it.swingProgressInt = -1
        }
    }
}