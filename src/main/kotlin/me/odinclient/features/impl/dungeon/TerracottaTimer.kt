package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.BlockUpdateEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.round
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object TerracottaTimer: Module(
    name = "Terracotta Timer",
    category = Category.DUNGEON
) {

    private var terracottaTimers: HashMap<BlockPos, Float> = HashMap()

    @SubscribeEvent
    fun onBlockUpdate(event: BlockUpdateEvent)
    {
        if (DungeonUtils.isFloor(6) && DungeonUtils.inBoss && mc.theWorld.getBlockState(event.pos) == Blocks.air && event.state.block == Blocks.air)
        {
            terracottaTimers[event.pos] = System.currentTimeMillis() + 15000f
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent)
    {
        terracottaTimers.entries.removeAll {
            val time = it.value - System.currentTimeMillis()
            val vec3 = Vec3(it.key.x.toDouble(), it.key.y.toDouble(), it.key.z.toDouble()).addVector(0.0, 1.5, 0.0)
            val color = Color(1 - it.value / 15000f, it.value / 15000f, 0f).rgb
            RenderUtils.drawStringInWorld((time / 1000f).round(2).toString(), vec3, color, renderBlackBox = false, increase = false, depthTest = false, scale = 0.016666668f * 1.6f)
            return@removeAll time <= 0
        }
    }

}