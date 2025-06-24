package me.odinmain.utils.data

import kotlin.reflect.KProperty

class Ref<T>(var value: T) {
    operator fun getValue(thisRef: T, property: KProperty<*>): T {
        return value
    }
    operator fun setValue(thisRef: T, property: KProperty<*>, value: T) {
        this.value = value
    }
}