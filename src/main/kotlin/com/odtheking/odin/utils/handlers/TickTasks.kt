package com.odtheking.odin.utils.handlers

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.logError
import java.util.concurrent.CopyOnWriteArrayList

open class TickTask(
    private val ticksPerCycle: Int,
    serverTick: Boolean = false,
    private val task: () -> Unit
) {
    internal var ticks = 0

    init {
        if (serverTick) TickTasks.registerServerTask(this)
        else TickTasks.registerClientTask(this)
    }

    open fun run() {
        if (++ticks < ticksPerCycle) return
        runCatching(task).onFailure { logError(it, this) }
        ticks = 0
    }
}

class OneShotTickTask(ticks: Int, serverTick: Boolean = false, task: () -> Unit) : TickTask(ticks, serverTick, task) {
    override fun run() {
        super.run()
        if (ticks == 0) TickTasks.unregister(this)
    }
}

fun schedule(ticks: Int, serverTick: Boolean = false, task: () -> Unit) {
    OneShotTickTask(ticks, serverTick, task)
}

object TickTasks {
    private val clientTickTasks = CopyOnWriteArrayList<TickTask>()
    private val serverTickTasks = CopyOnWriteArrayList<TickTask>()

    fun registerClientTask(task: TickTask) = clientTickTasks.add(task)
    fun registerServerTask(task: TickTask) = serverTickTasks.add(task)

    fun unregister(task: TickTask) {
        clientTickTasks.remove(task)
        serverTickTasks.remove(task)
    }

    init {
        on<TickEvent.End> {
            clientTickTasks.forEach { it.run() }
        }

        on<TickEvent.Server> {
            serverTickTasks.forEach { it.run() }
        }
    }
}
