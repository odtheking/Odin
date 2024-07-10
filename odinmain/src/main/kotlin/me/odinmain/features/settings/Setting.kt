package me.odinmain.features.settings

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.events.Lifetime
import com.github.stivais.ui.impl.description
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.loop
import com.github.stivais.ui.utils.seconds
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.odinmain.features.Module
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Superclass for settings
 *
 * If you want to implement saving/loading for your setting, you need to also use the [Saving] interface
 *
 * @param name Name of the setting
 * @param hidden If setting shouldn't ever appear in the UI
 * @param description Description for the setting
 *
 * @author Stivais, Aton
 */
abstract class Setting<T> (
    val name: String,
    var hidden: Boolean = false,
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
     * @see SettingElement
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

    /**
     * Creates the elements required for the UI
     *
     * It is highly recommended to use [setting] as your base element, due to simplify animations
     */
    internal open fun ElementScope<*>.createElement() {}

    /**
     * Intended to be used as a base in [createElement] to easily provide animations for settings with potential requirements
     */
    protected fun ElementDSL.setting(height: Size, block: ElementScope<SettingElement>.() -> Unit = {}): ElementScope<*> {
        return create(ElementScope(SettingElement(height)).also { it.description(description) }, block)
    }

    companion object {

        /**
         * [Gson] for saving and loading settings
         */
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        /**
         * Adds a dependency for a setting, for it to only be rendered if it matches true
         *
         * @see SettingElement
         */
        fun <K : Setting<T>, T> K.withDependency(dependency: () -> Boolean): K {
            visibilityDependency = dependency
            return this
        }
    }

    // TODO: Find out why ClickGUI runs 2x slower until a setting visibility changes
    protected inner class SettingElement(height: Size) : Element(size(240.px, Animatable(from = height, to = 0.px))) {

        private var visible: Boolean = visibilityDependency?.invoke() ?: true

        init {
            alphaAnim = Animatable(from = 1.px, to = 0.px)
            scissors = true
            if (!visible) {
                (constraints.height as Animatable).swap()
                (alphaAnim as Animatable).swap()
            }

            Lifetime.AfterInitialized register {
                elements?.loop { fixHeight(it) }
                false
            }
        }

        override fun draw() {
            if ((visibilityDependency?.invoke() != false) != visible) {
                visible = !visible
                constraints.height.animate(0.25.seconds, Animations.EaseInOutQuint)
                alphaAnim!!.animate(0.25.seconds, Animations.EaseInOutQuint)
                redraw = true
            }
        }

        // this is a weird way to get animations to work nicely with px
        private fun fixHeight(element: Element) {
            element.constraints.apply {
                if (height is Pixel) {
                    height = 100.percent.coerce((height as Pixel).pixels)
                }
            }
            element.elements?.loop {
                fixHeight(it)
            }
        }
    }
}