package me.odinclient.utils

import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

//TODO: ADD AN OPTIONAL EXECUTOR THAT REMOVES AFTER A CERTAIN AMOUNT OF REPEATS
class Executor(val delay: Long, register: Boolean = false, val func: () -> Unit) {

    var lastTime = System.currentTimeMillis()
    inline val time get() = System.currentTimeMillis() - lastTime

    init {
        if (register) executors.add(this)
    }

    companion object {
        val executors = ArrayList<Executor>()

        @SubscribeEvent
        fun onUpdate(event: RenderWorldLastEvent) {
            for (i in executors) {
                if (i.time >= i.delay) {
                    i.func.invoke()
                    i.lastTime = System.currentTimeMillis()
                }
            }
        }
    }
}