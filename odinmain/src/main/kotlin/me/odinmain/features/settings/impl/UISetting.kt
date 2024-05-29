package me.odinmain.features.settings.impl

import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.elements.scope.ElementScope
import me.odinmain.features.settings.Setting

// doesn't save
// todo:rename and cleanup
class UISetting(height: Size, val block: ElementScope<*>.() -> Unit) : Setting<Any?>("", false, "") {
    override val default: Any? = null
    override var value: Any? = null

    private val _height = height

    override fun ElementScope<*>.createElement() {
        setting(_height) {
            block()
        }
    }
}