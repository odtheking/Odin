package me.odinclient.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.features.impl.dungeon.AutoIceFill
import me.odinmain.utils.skyblock.getBlockIdAt
import net.minecraft.util.Vec3



suspend fun waitUntilPacked(x: Double, y: Double, z: Double) = coroutineScope {
    val deferredResult = CompletableDeferred<Unit>()

    fun check() {
        if (!AutoIceFill.enabled) {
            deferredResult.completeExceptionally(Exception("Promise rejected"))
            return
        }
        if (getBlockIdAt(x, y, z) == 174) {
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

    suspend fun waitUntilPacked(vec: Vec3): CompletableDeferred<Unit> = coroutineScope {
        waitUntilPacked(vec.xCoord, vec.yCoord, vec.zCoord)
    }
