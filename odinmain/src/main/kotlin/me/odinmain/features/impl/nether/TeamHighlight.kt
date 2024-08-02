package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.nether.FreshTimer.highlightFresh
import me.odinmain.features.impl.nether.FreshTimer.highlightFreshColor
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.addVec
import me.odinmain.utils.distanceSquaredTo
import me.odinmain.utils.render.*
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.kuudraTeammatesNoSelf
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeamHighlight : Module(
    name = "Team Highlight",
    description = "Highlights your teammates in Kuudra.",
    category = Category.NETHER
) {
    private val mode: Int by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList)
    private val thickness: Float by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode != HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showHighlight: Boolean by BooleanSetting("Show highlight", true, description = "Highlights teammates with an outline.")
    private val showName: Boolean by BooleanSetting("Show name", true, description = "Highlights teammates with a name tag.")
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Highlights teammates only when they are visible.")
    private val outlineColor: Color by ColorSetting("Outline Color", Color.PURPLE, true, description = "Color of the player outline").withDependency { showHighlight }
    private val nameColor: Color by ColorSetting("Name Color", Color.PINK, true, description = "Color of the name highlight").withDependency { showName }

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || !KuudraUtils.inKuudra || KuudraUtils.phase < 1 || !showHighlight) emptyList()
            else {
                kuudraTeammatesNoSelf.mapNotNull {
                    it.entity?.let { entity -> HighlightRenderer.HighlightEntity(entity, if (it.eatFresh && highlightFresh) highlightFreshColor else outlineColor, thickness, depthCheck, style) }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!showName || !KuudraUtils.inKuudra || KuudraUtils.phase < 1) return

        kuudraTeammatesNoSelf.forEach{ teammate ->
            if (teammate.entity == null) return@forEach
            if (teammate.entity?.let { mc.thePlayer.distanceSquaredTo(it) >= 2100 } == true) return@forEach

            Renderer.drawStringInWorld(
                teammate.playerName, teammate.entity?.renderVec?.addVec(y = 2.6) ?: return,
                if (teammate.eatFresh) highlightFreshColor else nameColor,
                depth = false, scale = 0.05f
            )
        }
    }
}