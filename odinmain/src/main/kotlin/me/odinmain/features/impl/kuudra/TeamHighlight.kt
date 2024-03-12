package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.kuudra.FreshTimer.highlightFresh
import me.odinmain.features.impl.kuudra.FreshTimer.highlightFreshColor
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.distanceSquaredTo
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.OutlineUtils
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.kuudraTeammates
import me.odinmain.utils.skyblock.LocationUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeamHighlight : Module(
    name = "Team Highlight",
    description = "Highlights your teammates in Kuudra.",
    category = Category.KUUDRA
) {
    private val playerOutline: Boolean by BooleanSetting("Player Outline", true, description = "Outlines the player")
    private val highlightName: Boolean by BooleanSetting("Name Highlight", true, description = "Highlights the player name")
    private val outlineColor: Color by ColorSetting("Outline Color", Color.PURPLE, true, description = "Color of the player outline").withDependency { playerOutline }
    private val nameColor: Color by ColorSetting("Name Color", Color.PINK, true, description = "Color of the name highlight").withDependency { highlightName }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (event.entity == mc.thePlayer || !playerOutline || LocationUtils.currentArea != Island.Kuudra) return
        val teammate = kuudraTeammates.find { it.entity == event.entity } ?: return

        OutlineUtils.outlineEntity(event, 5f, if (teammate.eatFresh && highlightFresh) highlightFreshColor else outlineColor, true)
    }

    @SubscribeEvent
    fun handleNames(event: RenderWorldLastEvent) {
        if (!highlightName || LocationUtils.currentArea != Island.Kuudra || KuudraUtils.phase < 1) return
        kuudraTeammates.forEach {
             if (it.entity == null || it.playerName == mc.thePlayer.name) return@forEach
            if ((it.entity?.distanceSquaredTo(mc.thePlayer) ?: return@forEach) >= 2333) return@forEach

            Renderer.drawStringInWorld(
                it.playerName, it.entity?.renderVec?.addVec(y = 2.6) ?: return@forEach,
                if (it.eatFresh) highlightFreshColor else nameColor,
                depth = false, renderBlackBox = false,
                scale = 0.05f
            )
        }
    }
}