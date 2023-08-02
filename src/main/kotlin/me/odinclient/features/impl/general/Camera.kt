package me.odinclient.features.impl.general

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.Hud
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.nvg.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera : Module(
    "Camera",
    category = Category.GENERAL
) {
    private val frontCamera: Boolean by BooleanSetting("No Front Camera", false)
    private val cameraDist: Float by NumberSetting("Distance", 4f, 3.0, 12.0, 0.5)

    private val color: Color by ColorSetting("HUD Color", Color(255, 0, 0))

    fun getCameraDistance(): Float = if (enabled) cameraDist else 4f

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (frontCamera && mc.gameSettings.thirdPersonView == 2) {
            mc.gameSettings.thirdPersonView = 0
        }
    }

    @Hud("Name", false, false)
    object CameraHud : HudElement(
        10f, 10f
    ) {
        override fun render(vg: NVG, example: Boolean): Pair<Float, Float> {
            val string = if (example) "Example Hud" else "Not Example Hud"

            vg.text(string, 0f, 0f, color, 16f, Fonts.REGULAR, TextAlign.Left, TextPos.Top)

            return vg.getTextWidth(string, 16f, Fonts.REGULAR) to 16f
        }
    }
}