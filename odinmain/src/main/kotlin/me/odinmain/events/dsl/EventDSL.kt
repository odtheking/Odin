package me.odinmain.events.dsl

import net.minecraftforge.fml.common.eventhandler.IEventListener
import net.minecraftforge.fml.common.eventhandler.Event as ForgeEvent

open class EventDSL {
    val listeners: ArrayList<Listener<*>> = ArrayList()

    inline fun <reified E : ForgeEvent> onEvent(crossinline block: (E) -> Unit) {
        listeners.add(
            object : Listener<E>(E::class.java) {
                override fun invokeEvent(event: E) {
                    block(event)
                }
            }
        )
    }
}

abstract class Listener<E : ForgeEvent>(val event: Class<E>) : IEventListener {

    override fun invoke(event: ForgeEvent) {
        if (event.isCancelable && event.isCanceled) return
        @Suppress("UNCHECKED_CAST")
        val it = event as? E ?: return
        invokeEvent(it)
    }

    abstract fun invokeEvent(event: E)
}