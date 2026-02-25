package com.odtheking.odin.events.core

import com.odtheking.odin.events.PacketEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.util.profiling.Profiler
import net.minecraft.util.profiling.ProfilerFiller

object EventBus {

    @JvmField
    internal val listenerArrays = mutableMapOf<Class<out Event>, Array<ListenerEntry<out Event>>>()
    @JvmField
    internal val activeSubscribers = mutableSetOf<Any>()
    @JvmField
    internal val subscriberClasses = mutableMapOf<Any, Class<*>>()
    @JvmField
    internal val invokers = HashMap<Class<out Event>, Invoker>()

    private val profilerNameCache = HashMap<Class<out Event>, String>()

    fun subscribe(subscriber: Any) {
        if (activeSubscribers.add(subscriber)) {
            subscriberClasses[subscriber] = subscriber.javaClass
            rebuildAffectedCaches(subscriber.javaClass)
        }
    }

    fun unsubscribe(subscriber: Any) {
        if (activeSubscribers.remove(subscriber))
            subscriberClasses.remove(subscriber)?.let { rebuildAffectedCaches(it) }
    }

    @JvmStatic
    fun <T : Event> post(event: T) {
        val eventClass = event.javaClass
        val invoker = invokers[eventClass] ?: return

        val profiler = Profiler.get()
        val profilerName = profilerNameCache.getOrPut(eventClass) { "Odin: ${eventClass.simpleName}" }
        profiler.push(profilerName)
        try {
            invoker.invoke(event, profiler)
        } finally {
            profiler.pop()
        }
    }

    fun <T : Event> registerListener(
        subscriberClass: Class<*>,
        eventClass: Class<T>,
        priority: Int,
        ignoreCancelled: Boolean,
        handler: (T) -> Unit
    ) {
        val name = subscriberClass.simpleName.ifEmpty { subscriberClass.name }
        val entry = ListenerEntry(subscriberClass, EventListener(priority, ignoreCancelled, name, handler))

        val existing = listenerArrays[eventClass] ?: emptyArray()
        val newArray = (existing + entry).sortedByDescending { it.listener.priority }.toTypedArray()
        listenerArrays[eventClass] = newArray
        rebuildInvoker(eventClass, newArray)
    }

    private fun rebuildAffectedCaches(changedClass: Class<*>) {
        for ((eventClass, listeners) in listenerArrays) {
            if (listeners.any { it.subscriber == changedClass }) rebuildInvoker(eventClass, listeners)
        }
    }

    private fun rebuildInvoker(
        eventClass: Class<out Event>,
        allListeners: Array<ListenerEntry<*>>
    ) {
        if (activeSubscribers.isEmpty()) {
            invokers[eventClass] = EmptyInvoker
            return
        }

        val activeClasses = subscriberClasses.values

        @Suppress("UNCHECKED_CAST")
        val activeListeners = allListeners
            .filter { it.subscriber in activeClasses }
            .map { it.listener as EventListener<Event> }
            .toTypedArray()

        invokers[eventClass] = when {
            activeListeners.isEmpty() -> EmptyInvoker
            else -> InvokerFactory.build(activeListeners)
        }
    }

    data class ListenerEntry<T : Event>(
        val subscriber: Class<*>,
        val listener: EventListener<T>
    )

    class EventListener<T : Event>(
        val priority: Int,
        ignoreCancelled: Boolean,
        val subscriberName: String,
        val handler: (T) -> Unit
    ) {
        @JvmField
        val checkCancelled = ignoreCancelled

        fun invoke(event: T) {
            if (!checkCancelled || event !is CancellableEvent || !event.isCancelled)
                handler(event)
        }
    }

    interface Invoker {
        fun invoke(event: Event, profiler: ProfilerFiller)
    }

    private object EmptyInvoker : Invoker {
        override fun invoke(event: Event, profiler: ProfilerFiller) {}
    }

    private object InvokerFactory {
        fun build(listeners: Array<EventListener<Event>>): Invoker {
            return object : Invoker {
                override fun invoke(event: Event, profiler: ProfilerFiller) {
                    for (listener in listeners) {
                        profiler.push(listener.subscriberName)
                        try {
                            listener.invoke(event)
                        } finally {
                            profiler.pop()
                        }
                    }
                }
            }
        }
    }
}

inline fun <reified T : Event> Any.on(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: T.() -> Unit
) = EventBus.registerListener(this.javaClass, T::class.java, priority, ignoreCancelled) {
    it.handler()
}

inline fun <reified P : Packet<*>> Any.onReceive(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Receive) -> Unit
) = EventBus.registerListener(this.javaClass, PacketEvent.Receive::class.java, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}

inline fun <reified P : Packet<*>> Any.onSend(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Send) -> Unit
) = EventBus.registerListener(this.javaClass, PacketEvent.Send::class.java, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}