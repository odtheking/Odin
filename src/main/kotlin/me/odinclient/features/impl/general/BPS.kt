package me.odinclient.features.impl.general

import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.round

object BPS : Module(
    name = "BPS",
    category = Category.GENERAL
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

    // basically JavaScript toFixed() method
    private fun Double.round(decimals: Int): Double
    {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

}