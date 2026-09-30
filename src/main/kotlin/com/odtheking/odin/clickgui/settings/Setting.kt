package com.odtheking.odin.clickgui.settings

import androidx.compose.runtime.Composable
import com.odtheking.odin.features.Module
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Setting<T>(
    val name: String,
    var description: String
) : ReadWriteProperty<Module, T>, PropertyDelegateProvider<Module, ReadWriteProperty<Module, T>> {

    abstract val default: T
    abstract var value: T

    var hidden: Boolean = false
    var visibilityDependency: (() -> Boolean)? = null

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

abstract class RenderableSetting<T>(name: String, description: String) : Setting<T>(name, description) {
    @Composable
    abstract fun Content()
}