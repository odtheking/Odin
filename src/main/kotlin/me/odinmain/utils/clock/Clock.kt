package me.odinmain.utils.clock

@Suppress("NOTHING_TO_INLINE")
/**
 * Class to simplify handling delays with [System.currentTimeMillis]
 *
 * @see [hasTimePassed]
 * @see [Executor]
 * @author Stivais
 */
class Clock(val delay: Long = 0L) {

    var lastTime = System.currentTimeMillis()

    inline fun getTime(): Long {
        return System.currentTimeMillis() - lastTime
    }

    inline fun setTime(time: Long) {
        lastTime = time
    }

    /**
     * Sets lastTime to now
     */
    inline fun update() {
        lastTime = System.currentTimeMillis()
    }

    inline fun updateCD() {
        lastTime = System.currentTimeMillis() + delay
    }

    /**
     * @param setTime sets lastTime if time has passed
     */
    inline fun hasTimePassed(setTime: Boolean = false): Boolean {
        if (getTime() >= delay) {
            if (setTime) lastTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    inline fun timeLeft(): Long {
        return lastTime - System.currentTimeMillis()
    }

    /**
     * @param delay the delay to check if it has passed since lastTime
     * @param setTime sets lastTime if time has passed
     */
    inline fun hasTimePassed(delay: Long, setTime: Boolean = false): Boolean {
        if (getTime() >= delay) {
            if (setTime) lastTime = System.currentTimeMillis()
            return true
        }
        return false
    }
}