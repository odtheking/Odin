package me.odinmain.utils.data

data class ReferenceCounted<T>(val value: T) {

    var count = 0

    fun increment() {
        count++
    }

    fun decrement(): Boolean {
        count--
        return count == 0
    }
}