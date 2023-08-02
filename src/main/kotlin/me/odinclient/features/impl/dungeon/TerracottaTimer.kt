package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.BlockUpdateEvent
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
            terracottaTimers[event.pos] = System.currentTimeMillis() + 1500f
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent)
    {
        for (timer in terracottaTimers)
        {
            val time = (timer.value / 1000f).round(2).toString()
            val vec3 = Vec3(timer.key.x.toDouble(), timer.key.y.toDouble(), timer.key.z.toDouble()).addVector(0.0, 1.5, 0.0)
            val color = Color(1 - timer.value / 1500f, timer.value / 1500f, 0f).rgb
            RenderUtils.drawStringInWorld(time, vec3, color, renderBlackBox = false, increase = false, depthTest = false, scale = 0.016666668f * 1.6f)
        }
    }

}