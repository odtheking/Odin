package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.events.impl.PostEntityMetadata
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KeyESP : Module(
    name = "Key ESP",
    description = "Draws a box around the key.",
    category = Category.DUNGEON,
) {
    private var currentKey: Pair<Color, Entity>? = null
    private val thickness: Float by NumberSetting("Thickness", 5f, 3f, 20f, .1f)

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
            true
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentKey = null
    }
}