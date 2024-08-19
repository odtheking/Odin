package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
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
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val witherColor: Color by ColorSetting("Wither Color", Color.BLACK.withAlpha(0.8f), allowAlpha = true, description = "The color of the box.")
    private val bloodColor: Color by ColorSetting("Blood Color", Color.RED.withAlpha(0.8f), allowAlpha = true, description = "The color of the box.")
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private data class KeyInfo(val entity: Entity, val color: Color)
    private var currentKey: KeyInfo? = null

    init {
        onWorldLoad {
            currentKey = null
        }
    }

    @SubscribeEvent
    fun postMetadata(event: PostEntityMetadata) {
        if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return
        val entity = mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityArmorStand ?: return

        currentKey = when (entity.name.noControlCodes) {
            "Wither Key" -> KeyInfo(entity, witherColor)
            "Blood Key" -> KeyInfo(entity, bloodColor)
            else -> return
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        currentKey?.let { (entity, color) ->
            if (entity.isDead) {
                currentKey = null
                return
            }
            Renderer.drawStyledBox(entity.positionVector.addVec(-0.5, 1, -0.5).toAABB(), color, style, lineWidth, isLegitVersion)
        }
    }
}