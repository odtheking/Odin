package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getAbilityCooldown
import me.odinmain.utils.skyblock.isAir
import me.odinmain.utils.skyblock.isHolding
import me.odinmain.utils.toVec3
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GyroWand : Module(
    name = "Gyro Wand",
    desc = "Shows area of effect and cooldown of the Gyrokinetic Wand."
) {
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_PURPLE.withAlpha(0.5f), allowAlpha = true, desc = "The color of the Gyrokinetic Wand range.")
    private val thickness by NumberSetting("Thickness", 0.4f, 0, 10, 0.05f, desc = "The thickness of the Gyrokinetic Wand range.")
    private val steps by NumberSetting("Smoothness", 40, 20, 80, 1, desc = "The amount of steps to use when rendering the Gyrokinetic Wand range.")
    private val showCooldown by BooleanSetting("Show Cooldown", true, desc = "Shows the cooldown of the Gyrokinetic Wand.")
    private val cooldownColor by ColorSetting("Cooldown Color", Colors.MINECRAFT_RED.withAlpha(0.5f), allowAlpha = true, desc = "The color of the cooldown of the Gyrokinetic Wand.").withDependency { showCooldown }

    private val gyroCooldown = Clock(30_000L)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!isHolding("GYROKINETIC_WAND")) return
        val position = EtherWarpHelper.getEtherPos(distance = 25.0).pos?.takeIf { !isAir(it) }?.toVec3() ?: return

        Renderer.drawCylinder(
            position.addVector(0.5, 1.0, 0.5),
            10f, 10f - thickness, 0.2f,
            steps, 1, 0f, 90f, 90f, if (showCooldown && !gyroCooldown.hasTimePassed(getAbilityCooldown(30_000L))) cooldownColor else color
        )
    }

    init {
        onMessage(Regex("(?s)(.*(-\\d+ Mana \\(Gravity Storm\\)).*)")) {
            gyroCooldown.update()
        }
    }
}
