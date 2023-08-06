package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.events.RenderEntityModelEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.VecUtils.noSqrt3DDistance
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.OutlineUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ESP : Module(
    "ESP",
    category = Category.GENERAL
) {
    private val color: Color by ColorSetting("Color", Color(255, 0, 0))
    private val through: Boolean by BooleanSetting("Through Walls", true)
    private val thickness: Float by NumberSetting("Outline Thickness", 5f, 5f, 20f, 0.5f)
    private val cancelHurt: Boolean by BooleanSetting("Cancel Hurt", true)

    private val addStar: () -> Unit by ActionSetting("Add Star") {
        if (miscConfig.espList.contains("✯")) return@ActionSetting
        modMessage("Added ✯ to ESP list")
        miscConfig.espList.add("✯")
        miscConfig.saveAllConfigs()
    }

    private inline val espList get() = miscConfig.espList

    var currentEntities = mutableListOf<Entity>()

    init {
        execute(1000) {
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

        execute(30000) {
            currentEntities.clear()
        }
    }

    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!currentEntities.contains(event.entity)) return
        if (!mc.thePlayer.canEntityBeSeen(event.entity) && !through) return
        OutlineUtils.outlineEntity(
            event,
            thickness,
            color,
            cancelHurt
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentEntities.clear()
    }
}
