package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KeyHighlight : Module(
    name = "Key Esp",
    description = "Draws a box around the key.",
    category = Category.DUNGEON,
) {
    private var currentKey: Pair<Color, Entity>? = null
    private val thickness: Float by NumberSetting("Thickness", 5f, 3f, 20f, .1f)
    private val renderThrough: Boolean get() = !OdinMain.onLegitVersion

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId) !is EntityArmorStand || !DungeonUtils.inDungeons || DungeonUtils.inBoss) return

        val entity = mc.theWorld.getEntityByID(event.packet.entityId) as EntityArmorStand
        val name = entity.name.noControlCodes
        if (name == "Wither Key") {
            currentKey = Color.BLACK to entity
        } else if (name == "Blood Key") {
            currentKey = Color(255, 0, 0) to entity
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val (color, entity) = currentKey ?: return
        if (entity.isDead) {
            currentKey = null
            return
        }

        val pos = entity.positionVector
        RenderUtils.drawCustomESPBox(
            pos.xCoord - 0.5, 1.0,
            pos.yCoord + 1.15, 1.0,
            pos.zCoord - 0.5, 1.0,
            color,
            thickness,
            renderThrough
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentKey = null
    }
}