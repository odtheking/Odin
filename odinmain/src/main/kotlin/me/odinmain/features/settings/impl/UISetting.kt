package me.odinmain.features.settings.impl

import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.elements.Element
import me.odinmain.features.settings.Setting

// doesn't save
// todo:rename and cleanup
class UISetting(val height: Size, val block: Element.() -> Unit) : Setting<Any?>("", false, "") {
    override val default: Any? = null
    override var value: Any? = null

    override fun getElement(parent: Element): SettingElement = parent.setting(height, block)
}