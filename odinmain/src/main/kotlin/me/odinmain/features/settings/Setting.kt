package me.odinmain.features.settings

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.forLoop
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

    internal open fun ElementScope<*>.createElement() {}

    protected fun ElementDSL.setting(height: Size, block: ElementScope<SettingElement>.() -> Unit): SettingElement {
        val element = SettingElement(height)
        create(ElementScope(element), block)
        return element
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

    // todo: cleanup
    protected inner class SettingElement(height: Size) : Element(size(240.px, Animatable(from = height, to = 0.px))) {

        private var visible: Boolean = visibilityDependency?.invoke() ?: true

        init {
            scissors = true
            if (!visible) {
                (constraints.height as Animatable).swap()
            }
            onInitialization {
                if (ui.onOpen == null) ui.onOpen = arrayListOf()
                ui.onOpen!!.add {
                    elements?.forLoop { fixHeight(it) }
                }
            }
        }

        override fun draw() {
            if ((visibilityDependency?.invoke() != false) != visible) {
                visible = !visible
                constraints.height.animate(0.25.seconds, Animations.EaseInOutQuint)
                redraw()
            }
        }

        // this is a weird way to get animations to work nicely with px
        private fun fixHeight(element: Element) {
            element.constraints.apply {
                if (height is Pixel) {
                    height = 100.percent.coerce((height as Pixel).pixels)
                }
            }
            element.elements?.forLoop {
                fixHeight(it)
            }
        }
    }
}