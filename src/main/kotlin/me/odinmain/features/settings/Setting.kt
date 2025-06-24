package me.odinmain.features.settings

import com.github.stivais.aurora.components.scope.ContainerScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.odinmain.features.Module
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Superclass for settings
 *
 * If you want to implement saving/loading for your setting, you need to implement the [Saving] interface
 *
 * @param name Name of the setting
 * @param hidden If setting shouldn't ever appear in the UI
 * @param description Description for the setting
 */
// todo: remove hidden param
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

    /**
     * Dependency for if it should be shown in the UI
     *
     * @see DrawableOld
     */
    var visibilityDependency: (() -> Boolean)? = null

    /**
     * Resets the setting to the default value
     */
    open fun reset() {
        value = default
    }


    override operator fun provideDelegate(thisRef: Module, property: KProperty<*>): ReadWriteProperty<Module, T> {
        return thisRef.register(this)
    }

    override operator fun getValue(thisRef: Module, property: KProperty<*>): T {
        return value
    }

    override operator fun setValue(thisRef: Module, property: KProperty<*>, value: T) {
        this.value = value
    }

    open fun ContainerScope<*>.example() {

    }

    companion object {

        /**
         * [Gson] for saving and loading settings
         */
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        /**
         * Adds a dependency for a setting, for it to only be rendered if it matches true
         *
         * @see DrawableOld
         */
        fun <K : Setting<T>, T> K.withDependency(dependency: () -> Boolean): K {
            visibilityDependency = dependency
            return this
        }
    }
}