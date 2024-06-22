package me.odinmain.features.impl.nether

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.nether.FreshTimer.highlightFresh
import me.odinmain.features.impl.nether.FreshTimer.highlightFreshColor
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.distanceSquaredTo
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.OutlineUtils
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.kuudraTeammates
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeamHighlight : Module(
    name = "Team Highlight",
    description = "Highlights your teammates in Kuudra.",
    category = Category.NETHER
) {
    private val playerOutline: Boolean by BooleanSetting("Player Outline", true, description = "Outlines the player")
    private val highlightName: Boolean by BooleanSetting("Name Highlight", true, description = "Highlights the player name")
    private val outlineColor: Color by ColorSetting("Outline Color", Color.PURPLE, true, description = "Color of the player outline").withDependency { playerOutline }
    private val nameColor: Color by ColorSetting("Name Color", Color.PINK, true, description = "Color of the name highlight").withDependency { highlightName }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (event.entity == mc.thePlayer || !playerOutline || !KuudraUtils.inKuudra || KuudraUtils.phase < 1) return
        val teammate = kuudraTeammates.find { it.entity == event.entity } ?: return

        OutlineUtils.outlineEntity(event, 5f, if (teammate.eatFresh && highlightFresh) highlightFreshColor else outlineColor, true)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!highlightName || !KuudraUtils.inKuudra || KuudraUtils.phase < 1) return

        kuudraTeammates.forEach{ teammate ->
            if (teammate.entity == mc.thePlayer || teammate.entity == null) return@forEach
            if (teammate.entity?.let { mc.thePlayer.distanceSquaredTo(it) >= 2333 } == true) return@forEach

            Renderer.drawStringInWorld(
                teammate.playerName, teammate.entity?.renderVec?.addVec(y = 2.6) ?: return,
                if (teammate.eatFresh) highlightFreshColor else nameColor,
                depth = false, scale = 0.05f
            )
        }
    }
}