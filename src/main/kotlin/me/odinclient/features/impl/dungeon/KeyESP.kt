package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object KeyESP : Module(
    name = "Key ESP",
    description = "Draws a box around the key",
    category = Category.DUNGEON
) {
    private var currentKey: Pair<Color, Entity>? = null

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityArmorStand || !DungeonUtils.inDungeons) return

        scope.launch(Dispatchers.IO) {
            delay(500)

            val name = event.entity.name.noControlCodes
            if (name == "Wither Key") {
                currentKey = Color(0, 0, 0) to event.entity
            } else if (name == "Blood Key") {
                currentKey = Color(255, 0, 0) to event.entity
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