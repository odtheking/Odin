package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KeyHighlight : Module(
    name = "Key Highlight",
    description = "Highlights wither and blood keys in dungeons.",
    category = Category.DUNGEON,
) {
    private var currentKey: Pair<Color, Entity>? = null
    private val thickness: Float by NumberSetting("Thickness", 5f, 1f, 20f, .1f, description = "The thickness of the box.")

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId) !is EntityArmorStand || !DungeonUtils.inDungeons || DungeonUtils.inBoss) return
        val entity = mc.theWorld.getEntityByID(event.packet.entityId) as EntityArmorStand
        currentKey = when (entity.name.noControlCodes) {
            "Wither Key" -> Color.BLACK to entity
            "Blood Key" -> Color.RED to entity
            else -> return
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val (color, entity) = currentKey ?: return
        if (entity.isDead) {
            currentKey = null
            return
        }

        val pos = entity.positionVector.addVec(-0.5, 1, -0.5)
        Renderer.drawBox(pos.toAABB(), color, fillAlpha = 0f, outlineWidth = thickness, depth = isLegitVersion)
    }

    init {
        onWorldLoad {
            currentKey = null
        }
    }
}