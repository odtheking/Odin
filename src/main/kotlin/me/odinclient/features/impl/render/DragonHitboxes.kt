package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object DragonHitboxes : Module(
    name = "Dragon Hitboxes",
    category = Category.RENDER,
    description = "Renders real dragon hitboxes"
) {

    private val onlyM7: Boolean by BooleanSetting(name = "Only M7")
    private val color: Color by ColorSetting(name = "Hitbox Color", default = Color(0, 255, 255))
    private val lineWidth: Float by NumberSetting(name = "Line Thickness", default = 3f, min = 0f, max = 10f, increment = 0.1f)

    private val entityPositions = mutableMapOf<Int, Array<Double>>()
    private var dragonRenderQueue: List<EntityDragon> = ArrayList()

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent) {
        if (mc.theWorld == null) return
        val entityDragons = mc.theWorld.loadedEntityList.filterIsInstance<EntityDragon>()
        dragonRenderQueue = entityDragons
        if (event.phase == TickEvent.Phase.END) return

        for (dragon in entityDragons) {
            for (entity in dragon.dragonPartArray) {
                val entityId = entity.entityId
                if (!entityPositions.containsKey(entityId)) {
                    entityPositions[entityId] = arrayOf(
                        entity.posX,
                        entity.posY,
                        entity.posZ,
                        entity.posX,
                        entity.posY,
                        entity.posZ
                    )
                }
                entityPositions[entityId]?.apply {
                    this[0] = this[3]
                    this[1] = this[4]
                    this[2] = this[5]
                    this[3] = entity.posX
                    this[4] = entity.posY
                    this[5] = entity.posZ
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (dragonRenderQueue.isEmpty() || (onlyM7 && !DungeonUtils.isFloor(7))) return

        for (dragon in dragonRenderQueue) {
            for (entity in dragon.dragonPartArray) {
                val entityId = entity.entityId
                entityPositions[entityId]?.apply {
                    val lastX = this[0]
                    val lastY = this[1]
                    val lastZ = this[2]
                    val x = this[3]
                    val y = this[4]
                    val z = this[5]

                    val dX = lastX + (x - lastX) * event.partialTicks
                    val dY = lastY + (y - lastY) * event.partialTicks
                    val dZ = lastZ + (z - lastZ) * event.partialTicks
                    val w = entity.width
                    val h = entity.height

                    RenderUtils.drawCustomESPBox(dX - w / 2, dY, dZ - w / 2, w.toDouble(), h.toDouble(), color, lineWidth, phase = true)
                }
            }
        }
    }

}