package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.RenderOverlayNoCaching
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeammatesHighlight : Module(
    name = "Teammate Highlight",
    category = Category.DUNGEON,
    description = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, description = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, description = "The line width of Outline / Boxes / 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val showClass by BooleanSetting("Show class", true, description = "Shows the class of the teammate.")
    private val showHighlight by BooleanSetting("Show highlight", true, description = "Highlights teammates with an outline.")
    private val showName by BooleanSetting("Show name", true, description = "Highlights teammates with a name tag.")
    private val nameStyle by SelectorSetting("Name Style", "Plain Text", arrayListOf("Plain Text", "Oringo Style"), description = "The style of the name tag to render.").withDependency { showName }
    private val backgroundColor by ColorSetting("Background Color", default = Color.DARK_GRAY.withAlpha(0.5f), true, description = "The color of the nametag background").withDependency { showName && nameStyle == 1 }
    private val accentColor by ColorSetting("Accent Color", default = Color.BLUE, true, description = "The color of the nametag accent").withDependency { showName && nameStyle == 1 }
    private val padding by NumberSetting("Padding", default = 5, min = 0, max = 20, increment = 1, description = "The padding around the text of the nametag.").withDependency { showName && nameStyle == 1 }
    private val scale by NumberSetting("Scale", default = 0.8f, min = 0, max = 2, increment = 0.1, description = "The scale of the nametag").withDependency { showName && nameStyle ==1 }
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
    fun onRenderEntity(event: RenderLivingEvent.Specials.Pre<EntityOtherPlayerMP>) {
        if (!showName || !shouldRender()) return
        val teammate = dungeonTeammatesNoSelf.find { it.entity == event.entity } ?: return
        event.isCanceled = true
        if (nameStyle != 0) return
        val text = if (showClass) "§${teammate.clazz.colorCode}${teammate.name} §e[${teammate.clazz.name[0]}]" else "§${teammate.clazz.colorCode}${teammate.name}"
        RenderUtils.drawMinecraftLabel(text, Vec3(event.x, event.y + 0.5, event.z), 0.05, false)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderOverlayNoCaching) {
        if (!showName || !shouldRender() || nameStyle == 0) return
        dungeonTeammatesNoSelf.forEach { teammate ->
            teammate.entity?.let { entity ->
                val text = if (showClass) "§${teammate.clazz.colorCode}${teammate.name} §e[${teammate.clazz.name[0]}]" else "§${teammate.clazz.colorCode}${teammate.name}"
                RenderUtils2D.drawBackgroundNameTag(text, entity, padding, backgroundColor, accentColor, scale = scale)
            }
        }
    }

    private fun shouldRender(): Boolean {
        return (inBoss || !DungeonUtils.inBoss) // boss
                && DungeonUtils.inDungeons
    }
}