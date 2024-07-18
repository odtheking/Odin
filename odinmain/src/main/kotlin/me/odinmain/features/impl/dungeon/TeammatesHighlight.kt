package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.addVec
import me.odinmain.utils.distanceSquaredTo
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeammatesHighlight : Module(
    name = "Teammate Highlight",
    category = Category.DUNGEON,
    description = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val mode: Int by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList)
    private val thickness: Float by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode != HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showClass: Boolean by BooleanSetting("Show class", true, description = "Shows the class of the teammate.")
    private val showHighlight: Boolean by BooleanSetting("Show highlight", true, description = "Highlights teammates with an outline.")
    private val showName: Boolean by BooleanSetting("Show name", true, description = "Highlights teammates with a name tag.")
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Highlights teammates only when they are visible.")
    private val inBoss: Boolean by BooleanSetting("In boss", true, description = "Highlights teammates in boss rooms.")

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
    fun onRenderWorld(event: RenderWorldLastEvent) { // make a entity render nametag event and use that instead
        if (!showName || !shouldRender()) return
        dungeonTeammatesNoSelf.forEach { teammate ->
            val entity = teammate.entity ?: return@forEach
            if (entity.distanceSquaredTo(mc.thePlayer) >= 2100) return@forEach
            Renderer.drawStringInWorld(
                if (showClass) "${teammate.name} §e[${teammate.clazz.name[0]}]" else teammate.name,
                teammate.entity.renderVec.addVec(y = 2.6),
                color = teammate.clazz.color,
                depth = depthCheck, scale = 0.05f
            )
        }
    }

    private fun shouldRender(): Boolean {
        return (inBoss || !DungeonUtils.inBoss) // boss
                && DungeonUtils.inDungeons
    }
}