package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object TerracottaTimer : Module(
    name = "Terracotta Timer",
    description = "Displays the time until the terracotta spawns.",
    category = Category.DUNGEON
) {
    private data class Terracotta(val pos: Vec3, var time: Double)
    private var terrasSpawning = mutableListOf<Terracotta>()

    @SubscribeEvent
    fun onBlockPacket(event: BlockChangeEvent) {
        if (!DungeonUtils.isFloor(6) || !DungeonUtils.inBoss || !event.update.block.isFlowerPot) return
        terrasSpawning.add(
            Terracotta(
                Vec3(event.pos).addVec(.5, 1.5, .5),
                if (LocationUtils.currentDungeon?.floor?.isInMM == true) 1200.0 else 1500.0
            )
        )
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        terrasSpawning.removeAll {
            it.time -= 5
            it.time <= 0
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val iterator = terrasSpawning.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            RenderUtils.drawStringInWorld(
                "${String.format(Locale.US, "%.2f",it.time / 100.0)}s",
                it.pos,
                getColor(it.time / 100.0).rgba,
                renderBlackBox = false,
                increase = false,
                depthTest = false,
                .03f
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

    fun onChat(event: ChatPacketEvent) {
        if (!event.message.startsWith("[BOSS] Sadan: ENOUGH!")) return
        terrasSpawning.removeAll {
            it.time > 0
        }
    }

}