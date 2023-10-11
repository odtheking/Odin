package me.odinclient.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.features.impl.dungeon.AutoIceFill
import me.odinmain.utils.skyblock.WorldUtils
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

object AsyncUtils {

    suspend fun waitUntilPacked(x: Double, y: Double, z: Double) = coroutineScope {
        val deferredResult = CompletableDeferred<Unit>()

        fun check() {
            if (!AutoIceFill.enabled) {
                deferredResult.completeExceptionally(Exception("Promise rejected"))
                return
            }
            if (WorldUtils.getBlockIdAt(x, y, z) == 174) {
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
        val deferredResult = CompletableDeferred<Unit>()
        val bPos = BlockPos(vec)

        fun check() {
            if (!AutoIceFill.enabled) {
                deferredResult.completeExceptionally(Exception("Promise rejected"))
                return
            }
            if (WorldUtils.getBlockIdAt(bPos) == 174) {
                deferredResult.complete(Unit)
                return
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
}