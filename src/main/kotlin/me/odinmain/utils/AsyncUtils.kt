package me.odinmain.utils

import kotlinx.coroutines.*
import net.minecraft.inventory.ContainerChest


suspend fun waitUntilLastItem(container: ContainerChest) = coroutineScope {
    val deferredResult = CompletableDeferred<Unit>()
    val startTime = System.currentTimeMillis()

    fun check() {
        if (System.currentTimeMillis() - startTime > 1000) {
            deferredResult.completeExceptionally(Exception("Promise rejected"))
            return
        } else if (container.inventory[container.inventory.size - 37] != null) {
            deferredResult.complete(Unit)
        } else {
            launch {
                delay(10)
                check()
            }
        }
    }

    launch {
        check()
    }

    deferredResult
}