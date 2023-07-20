package me.odinclient.utils

import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

open class Executor(val delay: Long, inline val func: () -> Unit) {

    var lastTime = System.currentTimeMillis()
    inline val time get() = System.currentTimeMillis() - lastTime

    fun run(): Boolean {
        if (shouldFinish) {
            onFinish()
            return true
        }
        if (time >= delay) {
            also()
            func()
            lastTime = System.currentTimeMillis()
        }
        return false
    }

    open fun also() {}

    private var condition: () -> Boolean = { false }

    open val shouldFinish = condition()

    private var onFinish: () -> Unit = {}

    fun onFinish(func: () -> Unit) = apply {
        onFinish = func
        println("Applied function")
    }

    class LimitedExecutor(delay: Long, val repeats: Int, func: () -> Unit) : Executor(delay, func) {
        private var cycles = 0
        override val shouldFinish: Boolean get() = cycles > repeats
        override fun also() { cycles++ }
    }

    class ConditionalExecutor(delay: Long, val condition: () -> Boolean, func: () -> Unit) : Executor(delay, func) {
        override val shouldFinish: Boolean get() = condition()
    }

    companion object {

        fun ArrayList<Executor>.executeList() {
            if (this.size == 0) return
            val time = System.nanoTime()
            this.removeAll { it.run() }
            println(System.nanoTime() - time)
        }

        val executors = ArrayList<Executor>()

        @SubscribeEvent
        fun onUpdate(event: RenderWorldLastEvent) {
            executors.executeList()
        }
    }
}