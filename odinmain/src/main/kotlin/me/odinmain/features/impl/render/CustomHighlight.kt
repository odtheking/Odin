package me.odinmain.features.impl.render

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.ServerUtils.getPing
import me.odinmain.utils.render.*
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.RenderUtils.renderVec
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object CustomHighlight : Module(
    name = "Custom Highlight",
    category = Category.RENDER,
    tag = TagType.FPSTAX,
    description = "Allows you to highlight selected mobs. (/highlight)"
) {
    private val scanDelay: Long by NumberSetting("Scan Delay", 500L, 10L, 2000L, 100L)
    private val starredMobESP: Boolean by BooleanSetting("Starred Mob Highlight", true, description = "Highlights mobs with a star in their name (remove star from the separate list).")
    private val shadowAssasin: Boolean by BooleanSetting("Shadow Assassin", false, description = "Highlights Shadow Assassins")
    private val color: Color by ColorSetting("Color", Color.WHITE, true)
    //private val mode: Int by SelectorSetting("Mode", HighlightRenderer.highlightModeDefault, HighlightRenderer.highlightModeList)
    val mode: Int by SelectorSetting("Mode", "Outline", arrayListOf("Outline", "Boxes", "2D"))
    private val thickness: Float by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline/ Boxes/ 2D Boxes").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    //private val glowIntensity: Float by NumberSetting("Glow Intensity", 2f, .5f, 5f, .1f, description = "The intensity of the glow effect.").withDependency { mode == HighlightRenderer.HighlightType.Glow.ordinal }
    private val tracerLimit: Int by NumberSetting("Tracer Limit", 0, 0, 15, description = "Highlight will draw tracer to all mobs when you have under this amount of mobs marked, set to 0 to disable. Helpful for finding lost mobs.").withDependency { !isLegitVersion }

    private val xray: Boolean by BooleanSetting("Depth Check", false).withDependency { !isLegitVersion }
    val highlightList: MutableList<String> by ListSetting("List", mutableListOf())
    private val depthCheck: Boolean get() = if (isLegitVersion) true else xray
    private var currentEntities = mutableSetOf<Entity>()

    init {
        execute({ scanDelay }) {
            currentEntities.removeAll { it.isDead }
            getEntities()
        }

        execute(30_000) {
            currentEntities.clear()
            getEntities()
        }

        onWorldLoad { currentEntities.clear() }

        /*HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else currentEntities.map { HighlightRenderer.HighlightEntity(it, color, thickness, !renderThrough, glowIntensity) }
        }*/
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (mode != 0 || event.entity !in currentEntities || (depthCheck && !mc.thePlayer.canEntityBeSeen(event.entity))) return
        profile("Outline Esp") { OutlineUtils.outlineEntity(event, thickness, color, true) }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!mode.equalsOneOf(1,2) && tracerLimit == 0) return
        profile("ESP") { currentEntities.forEach {
            if (currentEntities.size < tracerLimit && !isLegitVersion)
                RenderUtils.draw3DLine(getPositionEyes(mc.thePlayer.renderVec), getPositionEyes(it.renderVec), color, 2f, false)

            if (mode == 1)
                Renderer.drawBox(it.renderBoundingBox, color, thickness, depth = !depthCheck, fillAlpha = 0)
            else if (mode == 2 && (mc.thePlayer.canEntityBeSeen(it) || depthCheck))
                Renderer.draw2DEntity(it, thickness, color)
        }}
    }

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        val entity = mc.theWorld.getEntityByID(event.packet.entityId) ?: return
        checkEntity(entity)
        if (starredMobESP) checkStarred(entity)
        if (shadowAssasin) checkAssassin(entity)
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
        if (entity !is EntityArmorStand || !entity.name.startsWith("§6✯ ") || !entity.name.endsWith("§c❤") || entity in currentEntities || !entity.alwaysRenderNameTag && !depthCheck) return
        currentEntities.add(getMobEntity(entity) ?: return)
    }

    private fun checkAssassin(entity: Entity) {
        if (entity !is EntityOtherPlayerMP || entity.name != "Shadow Assassin") return
        currentEntities.add(entity)
    }

    private fun getMobEntity(entity: Entity): Entity? {
        return mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
            .filter { it != null && it !is EntityArmorStand && it.getPing() != 1 && it != mc.thePlayer}
            .minByOrNull { entity.getDistanceToEntity(it) }
            .takeIf { !(it is EntityWither && it.isInvisible) }
    }
}