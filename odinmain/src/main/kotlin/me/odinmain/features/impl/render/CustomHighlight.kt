package me.odinmain.features.impl.render

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.isOtherPlayer
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand

object CustomHighlight : Module(
    name = "Custom Highlight",
    category = Category.RENDER,
    tag = TagType.FPSTAX,
    description = "Allows you to highlight selected mobs. (/highlight)"
) {
    private val starredMobESP by BooleanSetting("Starred Mob Highlight", true, description = "Highlights mobs with a star in their name.")
    private val shadowAssassin by BooleanSetting("Shadow Assassin", false, description = "Highlights Shadow Assassins.").withDependency { !isLegitVersion }
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, description = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)

    private val color by ColorSetting("Color", Color.WHITE.withAlpha(0.75f), true, description = "The color of the highlight.")
    private val thickness by NumberSetting("Line Width", 2f, 1f, 6f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val scanDelay by NumberSetting("Scan Delay", 100L, 20L, 2000L, 20L, description = "The delay between entity scans.", unit = "ms")

    private val xray by BooleanSetting("Depth Check", false, description = "Highlights entities through walls.").withDependency { !isLegitVersion }
    private val showInvisible by BooleanSetting("Show Invisible", false, description = "Highlights invisible entities.").withDependency { !isLegitVersion }

    val highlightList: MutableList<String> by ListSetting("List", mutableListOf())
    private val depthCheck get() = if (isLegitVersion) true else xray
    private var currentEntities = mutableSetOf<Entity>()

    init {
        execute({ scanDelay }) {
            currentEntities.clear()
            getEntities()
        }

        onWorldLoad { currentEntities.clear() }

        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else currentEntities.map { HighlightRenderer.HighlightEntity(it, color, thickness, depthCheck, style) }
        }
    }

    private fun getEntities() {
        if (highlightList.isEmpty() && ((!starredMobESP && !shadowAssassin) || !DungeonUtils.inDungeons)) return
        mc.theWorld?.loadedEntityList?.forEach { entity ->
            checkEntity(entity)
            if (starredMobESP) checkStarred(entity)
            if (shadowAssassin && !isLegitVersion) checkAssassin(entity)
            if (showInvisible && entity.isInvisible && !isLegitVersion && entity in currentEntities) entity.isInvisible = false
        }
    }

    private fun checkEntity(entity: Entity) {
        if (entity !is EntityArmorStand || highlightList.none { entity.name.contains(it, true) } || entity in currentEntities || !entity.alwaysRenderNameTag && !depthCheck) return
        currentEntities.add(getMobEntity(entity) ?: return)
    }

    private fun checkStarred(entity: Entity) {
        if (entity !is EntityArmorStand || !entity.name.startsWith("§6✯ ") || !entity.name.endsWith("§c❤") || entity in currentEntities || (!entity.alwaysRenderNameTag && depthCheck)) return
        currentEntities.add(getMobEntity(entity) ?: return)
    }

    private fun checkAssassin(entity: Entity) {
        if (entity !is EntityOtherPlayerMP || entity.name != "Shadow Assassin") return
        currentEntities.add(entity)
    }

    private fun getMobEntity(entity: Entity): Entity? {
        return mc.theWorld?.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
            ?.filter { it !is EntityArmorStand && mc.thePlayer != it && !(it is EntityWither && it.isInvisible) && !(it is EntityOtherPlayerMP && it.isOtherPlayer()) }
            ?.minByOrNull { entity.getDistanceToEntity(it) }
    }
}