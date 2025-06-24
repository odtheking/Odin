package me.odinmain.features.impl.nether

import com.github.stivais.aurora.color.Color
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.ui.Colors
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BuildHelper : Module(
    name = "Build Helper",
    description = "Helps you to build the ballista in Kuudra."
) {
    private val buildHelperDraw by BooleanSetting("Render on Ballista", false, description = "Draws the build helper.")
    private val unfinishedWaypoints by BooleanSetting("Unfinished Waypoints", true, description = "Renders the unfinished piles waypoints.")
    private val fadeWaypoints by BooleanSetting("Fade Waypoints", true, description = "Fades the waypoints when close to them.")


    private val stunNotification by BooleanSetting("Stun Notification", true, description = "Notifies you when to go to stun.")
    private val stunNotificationNumber by NumberSetting("Stun Percent", 93, 0.0, 100.0, description = "The build % to notify at.", unit = "%").withDependency { stunNotification }

    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 2) return
        if (stunNotification && KuudraUtils.buildDonePercentage > stunNotificationNumber) PlayerUtils.alert("§lGo to stun", playSound = false, color = Colors.MINECRAFT_AQUA)
        if (buildHelperDraw)
            Renderer.drawStringInWorld("Build: ${colorBuild(KuudraUtils.buildDonePercentage)}%", Vec3(-101.5, 84.0, -105.5), Color.WHITE, depth = false, scale = 0.15f)

        if (buildHelperDraw)
            Renderer.drawStringInWorld("Builders: ${colorBuilders(KuudraUtils.playersBuildingAmount)}", Vec3(-101.5, 81.0, -105.5), Color.WHITE, depth = false, scale = 0.15f)

        if (unfinishedWaypoints)
            KuudraUtils.buildingPiles.forEach {
                Renderer.drawCustomBeacon(it.name, it.positionVector.addVec(0.5), Colors.MINECRAFT_DARK_RED, true, increase = false, noFade = !fadeWaypoints, distance = false)
            }
    }

    private fun colorBuild(build: Int): Color {
        return when {
            build >= 75 -> Colors.MINECRAFT_GREEN
            build >= 50 -> Colors.MINECRAFT_GOLD
            build >= 25 -> Colors.MINECRAFT_GOLD
            else -> Colors.MINECRAFT_RED
        }
    }

    private fun colorBuilders(builders: Int): Color {
        return when {
            builders >= 3 -> Colors.MINECRAFT_GREEN
            builders >= 2 -> Colors.MINECRAFT_GOLD
            else -> Colors.MINECRAFT_RED
        }
    }
}