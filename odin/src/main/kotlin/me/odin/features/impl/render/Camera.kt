package me.odin.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera : Module(
    "Camera",
    category = Category.RENDER,
    description = "Allows you to change qualities about third person view."
) {
    private val frontCamera: Boolean by BooleanSetting("No Front Camera")
    private val fov: Float by NumberSetting("FOV", 70f, 1f, 180f, 1f)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.gameSettings.fovSetting != fov)
            mc.gameSettings.fovSetting = fov

        if (frontCamera && mc.gameSettings.thirdPersonView == 2)
            mc.gameSettings.thirdPersonView = 0
    }
}