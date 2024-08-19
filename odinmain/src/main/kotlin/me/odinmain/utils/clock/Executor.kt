package me.odinmain.utils.clock

import me.odinmain.utils.profile
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Class that allows repeating execution of code while being dynamic.
 * @author Stivais
 */
open class Executor(val delay: () -> Long, private val profileName: String = "Unspecified odin executor", val func: Executable) {

    constructor(delay: Long, profileName: String = "Unspecified odin executor", func: Executable) : this({ delay }, profileName, func)

    internal val clock = Clock()
    internal var shouldFinish = false

    open fun run(): Boolean {
        if (shouldFinish) return true
        profile(profileName) {
            if (clock.hasTimePassed(delay(), true)) {
                runCatching {
                    func()
                }
            }
        }
        return false
    }

    /**
     * Starts an executor that ends after a certain amount of times.
     * @author Stivais
     */
    class LimitedExecutor(delay: Long, repeats: Int, profileName: String = "Unspecified odin executor", func: Executable) : Executor(delay, profileName, func) {
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

/**
 * Here for more readability
 */
typealias Executable = Executor.() -> Unit