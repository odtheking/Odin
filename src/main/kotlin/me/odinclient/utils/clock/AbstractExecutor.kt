package me.odinclient.utils.clock

//TODO: Rename stuff
abstract class AbstractExecutor(inline val func: () -> Unit) {

    private val clock = Clock()

    abstract val delay: Long
    abstract val shouldFinish: Boolean

    private var onFinish: () -> Unit = {}
    fun onFinish(func: () -> Unit) = apply { onFinish = func }

    open fun also() {}

    fun run(): Boolean {
        if (shouldFinish) {
            onFinish()
            return true
        }
        if (clock.hasTimePassed(delay, setTime = true)) {
            also()
            func()
        }
        return false
    }

    class Executor(override val delay: Long, func: () -> Unit): AbstractExecutor(func) {
        override val shouldFinish: Boolean = false
    }

    class ConditionalExecutor(override val delay: Long, val condition: () -> Boolean, func: () -> Unit) : AbstractExecutor(func) {
        override val shouldFinish: Boolean get() = condition()
    }

    class VaryingExecutor(val delay2: () -> Long, func: () -> Unit) : AbstractExecutor(func) {
        override val delay: Long get() = delay2()
        override val shouldFinish: Boolean = false
    }

    class LimitedExecutor(override val delay: Long, private val repeats: Int, func: () -> Unit): AbstractExecutor(func) {
        private var cycles = 0

        override val shouldFinish: Boolean get() = cycles > repeats

        override fun also() {
            cycles++
        }
    }

    companion object {

        fun ArrayList<AbstractExecutor>.executeAll() {
            this.removeAll { it.run() }
        }

        private val executors = ArrayList<AbstractExecutor>()

        fun AbstractExecutor.register() {
            executors.add(this)
        }
    }
}