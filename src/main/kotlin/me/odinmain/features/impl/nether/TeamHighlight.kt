package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.impl.nether.FreshTimer.highlightFresh
import me.odinmain.features.impl.nether.FreshTimer.highlightFreshColor
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.kuudraTeammates
import me.odinmain.utils.ui.Colors
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeamHighlight : Module(
    name = "Team Highlight",
    desc = "Highlights your teammates in Kuudra."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showHighlight by BooleanSetting("Show highlight", true, desc = "Highlights teammates with an outline.")
    private val showName by BooleanSetting("Show name", true, desc = "Highlights teammates with a name tag.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Highlights teammates only when they are visible.")
    private val outlineColor by ColorSetting("Outline Color", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the player outline.").withDependency { showHighlight }
    private val nameColor by ColorSetting("Name Color", Colors.MINECRAFT_LIGHT_PURPLE, true, desc = "Color of the name highlight.").withDependency { showName }

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

        RenderUtils.drawMinecraftLabel(teammate.playerName, Vec3( event.x, event.y + 0.5, event.z), 0.05, false, if (teammate.eatFresh) highlightFreshColor else nameColor)
        event.isCanceled = true
    }
}