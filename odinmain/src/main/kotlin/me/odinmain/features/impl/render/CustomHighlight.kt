package me.odinmain.features.impl.render

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.ServerUtils.getPing
import me.odinmain.utils.render.*
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CustomHighlight : Module(
    name = "Custom Highlight",
    category = Category.RENDER,
    tag = TagType.FPSTAX,
    description = "Allows you to highlight selected mobs. (/highlight)"
) {
    private val starredMobESP: Boolean by BooleanSetting("Starred Mob Highlight", true, description = "Highlights mobs with a star in their name (remove star from the separate list).")
    private val shadowAssasin: Boolean by BooleanSetting("Shadow Assassin", false, description = "Highlights Shadow Assassins").withDependency { !isLegitVersion }
    private val mode: Int by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList)

    private val color: Color by ColorSetting("Color", Color.WHITE, true)
    private val thickness: Float by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode != HighlightRenderer.HighlightType.Boxes.ordinal }
    private val scanDelay: Long by NumberSetting("Scan Delay", 500L, 10L, 2000L, 100L)

    private val xray: Boolean by BooleanSetting("Depth Check", false).withDependency { !isLegitVersion }
    private val showInvisible: Boolean by BooleanSetting("Show Invisible", false).withDependency { !isLegitVersion }

    val highlightList: MutableList<String> by ListSetting("List", mutableListOf())
    private val depthCheck: Boolean get() = if (isLegitVersion) true else xray
    private var currentEntities = mutableSetOf<Entity>()

    init {
        execute({ scanDelay }) {
            currentEntities.removeAll { mc.theWorld?.getEntityByID(it.entityId) == null }
            getEntities()
        }

        onWorldLoad { currentEntities.clear() }

        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else currentEntities.map { HighlightRenderer.HighlightEntity(it, color, thickness, depthCheck, style) }
        }
    }

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        val entity = mc.theWorld?.getEntityByID(event.packet.entityId) ?: return
        checkEntity(entity)
        if (starredMobESP) checkStarred(entity)
        if (shadowAssasin && isLegitVersion) checkAssassin(entity)
        if (showInvisible && entity.isInvisible && isLegitVersion && entity !in currentEntities) entity.isInvisible = false
    }

    private fun getEntities() {
        mc.theWorld?.loadedEntityList?.forEach {
            checkEntity(it)
            if (starredMobESP) checkStarred(it)
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
            ?.filter { it != null && it !is EntityArmorStand && it.getPing() != 1 && it != mc.thePlayer && !(it is EntityWither && it.isInvisible)}
            ?.minByOrNull { entity.getDistanceToEntity(it) }
    }
}