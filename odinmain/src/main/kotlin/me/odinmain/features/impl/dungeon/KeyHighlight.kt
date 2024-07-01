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
    private val thickness: Float by NumberSetting("Thickness", 5f, 1f, 20f, .1f, description = "The thickness of the box.")
    data class KeyInfo(val color: Color, val entity: Entity)
    private var currentKey: KeyInfo? = null

    init {
        onWorldLoad {
            currentKey = null
        }
    }

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        val entity = mc.theWorld.getEntityByID(event.packet.entityId) as? EntityArmorStand ?: return
        if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return

        currentKey = when (entity.name.noControlCodes) {
            "Wither Key" -> KeyInfo(Color.BLACK, entity)
            "Blood Key" -> KeyInfo(Color.RED, entity)
            else -> return
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        currentKey?.let { (color, entity) ->
            if (entity.isDead) {
                currentKey = null
                return
            }

            Renderer.drawBox(entity.positionVector.addVec(-0.5, 1, -0.5).toAABB(), color, fillAlpha = 0f, outlineWidth = thickness, depth = isLegitVersion)
        }
    }
}