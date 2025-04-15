package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils.alert
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toAABB
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KeyHighlight : Module(
    name = "Key Highlight",
    desc = "Highlights wither and blood keys in dungeons."
) {
    private val announceKeySpawn by BooleanSetting("Announce Key Spawn", true, desc = "Announces when a key is spawned.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION)
    private val witherColor by ColorSetting("Wither Color", Colors.BLACK.withAlpha(0.8f), allowAlpha = true, desc = "The color of the box.")
    private val bloodColor by ColorSetting("Blood Color", Colors.MINECRAFT_RED.withAlpha(0.8f), allowAlpha = true, desc = "The color of the box.")
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.")
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
        if (currentKey?.entity == entity) return

        currentKey = when (entity.name.noControlCodes) {
            "Wither Key" -> KeyInfo(entity, witherColor)
            "Blood Key" -> KeyInfo(entity, bloodColor)
            else -> return
        }
        if (announceKeySpawn) alert("${entity.name}§7 spawned!")
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