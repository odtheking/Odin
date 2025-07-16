package me.odinmain.clickgui.settings

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.odinmain.features.Module
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Superclass of Settings.
 * @author Aton
 */
abstract class Setting<T> (
    val name: String,
    var description: String = "",
) : ReadWriteProperty<Module, T>, PropertyDelegateProvider<Module, ReadWriteProperty<Module, T>> {

    /**
     * Default value of the setting
     */
    abstract val default: T

    /**
     * Value of the setting
     */
    abstract var value: T

    private var hidden = false

    fun hide(): Setting<T> {
        hidden = true
        return this
    }

    /**
     * Dependency for if it should be shown in the [click gui][Module].
     */
    protected var visibilityDependency: (() -> Boolean)? = null

    /**
     * Resets the setting to the default value
     */
    open fun reset() {
        value = default
    }

    val isVisible: Boolean get() = (visibilityDependency?.invoke() ?: true) && !hidden

    override operator fun provideDelegate(thisRef: Module, property: KProperty<*>): ReadWriteProperty<Module, T> =
        thisRef.register(this)

    override operator fun getValue(thisRef: Module, property: KProperty<*>): T =
        value

    override operator fun setValue(thisRef: Module, property: KProperty<*>, value: T) {
        this.value = value
    }

    companion object {

        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        fun <K : Setting<T>, T> K.withDependency(dependency: () -> Boolean): K {
            visibilityDependency = dependency
            return this
        }
    }
}