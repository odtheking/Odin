package me.odinmain.features.impl.dungeon

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeammatesHighlight : Module(
    name = "Teammate Highlight",
    description = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes / 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showClass by BooleanSetting("Show class", true, desc = "Shows the class of the teammate.")
    private val showHighlight by BooleanSetting("Show highlight", true, desc = "Highlights teammates with an outline.")
    private val showName by BooleanSetting("Show name", true, desc = "Highlights teammates with a name tag.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Highlights teammates only when they are visible.")
    private val inBoss by BooleanSetting("In boss", true, desc = "Highlights teammates in boss rooms.")

    private inline val shouldRender get() = (inBoss || !DungeonUtils.inBoss) && DungeonUtils.inDungeons

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || !shouldRender || !showHighlight) emptyList()
            else dungeonTeammatesNoSelf.mapNotNull { it.entity?.let { entity -> HighlightRenderer.HighlightEntity(entity, it.clazz.color, thickness, depthCheck, style) } }
        }
    }

    @SubscribeEvent
    fun onRenderEntity(event: RenderLivingEvent.Specials.Pre<EntityOtherPlayerMP>) {
        if (!showName || !shouldRender) return
        val teammate = dungeonTeammatesNoSelf.find { it.entity == event.entity } ?: return
        event.isCanceled = true
        RenderUtils.drawMinecraftLabel(
            if (showClass) "§${teammate.clazz.colorCode}${teammate.name} §e[${teammate.clazz.name[0]}]" else "§${teammate.clazz.colorCode}${teammate.name}",
            Vec3(event.x, event.y + 0.5, event.z), 0.05, depth = false
        )
    }
}