package me.odinmain.utils.clock

import me.odinmain.utils.skyblock.ChatUtils.devMessage
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Class that allows repeating execution of code while being dynamic.
 * @author Stivais
 */
open class Executor(val delay: () -> Long, val func: Executor.() -> Unit) {

    constructor(delay: Long, func: Executor.() -> Unit) : this({ delay } , func)

    internal val clock = Clock()
    internal var shouldFinish = false

    open fun run(): Boolean {
        if (shouldFinish) return true
        if (clock.hasTimePassed(delay(), true)) {
            runCatching {
                this.func()
            }
        }
        return false
    }

    /**
     * Starts an executor that ends after a certain amount of times.
     * @author Stivais
     */
    class LimitedExecutor(delay: Long, repeats: Int, func: Executor.() -> Unit) : Executor(delay, func) {
        private val repeats = repeats - 1
        private var totalRepeats = 0

        override fun run(): Boolean {
            if (shouldFinish) return true
            if (clock.hasTimePassed(delay(), true)) {
                runCatching {
                    if (totalRepeats >= repeats) destroyExecutor()
                    totalRepeats++
                    func()
                }
            }
            return false
        }
    }

    /**
     * Allows to stop executing an executor
     *
     * Returning [Nothing] allows for us to stop running the function without specifyinge
     * @author Stivais
     */
    fun Executor.destroyExecutor(): Nothing {
        devMessage("destroying")
        shouldFinish = true
        throw Throwable()
    }

    companion object {

        private val executors = ArrayList<Executor>()

        fun Executor.register() {
            executors.add(this)
        }

        @SubscribeEvent
        fun onRender(event: RenderWorldLastEvent) {
            executors.removeAll { it.run() }
        }
    }
}