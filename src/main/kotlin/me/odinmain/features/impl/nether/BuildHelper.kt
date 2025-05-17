package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.ui.Colors
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BuildHelper : Module(
    name = "Build Helper",
    desc = "Displays various information about the current state of the ballista build."
) {
    private val buildHelperDraw by BooleanSetting("Render on Ballista", false, desc = "Draws the build helper.")
    private val unfinishedWaypoints by BooleanSetting("Unfinished Waypoints", true, desc = "Renders the unfinished piles waypoints.")
    private val fadeWaypoints by BooleanSetting("Fade Waypoints", true, desc = "Fades the waypoints when close to them.")
    private val buildHelperColor by ColorSetting("Build Helper Color", Colors.MINECRAFT_GOLD, desc = "Color of the build helper.")
    private val hud by HudSetting("Build helper HUD", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("Build §c50§8%", 1f, 1f, 1f, buildHelperColor, shadow = true)
            RenderUtils.drawText("Builders §e2", 1f, 12f, 1f, buildHelperColor, shadow = true)
            RenderUtils.drawText("Freshers: §e1", 1f, 24f, 1f, buildHelperColor, shadow = true)

            getMCTextWidth("Freshers: 1") + 2f to 36f
        } else {
            if (KuudraUtils.phase != 2) return@HudSetting 0f to 0f

            RenderUtils.drawText("Build ${colorBuild(KuudraUtils.buildDonePercentage)}§8%", 1f,1f, 1f, buildHelperColor, shadow = true)
            RenderUtils.drawText("Builders ${colorBuilders(KuudraUtils.playersBuildingAmount)}", 1f, 12f, 1f, buildHelperColor, shadow = true)
            RenderUtils.drawText("Freshers: ${colorBuilders(KuudraUtils.kuudraTeammates.count { teammate -> teammate.eatFresh })}", 1f, 24f, 1f, buildHelperColor, shadow = true)
            getMCTextWidth("4Build 50%") + 2f to 36f
        }
    }
    private val stunNotification by BooleanSetting("Stun Notification", true, desc = "Notifies you when to go to stun.")
    private val stunNotificationNumber by NumberSetting("Stun Percent", 93, 0.0, 100.0, desc = "The build % to notify at.", unit = "%").withDependency { stunNotification }

    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 2) return
        if (stunNotification && KuudraUtils.kuudraTier > 2 && KuudraUtils.buildDonePercentage > stunNotificationNumber) PlayerUtils.alert("§lGo to stun", playSound = false, color = Colors.MINECRAFT_DARK_AQUA)
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