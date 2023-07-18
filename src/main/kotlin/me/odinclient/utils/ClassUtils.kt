package me.odinclient.utils

import org.reflections.Reflections

/**
 * https://github.com/lambda-client/lambda/blob/59752a31d5b179099ceb9c60bc1e77e5446fd69e/src/main/kotlin/com/lambda/client/commons/utils/ClassUtils.kt#L5
 */
object ClassUtils {

    inline fun <reified T> findClasses(
        pack: String,
        noinline block: Sequence<Class<out T>>.() -> Sequence<Class<out T>> = { this }
    ): List<Class<out T>> {
        return findClasses(pack, T::class.java, block)
    }

    fun <T> findClasses(
        pack: String,
        subType: Class<T>,
        block: Sequence<Class<out T>>.() -> Sequence<Class<out T>> = { this }
    ): List<Class<out T>> {
        return Reflections(pack).getSubTypesOf(subType).asSequence()
            .run(block)
            .sortedBy { it.simpleName }
            .toList()
    }

    @Suppress("UNCHECKED_CAST")
    val <T> Class<out T>.instance get() = this.getDeclaredField("INSTANCE")[null] as T
}