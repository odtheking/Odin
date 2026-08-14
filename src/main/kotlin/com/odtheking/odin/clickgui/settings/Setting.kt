package com.odtheking.odin.clickgui.settings

import com.odtheking.odin.features.Module
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Setting<T> : ReadWriteProperty<Module, T>, PropertyDelegateProvider<Module, ReadWriteProperty<Module, T>> {
    val name: String
    var description: String

    val default: T
    var value: T

    var hidden: Boolean
    var visibilityDependency: (() -> Boolean)?

    val isVisible: Boolean
        get() = !hidden && visibilityDependency?.invoke() != false

    fun reset() {
        value = default
    }

    fun hide(): Setting<T> {
        hidden = true
        return this
    }

    override operator fun provideDelegate(thisRef: Module, property: KProperty<*>): ReadWriteProperty<Module, T> =
        thisRef.registerSetting(this)

    override operator fun getValue(thisRef: Module, property: KProperty<*>): T = value

    override operator fun setValue(thisRef: Module, property: KProperty<*>, value: T) {
        this.value = value
    }

    companion object {
        fun <K : Setting<*>> K.withDependency(dependency: () -> Boolean): K {
            visibilityDependency = dependency
            return this
        }
    }
}

abstract class AbstractSetting<T>(
    override val name: String,
    override var description: String = ""
) : Setting<T> {
    override var hidden: Boolean = false
    override var visibilityDependency: (() -> Boolean)? = null
}