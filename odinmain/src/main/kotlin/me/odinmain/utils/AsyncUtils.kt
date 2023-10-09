package me.odinmain.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.Companion.mc
import net.minecraft.inventory.ContainerChest

object AsyncUtils {
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

    suspend fun waitUntilPlayer() = coroutineScope {
        val deferredResult = CompletableDeferred<Unit>()

        fun check(times: Int) {
            if (times > 100) {
                deferredResult.completeExceptionally(Exception("Player took too long to load, aborting!"))
                return
            }
            if (mc.thePlayer != null) {
                deferredResult.complete(Unit)
            } else {
                launch {
                    delay(500)
                    check(times + 1)
                }
            }
        }

        launch {
            check(0)
        }

        deferredResult
    }
}