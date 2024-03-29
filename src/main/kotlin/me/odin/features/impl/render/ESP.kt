package me.odin.features.impl.render

import me.odin.Odin.Companion.mc
import me.odin.Odin.Companion.miscConfig
import me.odin.events.impl.RenderEntityModelEvent
import me.odin.features.Category
import me.odin.features.Module
import me.odin.features.settings.impl.ActionSetting
import me.odin.features.settings.impl.BooleanSetting
import me.odin.features.settings.impl.ColorSetting
import me.odin.features.settings.impl.NumberSetting
import me.odin.utils.VecUtils.noSqrt3DDistance
import me.odin.utils.render.Color
import me.odin.utils.render.world.OutlineUtils
import me.odin.utils.skyblock.ChatUtils.modMessage
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ESP : Module(
    "Highlight",
    category = Category.RENDER,
    tag = TagType.FPSTAX,
    description = "Allows you to highlight selected mobs."

) {
    private val color: Color by ColorSetting("Color", Color(255, 0, 0))
    private val thickness: Float by NumberSetting("Outline Thickness", 5f, 5f, 20f, 0.5f)
    private val cancelHurt: Boolean by BooleanSetting("Cancel Hurt", true)

    private val addStar: () -> Unit by ActionSetting("Add Star") {
        if (miscConfig.espList.contains("✯")) return@ActionSetting
        modMessage("Added ✯ to ESP list")
        miscConfig.espList.add("✯")
        miscConfig.saveAllConfigs()
    }

    private inline val espList get() = miscConfig.espList

    var currentEntities = mutableListOf<Pair<Entity, Boolean>>()

    init {
        execute(1000) {
            currentEntities.removeAll { it.first.isDead }
            currentEntities = currentEntities.map { Pair(it.first, mc.thePlayer.canEntityBeSeen(it.first)) }.toMutableList()

            mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.forEach { entity ->
                if (
                    !espList.any { entity.name.lowercase().contains(it) } ||
                    currentEntities.any {it.first == entity}
                ) return@forEach

                val entities =
                    mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.expand(1.0, 5.0, 1.0))
                        .filter { it != null && it !is EntityArmorStand && it != mc.thePlayer }
                        .sortedByDescending { noSqrt3DDistance(it, entity) }
                if (entities.isEmpty()) return@forEach
                currentEntities.add(Pair(entities.first(), mc.thePlayer.canEntityBeSeen(entities.first())))
            }
        }

        execute(30000) {
            currentEntities.clear()
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!currentEntities.any { it.first == event.entity }) return
        if (!mc.thePlayer.canEntityBeSeen(event.entity)) return

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
