package com.odtheking.odin.utils.ui.compose

import kotlin.math.sign

class ScrollState(private val step: Int = 16) {
    var maxScroll = 0
    private var value = 0

    var offset: Int
        get() = value.coerceIn(-maxScroll.coerceAtLeast(0), 0)
        set(new) { value = new.coerceIn(-maxScroll.coerceAtLeast(0), 0) }

    fun by(amount: Double) {
        offset += (amount.sign * step).toInt()
    }
}