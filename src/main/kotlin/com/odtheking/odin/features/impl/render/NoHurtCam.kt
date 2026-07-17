package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module

object NoHurtCam : Module(
    name = "No Hurt Cam",
    description = "Removes or reduces the camera tilt when you take damage."
) {
    private val tiltStrength by NumberSetting("Tilt Strength", 0, 0, 100, 1, desc = "Percentage of the vanilla damage tilt to keep. 0% fully removes it.", unit = "%")

    /**
     * @see com.odtheking.mixin.mixins.GameRendererMixin.modifyDamageTilt
     */
    @JvmStatic
    fun getDamageTiltMultiplier(): Double =
        if (enabled) tiltStrength / 100.0 else 1.0
}
