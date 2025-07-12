package me.odinmain.features.impl.nether

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySkeleton

object BlazeAttunement : Module(
    name = "Blaze Attunement",
    description = "Displays what attunement a blaze boss currently requires."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }

    private var currentBlazes = hashMapOf<Entity, Color>()

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else currentBlazes.map { HighlightRenderer.HighlightEntity(it.key, it.value, thickness, true, style) }
        }

        execute(250) {
            currentBlazes.clear()
            mc.theWorld?.loadedEntityList?.forEach { entity ->
                if (entity !is EntityArmorStand || currentBlazes.any { it.key == entity }) return@forEach
                val name = entity.name.noControlCodes

                val color = when {
                    name.contains("CRYSTAL ♨") -> Colors.MINECRAFT_AQUA
                    name.contains("ASHEN ♨") -> Colors.MINECRAFT_GRAY
                    name.contains("AURIC ♨") -> Colors.MINECRAFT_YELLOW
                    name.contains("SPIRIT ♨") -> Colors.WHITE
                    else -> return@forEach
                }.withAlpha(.4f)

                currentBlazes[mc.theWorld?.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
                    ?.filter { it is EntityBlaze || it is EntitySkeleton || it is EntityPigZombie }
                    ?.sortedByDescending { it.positionVector.squareDistanceTo(entity.positionVector) }
                    ?.takeIf { it.isNotEmpty() }?.firstOrNull() ?: return@execute] = color
            }
        }
    }
}
