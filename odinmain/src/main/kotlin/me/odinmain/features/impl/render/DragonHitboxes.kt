package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object DragonHitboxes : Module(
    name = "Dragon Hitboxes",
    category = Category.RENDER,
    description = "Draws hitboxes around dragons."
) {
    private val onlyM7 by BooleanSetting(name = "Only M7", default = true, description = "Only render hitboxes in floor 7.")
    private val color by ColorSetting(name = "Hitbox Color", default = Color(0, 255, 255), description = "The color of the hitboxes.")
    private val lineWidth by NumberSetting(name = "Line Thickness", default = 3f, min = 0f, max = 10f, increment = 0.1f, description = "The thickness of the lines.")

    private val entityPositions = mutableMapOf<Int, DoubleArray>()
    private var dragonRenderQueue: List<EntityDragon> = emptyList()

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) return
        val entityDragons = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityDragon>() ?: return
        dragonRenderQueue = entityDragons

        for (dragon in entityDragons) {
            for (entity in dragon.dragonPartArray) {
                val positions = entityPositions.computeIfAbsent(entity.entityId) { DoubleArray(6) { entity.posX } }
                positions[0] = positions[3]
                positions[1] = positions[4]
                positions[2] = positions[5]
                positions[3] = entity.posX
                positions[4] = entity.posY
                positions[5] = entity.posZ
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (dragonRenderQueue.isEmpty() || (onlyM7 && !DungeonUtils.isFloor(7))) return

        for (dragon in dragonRenderQueue) {
            if (dragon.health.toInt() == 0 || dragon.entityId == PersonalDragon.dragon?.entityId) continue
            for (entity in dragon.dragonPartArray) {
                val positions = entityPositions[entity.entityId] ?: continue
                val lastX = positions[0]
                val lastY = positions[1]
                val lastZ = positions[2]

                val dX = lastX + (positions[3] - lastX) * event.partialTicks
                val dY = lastY + (positions[4] - lastY) * event.partialTicks
                val dZ = lastZ + (positions[5] - lastZ) * event.partialTicks

                Renderer.drawBox(AxisAlignedBB(dX - entity.width / 2, dY, dZ - entity.width / 2, dX + entity.width / 2, dY + entity.height, dZ + entity.width / 2), color, lineWidth, depth = true, fillAlpha = 0)
            }
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        entityPositions.clear()
        dragonRenderQueue = emptyList()
    }
}