package me.odinclient.utils

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentLinkedQueue

//TODO: ADD AN OPTIONAL EXECUTOR THAT REMOVES AFTER A CERTAIN AMOUNT OF REPEATS
class Executor(val delay: Long, val func: () -> Unit) {

    var lastTime = System.currentTimeMillis()
    inline val time get() = System.currentTimeMillis() - lastTime

    init {
        executors.add(this)
    }

    companion object {
        val executors = ConcurrentLinkedQueue<Executor>()

        @SubscribeEvent
        fun onUpdate(event: LivingUpdateEvent) {
            val currentTime = System.nanoTime()
            for (i in executors) {
                if (i.time >= i.delay) {
                    i.func.invoke()
                    i.lastTime = System.currentTimeMillis()
                }
            }
        }
    }
}