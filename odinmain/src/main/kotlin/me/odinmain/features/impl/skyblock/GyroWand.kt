package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.heldItem
import me.odinmain.utils.skyblock.itemID
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GyroWand : Module(
    "Gyro Wand",
    description = "Helpful features for the Gyrokinetic Wand",
    category = Category.SKYBLOCK
) {
    private val gyroRange: Boolean by BooleanSetting("Gyro Range", true, description = "Renders a helpful circle to show the range of the Gyrokinetic Wand.")
    private val color: Color by ColorSetting("Color", Color.MAGENTA.withAlpha(0.5f), allowAlpha = true).withDependency { gyroRange }
    private val thickness: Float by NumberSetting("Thickness", 0.4f, 0, 3, 0.05).withDependency { gyroRange }
    private val steps: Int by NumberSetting("Smoothness", 40, 20, 80, 1).withDependency { gyroRange }
    private val showCooldown: Boolean by BooleanSetting("Show Cooldown", true, description = "Shows the cooldown of the Gyrokinetic Wand.").withDependency { gyroRange }
    private val cooldownColor: Color by ColorSetting("Cooldown Color", Color.RED, allowAlpha = true).withDependency { showCooldown && gyroRange }
    private val blockWrongClicks: Boolean by BooleanSetting("Block aligning", true, description = "Blocks using the right click ability on Gyrokinetic Wand.")

    private val gyroCooldown = Clock(30_000)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (heldItem?.itemID != "GYROKINETIC_WAND" || !gyroRange) return
        val pos = mc.thePlayer.rayTrace(25.0, event.partialTicks)?.blockPos ?: return
        val block = mc.theWorld?.getBlockState(pos)?.block ?: return
        if (block.isAir(mc.theWorld, pos)) return
        val finalColor = if (showCooldown && !gyroCooldown.hasTimePassed()) cooldownColor else color

        Renderer.drawCylinder(
            Vec3(pos).addVector(0.5, 1.0, 0.5),
            10f, 10f - thickness, 0.2f,
            steps, 1,
            0f, 90f, 90f,
            finalColor
        )
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.RightClickEvent) {
        if (heldItem?.itemID != "GYROKINETIC_WAND" || !blockWrongClicks) return
        event.isCanceled = true
    }

    init {
        onMessage(Regex("(?s)(.*(-\\d+ Mana \\(Gravity Storm\\)).*)")) {
            gyroCooldown.update()
        }
    }
}