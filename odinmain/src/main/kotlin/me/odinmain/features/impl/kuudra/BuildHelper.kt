package me.odinmain.features.impl.kuudra

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.getTextWidth
import me.odinmain.ui.util.text
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BuildHelper : Module(
    name = "Build Helper",
    description = "Helps you build in Kuudra.",
    category = Category.KUUDRA
) {
    private val buildHelperDraw: Boolean by BooleanSetting("Build Helper Draw", false, description = "Draws the build helper")
    private val buildHelperColor: Color by ColorSetting("Build Helper Color", Color.ORANGE, description = "Color of the build helper")
    private val buildHud: HudElement by HudSetting("Build helper", 10f, 10f, 1f, true) {
        if (it) {
            text("Build §c50§8%", 1f, 9f, buildHelperColor, 12f, OdinFont.REGULAR)
            text("Builders §e2", 1f, 24f, buildHelperColor, 12f, OdinFont.REGULAR)

            getTextWidth("4Build 50%", 12f) + 2f to 34f
        } else {
            if (KuudraUtils.phase != 2) return@HudSetting 0f to 0f
            text("Build ${colorBuild(KuudraUtils.build)}§8%", 1f, 9f, buildHelperColor, 12f, OdinFont.REGULAR)
            text("Builders ${colorBuilders(KuudraUtils.builders)}", 1f,  24f, buildHelperColor, 12f, OdinFont.REGULAR)

            getTextWidth("4Build 50%", 12f) + 2f to 34f
        }
    }
    private val stunNotification: Boolean by BooleanSetting("Stun Notification", true, description = "Notifies you when to go to stun")
    private val stunNotificationNumber: Int by NumberSetting("Stun Notification Number", 93, 0.0, 100.0, description = "The number of builders to notify you at").withDependency { stunNotification }
    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldEvent) {
        if (stunNotification && KuudraUtils.build == stunNotificationNumber) PlayerUtils.alert("Go to stun")
        if (buildHelperDraw) RenderUtils.drawStringInWorld("Build ${KuudraUtils.build}%", Vec3(0.0, 0.5, 0.0), buildHelperColor.rgba, false, false, scale = 0.3f)
        if (buildHelperDraw) RenderUtils.drawStringInWorld("${KuudraUtils.builders} builders", Vec3(0.0, 0.0, 0.0), buildHelperColor.rgba, false, false, scale = 0.5f)
    }

    private fun colorBuild(build: Int): String {
        return when {
            build >= 75 -> "§a$build"
            build >= 50 -> "§e$build"
            build >= 25 -> "§6$build"
            else -> "§c$build"
        }
    }

    private fun colorBuilders(builders: Int): String {
        return when {
            builders >= 3 -> "§a$builders"
            builders >= 2 -> "§e$builders"
            else -> "§c$builders"
        }
    }
}