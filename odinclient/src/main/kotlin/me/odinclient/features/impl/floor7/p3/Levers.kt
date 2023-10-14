package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.block.BlockLever
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Levers : Module(
    name = "Levers Triggerbot",
    description = "Triggerbot for the levers device.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val delay: Long by NumberSetting<Long>("Delay", 200, 70, 500)
    private val triggerBotClock = Clock(delay)

    private val levers = setOf(
        BlockPos(58, 136, 142),
        BlockPos(58, 133, 142),
        BlockPos(60, 135, 142),
        BlockPos(60, 134, 142),
        BlockPos(62, 136, 142),
        BlockPos(62, 133, 142),
    )

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!triggerBotClock.hasTimePassed(delay) || DungeonUtils.getPhase() != 3) return
        val pos = mc.objectMouseOver?.blockPos ?: return
        if (pos !in levers || mc.theWorld.getBlockState(pos).getValue(BlockLever.POWERED)) return
        rightClick()
        triggerBotClock.update()
    }
}