package me.odinclient.features.impl.floor7.p3

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.PostEntityMetadata
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object ArrowAlign : Module(
    name = "Arrow Align",
    description = "Different features for the arrow alignment device",
    category = Category.FLOOR7
) {
    private val solver: Boolean by BooleanSetting("Solver")
    private val triggerBot: Boolean by BooleanSetting("Trigger Bot")

    private val area = BlockPos.getAllInBox(BlockPos(-2, 125, 79), BlockPos(-2, 121, 75))
        .toList().sortedWith { a, b ->
            if (a.y == b.y) return@sortedWith b.z - a.z
            if (a.y < b.y) return@sortedWith 1
            if (a.y > b.y) return@sortedWith -1
            return@sortedWith 0
        }
                                        //     x,   y,         entity,          needed clicks        (x is technically z in the world)
    private val neededRotations = HashMap<Pair<Int, Int>, Int>()

    init {
        execute(3000) {
            if (mc.thePlayer.getDistanceSq(BlockPos(-2, 122, 76)) > 225 /*|| !DungeonUtils.inBoss || !DungeonUtils.isFloor(7)*/) return@execute
            calculate()
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId)?.position !in area) return
        calculate()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        for (place in neededRotations) {
            val clicksNeeded = place.value
            val color = when {
                clicksNeeded == 0 -> continue
                clicksNeeded < 3 -> Color(85, 255, 85).rgba
                clicksNeeded < 5 -> Color(255, 170, 0).rgba
                else -> Color(170, 0, 0).rgba
            }
            RenderUtils.drawStringInWorld(
                clicksNeeded.toString(),
                Vec3(-1.8, 124.6 - place.key.second.toDouble(), 79.5 - place.key.first.toDouble()),
                color,
                renderBlackBox = false,
                increase = false,
                scale = .02f
            )
        }
    }

    private fun calculate() {
        val frames = mc.theWorld.getEntities(EntityItemFrame::class.java) {
            it != null && area.contains(it.position) && it.displayedItem != null
        }
        if (frames.isEmpty()) return
        val solutions = HashMap<Pair<Int, Int>, Int>()
        val maze = Array(5) { IntArray(5) }
        val queue = LinkedList<Pair<Int, Int>>()
        val visited = Array(5) { BooleanArray(5) }
        neededRotations.clear()
        area.forEachIndexed { i, pos ->
            val x = i % 5
            val y = i / 5
            val frame = frames.find { it.position == pos } ?: return@forEachIndexed
            // 0 = null, 1 = arrow, 2 = end, 3 = start
            maze[x][y] = when (frame.displayedItem.item) {
                Items.arrow -> 1
                Item.getItemFromBlock(Blocks.wool) -> {
                    when (frame.displayedItem.itemDamage) {
                        5 -> 3
                        14 -> 2
                        else -> 0
                    }
                }
                else -> 0
            }
            when (maze[x][y]) {
                1 -> neededRotations[Pair(x, y)] = frame.rotation
                3 -> queue.add(Pair(x, y))
            }
        }
        while (queue.size != 0) {
            val s = queue.poll()
            val directions = arrayOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1))
            for (i in 3 downTo 0) {
                val x = (s.first + directions[i].first)
                val y = (s.second + directions[i].second)
                if (x !in 0..4 || y !in 0..4) continue
                val rotations = i * 2 + 1
                if (solutions[Pair(x, y)] != null || maze[x][y] !in 1..2) continue
                queue.add(Pair(x, y))
                solutions[s] = rotations
                if (visited[s.first][s.second]) continue
                var neededRotation = neededRotations[s] ?: continue
                neededRotation = rotations - neededRotation
                if (neededRotation < 0) neededRotation += 8
                neededRotations[s] = neededRotation
                visited[s.first][s.second] = true
            }
        }
    }
}