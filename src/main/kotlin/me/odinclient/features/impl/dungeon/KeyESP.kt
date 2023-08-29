package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KeyESP : Module(
    name = "Key ESP",
    description = "Draws a box around the key",
    category = Category.DUNGEON
) {
    private var currentKey: Pair<Color, Entity>? = null
    private val thickness: Float by NumberSetting("Thickness", 5f, 3f, 20f, .1f)

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityArmorStand || !DungeonUtils.inDungeons) return

        scope.launch(Dispatchers.IO) {
            delay(500)

            val name = event.entity.name.noControlCodes
            if (name == "Wither Key") {
                currentKey = Color.BLACK to event.entity
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
        RenderUtils.drawCustomESPBox(
            pos.xCoord - 0.5, 1.0,
            pos.yCoord + 1.15, 1.0,
            pos.zCoord - 0.5, 1.0,
            color,
            thickness,
            true
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentKey = null
    }
}