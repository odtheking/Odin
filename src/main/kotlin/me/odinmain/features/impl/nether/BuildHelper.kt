package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.ui.Colors
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BuildHelper : Module(
    name = "Build Helper",
    description = "Displays various information about the current state of the ballista build."
) {
    private val buildHelperDraw by BooleanSetting("Render on Ballista", false, description = "Draws the build helper.")
    private val unfinishedWaypoints by BooleanSetting("Unfinished Waypoints", true, description = "Renders the unfinished piles waypoints.")
    private val fadeWaypoints by BooleanSetting("Fade Waypoints", true, description = "Fades the waypoints when close to them.")
    private val buildHelperColor by ColorSetting("Build Helper Color", Colors.MINECRAFT_GOLD, description = "Color of the build helper.")
    private val hud by HudSetting("Build helper HUD", 10f, 10f, 1f, true) {
        if (it) {
            text("Build §c50§8%", 1f, 9f, buildHelperColor, 12f, OdinFont.REGULAR, shadow = true)
            text("Builders §e2", 1f, 24f, buildHelperColor, 12f, OdinFont.REGULAR, shadow = true)
            text("Freshers: §e1", 1f, 39f, buildHelperColor, 12f, OdinFont.REGULAR, shadow = true)

            getTextWidth("4Build 50%", 12f) + 2f to 48f
        } else {
            if (KuudraUtils.phase != 2) return@HudSetting 0f to 0f

            text("Build ${colorBuild(KuudraUtils.buildDonePercentage)}§8%", 1f, 9f, buildHelperColor, 12f, OdinFont.REGULAR, shadow = true)
            text("Builders ${colorBuilders(KuudraUtils.playersBuildingAmount)}", 1f,  24f, buildHelperColor, 12f, OdinFont.REGULAR, shadow = true)
            text("Freshers: ${colorBuilders(KuudraUtils.kuudraTeammates.count { teammate -> teammate.eatFresh })}", 1f, 39f, buildHelperColor, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("4Build 50%", 12f) + 2f to 42f
        }
    }
    private val stunNotification by BooleanSetting("Stun Notification", true, description = "Notifies you when to go to stun.")
    private val stunNotificationNumber by NumberSetting("Stun Percent", 93, 0.0, 100.0, description = "The build % to notify at.", unit = "%").withDependency { stunNotification }

    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 2) return
        if (stunNotification && LocationUtils.kuudraTier > 2 && KuudraUtils.buildDonePercentage > stunNotificationNumber) PlayerUtils.alert("§lGo to stun", playSound = false, color = Colors.MINECRAFT_DARK_AQUA)
        if (buildHelperDraw)
            Renderer.drawStringInWorld("Build ${colorBuild(KuudraUtils.buildDonePercentage)}%", Vec3(-101.5, 84.0, -105.5), buildHelperColor, depth = false, scale = 0.15f)

        if (buildHelperDraw)
            Renderer.drawStringInWorld("Builders ${colorBuilders(KuudraUtils.playersBuildingAmount)}", Vec3(-101.5, 81.0, -105.5), buildHelperColor, depth = false, scale = 0.15f)

        if (unfinishedWaypoints)
            KuudraUtils.buildingPiles.forEach {
                Renderer.drawCustomBeacon(it.name, it.positionVector.addVec(0.5), Colors.MINECRAFT_DARK_RED, true, increase = false, noFade = !fadeWaypoints, distance = false)
            }
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