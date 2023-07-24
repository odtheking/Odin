package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ItemUtils.itemID
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object GyroRange : Module(
    "Gyro Range",
    category = Category.QOL
) {
    private val color: Color by ColorSetting("Color", Color(192, 64, 192, 128), allowAlpha = true)
    private val thickness: Float by NumberSetting("Thickness", 5f, 0.0, 10.0, 0.5)
    private val steps: Int by NumberSetting("Steps", 40, 20.0, 80.0, 1.0)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (mc.thePlayer?.heldItem?.itemID != "GYROKINETIC_WAND") return
        val pos = mc.thePlayer.rayTrace(25.0, event.partialTicks)?.blockPos ?: return
        val block = mc.theWorld?.getBlockState(pos)?.block ?: return
        if (block.isAir(mc.theWorld, pos)) return

        RenderUtils.drawCylinder(
            Vec3(pos).addVector(0.5, 1.0, 0.5),
            10f, 10f - thickness, 0.2f,
            steps, 1,
            0f, 90f, 90f,
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f
        )
    }
}
