package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.block.BlockLever
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LightsDevice : Module(
    name = "Lights Device",
    description = "Features to help with the lights device.",
    category = Category.FLOOR7
) {
    private val triggerBot by BooleanSetting("Triggerbot", false, description = "Toggles correct levers automatically when you look at them.")
    private val delay by NumberSetting("Delay", 200L, 70, 500, unit = "ms", description = "The delay between each click.").withDependency { triggerBot }
    val bigLevers by BooleanSetting("Big Levers", false, description = "Makes the levers you want to toggle a 1x1x1 hitbox so they are easier to hit.")
    private val triggerBotClock = Clock(delay)

    val levers = setOf(
        BlockPos(58, 136, 142),
        BlockPos(58, 133, 142),
        BlockPos(60, 135, 142),
        BlockPos(60, 134, 142),
        BlockPos(62, 136, 142),
        BlockPos(62, 133, 142),
    )

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!triggerBotClock.hasTimePassed(delay) || DungeonUtils.getF7Phase() != M7Phases.P3 || !triggerBot) return
        val pos = mc.objectMouseOver?.blockPos ?: return
        if (pos !in levers || mc.theWorld.getBlockState(pos).getValue(BlockLever.POWERED)) return
        rightClick()
        triggerBotClock.update()
    }
}