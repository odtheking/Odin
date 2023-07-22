package me.odinclient.features.impl.general

import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.RenderEntityModelEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.utils.VecUtils.noSqrt3DDistance
import me.odinclient.utils.render.OutlineUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object ESP : Module(
    "ESP",
    category = Category.GENERAL
) {
    private val color: Color by ColorSetting("Color", Color.WHITE)

    private inline val espList get() = OdinClient.miscConfig.espList

    var currentEntities = mutableListOf<Entity>()

    init {
        executor(1000) {
            currentEntities.removeAll { it.isDead }

            mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.forEach { entity ->
                if (
                    !espList.any { entity.name.lowercase().contains(it) } ||
                    currentEntities.contains(entity)
                ) return@forEach

                val entities =
                    mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.expand(1.0, 5.0, 1.0))
                        .filter { it != null && it !is EntityArmorStand && it != mc.thePlayer }
                        .sortedByDescending { noSqrt3DDistance(it, entity) }
                if (entities.isEmpty()) return@forEach
                currentEntities.add(entities.first())
            }
        }

        executor(30000) {
            currentEntities.clear()
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!currentEntities.contains(event.entity)) return
        if (!mc.thePlayer.canEntityBeSeen(event.entity) && !config.espThrough) return
        OutlineUtils.outlineEntity(
            event,
            config.espThickness,
            color,
            config.espCancelHurt
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentEntities.clear()
    }
}
