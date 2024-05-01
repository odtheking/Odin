package me.odinmain.features.settings

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.seconds
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
     * Dependency for if it should be shown in the [click gui][me.odinmain.ui.clickgui.elements.ModuleButton].
     */
    protected var visibilityDependency: (() -> Boolean)? = null

    /**
     * Resets the setting to the default value
     */
    open fun reset() {
        value = default
    }

    val shouldBeVisible: Boolean
        get() {
            return (visibilityDependency?.invoke() ?: true) && !hidden
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

    // todo: cleanup
    /**
     * You NEED to use [setting] for stuff to be properly initialized
     */
    open fun getUIElement(parent: Element): SettingElement? = null

    inline fun Element.setting(height: Size, block: SettingElement.() -> Unit): SettingElement {
        val element = SettingElement(height)
        addElement(element)
        element.block()
        return element
    }

    // todo: improve animation
    inner class SettingElement(height: Size) : Element(size(240.px, Animatable(from = height, to = 0.px))) {

        private var visible: Boolean = visibilityDependency?.invoke() ?: true

        init {
            if (!visible) (height() as Animatable).swap()
            scissors = true
        }

        override fun draw() {
            if ((visibilityDependency?.invoke() != false) != visible) {
                visible = !visible
                height().animate(0.2.seconds, Animations.EaseInOutQuint)
            }
        }
    }

    companion object {

        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        fun <K : Setting<T>, T> K.withDependency(dependency: () -> Boolean): K {
            visibilityDependency = dependency
            return this
        }
    }
}