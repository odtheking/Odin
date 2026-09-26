package com.odtheking.odin.utils.ui.animations

object Animations {

    var generation: Int = 0
        private set

    fun settle() {
        generation++
    }

    const val UNSETTLED = Int.MIN_VALUE
}