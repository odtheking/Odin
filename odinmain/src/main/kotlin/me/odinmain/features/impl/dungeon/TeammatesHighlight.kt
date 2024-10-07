package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeammatesHighlight : Module(
    name = "Teammate Highlight",
    category = Category.DUNGEON,
    description = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, description = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes / 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showClass by BooleanSetting("Show class", true, description = "Shows the class of the teammate.")
    private val showHighlight by BooleanSetting("Show highlight", true, description = "Highlights teammates with an outline.")
    private val showName by BooleanSetting("Show name", true, description = "Highlights teammates with a name tag.")
    private val depthCheck by BooleanSetting("Depth check", false, description = "Highlights teammates only when they are visible.")
    private val inBoss by BooleanSetting("In boss", true, description = "Highlights teammates in boss rooms.")

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || !shouldRender() || !showHighlight) emptyList()
            else {
                dungeonTeammatesNoSelf.mapNotNull {
                    it.entity?.let { entity -> HighlightRenderer.HighlightEntity(entity, it.clazz.color, thickness, depthCheck, style) }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderEntity(event: RenderLivingEvent.Specials.Pre<EntityLivingBase>) {
        if (!showName || !shouldRender()) return
        val teammate = dungeonTeammatesNoSelf.find { it.entity == event.entity } ?: return
        val text = if (showClass) "§${teammate.clazz.colorCode}${teammate.name} §e[${teammate.clazz.name[0]}]" else "§${teammate.clazz.colorCode}${teammate.name}"
        RenderUtils.drawMinecraftLabel(event.entity, text, event.x, event.y + 0.5, event.z, 0.05)
        event.isCanceled = true
    }

    private fun shouldRender(): Boolean {
        return (inBoss || !DungeonUtils.inBoss) // boss
                && DungeonUtils.inDungeons
    }
}