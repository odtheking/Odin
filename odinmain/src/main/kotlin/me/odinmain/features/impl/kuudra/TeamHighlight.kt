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
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.OutlineUtils
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.LocationUtils
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeamHighlight : Module(
    name = "Team Highlight",
    description = "Highlights your teammates in Kuudra",
    category = Category.KUUDRA
) {
    private val playerOutline: Boolean by BooleanSetting("Player Outline", true, description = "Outlines the player")
    private val highlightName: Boolean by BooleanSetting("Name Highlight", true, description = "Highlights the player name")
    private val outlineColor: Color by ColorSetting("Outline Color", Color.PURPLE, true, description = "Color of the player outline").withDependency { playerOutline }
    private val nameColor: Color by ColorSetting("Name Color", Color.PINK, true, description = "Color of the name highlight").withDependency { highlightName }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (event.entity == mc.thePlayer || !playerOutline || LocationUtils.currentArea != "Kuudra") return
        val teammate = KuudraUtils.kuudraTeammates.find { it.entity == event.entity } ?: return

        OutlineUtils.outlineEntity(event, 5f, if (teammate.eatFresh && highlightFresh) highlightFreshColor else outlineColor, true)
    }

    @SubscribeEvent
    fun handleNames(event: RenderLivingEvent.Pre<*>) {
        if (highlightName && LocationUtils.currentArea == "Kuudra") renderTeammatesNames(event.entity)
    }

    private fun renderTeammatesNames(event: Entity) {
        if (event == mc.thePlayer) return
        val teammate = KuudraUtils.kuudraTeammates.find { it.entity == event } ?: return

        RenderUtils.drawStringInWorld(event.name, event.renderVec.addVec(y = 2.6),
            if (teammate.eatFresh) highlightFreshColor.rgba else nameColor.rgba,
            depthTest = false, increase = false, renderBlackBox = false,
            scale = 0.05f
        )
    }
}