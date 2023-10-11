package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.utils.Utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KeyHighlight  {

    var currentKey: Pair<Color, Entity>? = null

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
}