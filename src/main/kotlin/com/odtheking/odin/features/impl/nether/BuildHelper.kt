package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.render.drawCustomBeacon
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.KuudraUtils
import net.minecraft.world.phys.Vec3

object BuildHelper : Module(
    name = "Build Helper",
    description = "Displays various information about the current state of the ballista build."
) {
    private val buildHelperDraw by BooleanSetting("Render on Ballista", false, desc = "Draws the build information on the ballista.")
    private val unfinishedWaypoints by BooleanSetting("Unfinished Waypoints", true, desc = "Draws waypoints over the unfinished piles.")
    private val hideDefaultTag by BooleanSetting("Hide Default Tag", true, desc = "Hides the default tag for unfinished piles.").withDependency { unfinishedWaypoints }
    private val hud by HUD(name, "Shows information about the build progress.") { example ->
        if (!example && (!KuudraUtils.inKuudra || KuudraUtils.phase != 2)) return@HUD 0 to 0
        val width = textDim("§bFreshers: ${colorBuilders(KuudraUtils.freshers.size)}", 0, 0).first
        text("§bBuilders: ${colorBuilders(KuudraUtils.playersBuildingAmount)}", 0, 9)
        text("§bBuild: ${colorBuild(KuudraUtils.buildDonePercentage)}%", 0, 18)
        width to mc.font.lineHeight * 3
    }

    private val stunNotificationNumber by NumberSetting("Stun Percent", 93f, 0..100, desc = "The build % to notify at (set to 0 to disable).", unit = "%")

    init {
        on<RenderEvent.Extract> {
            if (!KuudraUtils.inKuudra || KuudraUtils.phase != 2) return@on
            if (stunNotificationNumber != 0f && KuudraUtils.kuudraTier >= 3 && KuudraUtils.buildDonePercentage >= stunNotificationNumber)
                alert("§l§3Go to stun", false)
            if (buildHelperDraw)
                drawText(
                    "§bBuild §c${colorBuild(KuudraUtils.buildDonePercentage)}%",
                    Vec3(-101.5, 82.0, -105.5),
                    3f,
                    false
                )

            if (buildHelperDraw)
                drawText(
                    "§bBuilders ${colorBuilders(KuudraUtils.playersBuildingAmount)}",
                    Vec3(-101.5, 81.0, -105.5),
                    3f,
                    false
                )

            if (unfinishedWaypoints)
                KuudraUtils.buildingPiles.forEach {
                    drawCustomBeacon(
                        it.name.string,
                        it.blockPosition(),
                        Colors.MINECRAFT_DARK_RED,
                        increase = false,
                        distance = false
                    )
                    if (hideDefaultTag) it.isCustomNameVisible = false
                }
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