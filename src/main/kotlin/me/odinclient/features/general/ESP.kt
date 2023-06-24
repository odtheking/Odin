package me.odinclient.features.general

import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.ClientSecondEvent
import me.odinclient.events.RenderEntityModelEvent
import me.odinclient.utils.render.OutlineUtils
import me.odinclient.utils.VecUtils.noSqrt3DDistance
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ESP {

    private inline val espList get() = OdinClient.miscConfig.espList
    
    private var color = config.espColor.toJavaColor()

    private var currentEntities = mutableListOf<Entity>()
    @SubscribeEvent
    fun onSecond(event: ClientSecondEvent) {
        if (!config.esp) return
        currentEntities.removeAll { it.isDead }
        color = config.espColor.toJavaColor()
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

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!config.esp || !currentEntities.contains(event.entity)) return
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
        clear()
    }
    // TODO: make it reload the current entities every 30s or so
    fun clear() {
        currentEntities.clear()
    }
}
