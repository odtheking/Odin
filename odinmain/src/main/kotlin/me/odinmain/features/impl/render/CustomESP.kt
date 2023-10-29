package me.odinmain.features.impl.render

import me.odinmain.OdinMain
import me.odinmain.config.MiscConfig
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.VecUtils.noSqrt3DDistance
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.OutlineUtils
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderX
import me.odinmain.utils.render.world.RenderUtils.renderY
import me.odinmain.utils.render.world.RenderUtils.renderZ
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CustomESP : Module(
    "Custom ESP",
    category = Category.RENDER,
    tag = TagType.FPSTAX,
    description = "Allows you to highlight selected mobs (/esp)"
) {

    private val scanDelay: Long by NumberSetting("Scan Delay", 500L, 100L, 2000L, 100L)
    val color: Color by ColorSetting("Color", Color(255, 0, 0), true)
    val mode: Int by SelectorSetting("Mode", "Outline", arrayListOf("Outline", "Overlay", "Boxes"))
    private val xray: Boolean by BooleanSetting("Through Walls", true).withDependency { !OdinMain.onLegitVersion }
    val renderThrough: Boolean get() = if (OdinMain.onLegitVersion) false else xray
    private val thickness: Float by NumberSetting("Outline Thickness", 5f, 5f, 20f, 0.5f).withDependency { mode != 1 }
    private val cancelHurt: Boolean by BooleanSetting("Cancel Hurt", true).withDependency { mode != 1 }

    private val addStar: () -> Unit by ActionSetting("Add Star") {
        if (MiscConfig.espList.contains("✯")) return@ActionSetting
        modMessage("Added ✯ to ESP list")
        MiscConfig.espList.add("✯")
        MiscConfig.saveAllConfigs()
    }

    private inline val espList get() = MiscConfig.espList

    var currentEntities = mutableSetOf<Entity>()

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
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (mode != 0) return
        if (event.entity !in currentEntities) return
        if (!mc.thePlayer.canEntityBeSeen(event.entity) && !renderThrough) return

        OutlineUtils.outlineEntity(
            event,
            thickness,
            color,
            cancelHurt
        )
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (mode != 2) return
        currentEntities.forEach {
            RenderUtils.drawCustomESPBox(
                it.renderX - it.width / 2, it.width.toDouble(),
                it.renderY, it.height.toDouble(),
                it.renderZ - it.width / 2, it.width.toDouble(),
                color,
                thickness / 5f,
                renderThrough
            )
        }
    }

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        checkEntity(mc.theWorld.getEntityByID(event.packet.entityId) ?: return)
    }

    private fun getEntities() {
        mc.theWorld?.loadedEntityList?.forEach(::checkEntity)
    }

    private fun checkEntity(entity: Entity) {
        if (entity !is EntityArmorStand || espList.none { entity.name.contains(it, true) } || entity in currentEntities) return
        currentEntities.add(
            mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.expand(1.0, 5.0, 1.0))
                .filter { it != null && it !is EntityArmorStand && it != mc.thePlayer }
                .minByOrNull { noSqrt3DDistance(it, entity) }
                .takeIf { it !is EntityWither || DungeonUtils.inBoss } ?: return
        )
    }
}