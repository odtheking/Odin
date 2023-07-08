package me.odinclient.features.dungeon

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.config
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.render.RenderUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object KeyESP {
    private var currentKey: Pair<Color, Entity>? = null

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityArmorStand || !DungeonUtils.inDungeons || !config.keyESP) return

        GlobalScope.launch {
            delay(500)
            val name = event.entity.name.noControlCodes
            if (name == "Wither Key") {
                currentKey = Pair(Color(0, 0, 0), event.entity)
            } else if (name == "Blood Key") {
                currentKey = Pair(Color(255, 0, 0), event.entity)
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (currentKey == null) return
        val (color, entity) = currentKey!!
        if (entity.isDead) {
            currentKey = null
            return
        }
        val pos = entity.positionVector
        RenderUtils.drawCustomEspBox(
            pos.xCoord - 0.5, 1.0,
            pos.yCoord + 1.15, 1.0,
            pos.zCoord - 0.5, 1.0,
            color,
            5f,
            true
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentKey = null
    }
}