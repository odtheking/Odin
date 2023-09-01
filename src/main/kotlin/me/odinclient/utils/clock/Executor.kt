package me.odinclient.utils.clock

import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Class that allows repeating execution of code while being dynamic.
 * @author Stivais
 */
open class Executor(open val delay: Long, inline val func: Executable) {

    private val clock = Clock()
    private var shouldFinish = false

    open fun also() {}

    fun run(): Boolean {
        if (shouldFinish) return true
        if (clock.hasTimePassed(delay, true)) {
            runCatching {
                also()
                func()
            }
        }
        return false
    }

    /**
     * Starts an executor that can vary in delay rather than being static.
     * @author Stivais
     */
    class VaryingExecutor(val delay2: () -> Long, func: Executable) : Executor(delay2(), func) {
        override val delay: Long get() = delay2()
    }

    /**
     * Starts an executor that ends after a certain amount of times.
     * @author Stivais
     */
    class LimitedExecutor(delay: Long, repeats: Int, func: Executable) : Executor(delay, func) {
        private val repeats = repeats - 1
        private var totalRepeats = 0

        override fun also() {
            if (totalRepeats >= repeats) destroyExecutor()
            totalRepeats++
        }
    }

    /**
     * Allows to stop executing an executor
     *
     * Returning [Nothing] allows for us to stop running the function without specifyinge
     * @author Stivais
     */
    fun Executor.destroyExecutor(): Nothing {
        shouldFinish = true
        throw Throwable()
    }

    companion object {

        private val executors = ArrayList<Executor>()

        fun ArrayList<Executor>.executeAll() {
            this.removeAll {
                it.run()
            }
        }

        fun Executor.register() {
            executors.add(this)
        }

        @SubscribeEvent
        fun onRender(event: RenderWorldLastEvent) {
            executors.executeAll()
        }
    }
}

/**
 * Here for more readability
 */
typealias Executable = Executor.() -> Unit