package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.nether.FreshTimer.highlightFresh
import me.odinmain.features.impl.nether.FreshTimer.highlightFreshColor
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.kuudraTeammates
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeamHighlight : Module(
    name = "Team Highlight",
    description = "Highlights your teammates in Kuudra.",
    category = Category.NETHER
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, description = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showHighlight by BooleanSetting("Show highlight", true, description = "Highlights teammates with an outline.")
    private val showName by BooleanSetting("Show name", true, description = "Highlights teammates with a name tag.")
    private val depthCheck by BooleanSetting("Depth check", false, description = "Highlights teammates only when they are visible.")
    private val outlineColor by ColorSetting("Outline Color", Color.PURPLE, true, description = "Color of the player outline.").withDependency { showHighlight }
    private val nameColor by ColorSetting("Name Color", Color.PINK, true, description = "Color of the name highlight.").withDependency { showName }

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || !KuudraUtils.inKuudra || KuudraUtils.phase < 1 || !showHighlight) emptyList()
            else {
                kuudraTeammates.mapNotNull {
                    if (it.entity == mc.thePlayer) return@mapNotNull null
                    it.entity?.let { entity -> HighlightRenderer.HighlightEntity(entity, if (it.eatFresh && highlightFresh) highlightFreshColor else outlineColor, thickness, depthCheck, style) }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderEntity(event: RenderLivingEvent.Specials.Pre<EntityOtherPlayerMP>) {
        if (!showName || !KuudraUtils.inKuudra || KuudraUtils.phase < 1 || event.entity == mc.thePlayer) return
        val teammate = kuudraTeammates.find { it.entity == event.entity } ?: return

        RenderUtils.drawMinecraftLabel(event.entity, teammate.playerName, event.x, event.y + 0.5, event.z, 0.05, false, if (teammate.eatFresh) highlightFreshColor else nameColor)
        event.isCanceled = true
    }
}