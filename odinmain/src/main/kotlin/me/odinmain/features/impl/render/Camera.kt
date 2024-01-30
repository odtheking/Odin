package me.odinmain.features.impl.render

import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
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
    private val cameraClip: Boolean by BooleanSetting("Camera Clip").withDependency { !OdinMain.onLegitVersion }
    private val cameraDist: Float by NumberSetting("Distance", 4f, 3.0, 12.0, 0.1).withDependency { !OdinMain.onLegitVersion }

    fun getCameraDistance(): Float {
        return if (enabled && !OdinMain.onLegitVersion) cameraDist else 4f
    }

    fun getCameraClipEnabled(): Boolean {
        return if (enabled && !OdinMain.onLegitVersion) cameraClip else false
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (frontCamera && mc.gameSettings.thirdPersonView == 2) {
            mc.gameSettings.thirdPersonView = 0
        }
    }
}