package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object TerracottaTimer : Module(
    name = "Terracotta Timer",
    description = "Displays the time until the terracotta respawns.",
    category = Category.DUNGEON
) {
    private data class Terracotta(val pos: Vec3, var time: Double)
    private var terracottaSpawning = mutableListOf<Terracotta>()

    @SubscribeEvent
    fun onBlockPacket(event: BlockChangeEvent) {
        if (!DungeonUtils.isFloor(6) || !DungeonUtils.inBoss || !event.update.block.isFlowerPot || terracottaSpawning.any { it.pos.equal(event.pos.toVec3()) }) return
        terracottaSpawning.add(Terracotta(Vec3(event.pos).addVec(0.5, 1.5, 0.5), if (DungeonUtils.floor.isInMM) 1200.0 else 1500.0))
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        terracottaSpawning.removeAll {
            it.time -= 5
            it.time <= 0
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.isFloor(6) || !DungeonUtils.inBoss || terracottaSpawning.isEmpty()) return
        val terracottaList = terracottaSpawning.toList()
        terracottaList.forEach {
            Renderer.drawStringInWorld(
                "${String.format(Locale.US, "%.2f", it.time / 100.0)}s",
                it.pos, getColor(it.time / 100.0), depth = false, scale = 0.03f
            )
        }
    }

    private fun getColor(time: Double): Color {
        return when {
            time > 5.0 -> Color(0, 170, 0)
            time > 2.0 -> Color(255, 170, 0)
            else -> Color(170, 0, 0)
        }
    }
}