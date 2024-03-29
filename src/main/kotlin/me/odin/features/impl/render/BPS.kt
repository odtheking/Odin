package me.odin.features.impl.render

import me.odin.features.Category
import me.odin.features.Module
import me.odin.utils.Utils.round
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

// TODO: Make this do something
object BPS : Module(
    name = "BPS",
    category = Category.RENDER
) {

    private var startTime: Long = 0
    private var isBreaking: Boolean = false
    private var blocksBroken: Int = 0
    private var lastBrokenBlock: Long = 0
    private var bps: Double = 0.0

    @SubscribeEvent
    fun blockBreak(event: BlockEvent.BreakEvent) {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        isBreaking = true
        blocksBroken++
        lastBrokenBlock = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun tick(event: ClientTickEvent) {
        if (!isBreaking) return
        val secondsElapsed = (System.currentTimeMillis() - startTime) / 1000.0
        bps = (blocksBroken / secondsElapsed).round(2)

        if (System.currentTimeMillis() - lastBrokenBlock > 1000 * 1) {
            bps = 0.0
            isBreaking = false
            blocksBroken = 0
            startTime = 0
            lastBrokenBlock = 0
        }
    }

}