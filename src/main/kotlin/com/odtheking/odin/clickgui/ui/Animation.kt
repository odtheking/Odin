package com.odtheking.odin.clickgui.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import com.odtheking.odin.utils.ui.animations.Easing
import com.odtheking.odin.utils.ui.compose.*

@Composable
fun animateFloatAsState(target: Float, durationMillis: Int = 150, easing: Easing = Easing.LINEAR): Animatable {
    val animatable = remember { Animatable(target) }
    SideEffect { animatable.animateTo(target, durationMillis, easing) }
    return animatable
}

@Composable
fun animateProgress(target: Boolean, durationMillis: Int = 150, easing: Easing = Easing.LINEAR): Animatable =
    animateFloatAsState(if (target) 1f else 0f, durationMillis, easing)

@Composable
fun animateProgress(durationMillis: Int = 150, easing: Easing = Easing.LINEAR, target: () -> Boolean): Animatable {
    val current = rememberUpdatedState(target)
    return remember {
        object : Animatable(if (Snapshot.withoutReadObservation(target)) 1f else 0f) {
            override val value: Float
                get() {
                    animateTo(if (current.value()) 1f else 0f, durationMillis, easing)
                    return super.value
                }
        }
    }
}

@Composable
fun AnimatedVisibility(
    visible: Boolean,
    durationMillis: Int = 200,
    easing: Easing = Easing.EASE_IN_OUT,
    content: @Composable () -> Unit,
): UiNode? {
    val progress = animateProgress(visible, durationMillis, easing)
    var present by remember { mutableStateOf(visible) }
    SideEffect { if (visible) present = true }

    return if (visible || present) Reveal(progress, { present = false }, content) else null
}

@Composable
fun AnimatedVisibility(
    visible: () -> Boolean,
    durationMillis: Int = 200,
    easing: Easing = Easing.EASE_IN_OUT,
    content: @Composable () -> Unit,
): UiNode {
    val progress = animateProgress(durationMillis, easing, visible)
    return Reveal(progress, {}, content)
}

@Composable
private fun Reveal(progress: Animatable, onHidden: () -> Unit, content: @Composable () -> Unit): UiNode =
    Column(content = content)
        .width { if (progress.value == 0f) 0 else width }
        .height {
            val fraction = progress.value
            if (fraction == 0f && progress.target == 0f) onHidden()
            (height * fraction).toInt()
        }.clip { progress.value < 1f }