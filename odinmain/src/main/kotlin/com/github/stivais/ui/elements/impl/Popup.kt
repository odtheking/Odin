package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope

class Popup(element: Group) : ElementScope<Group>(element) {
    fun close() {
        ui.main.removeElement(element)
    }
}

fun ElementDSL.popup(constraints: Constraints? = size(Bounding, Bounding), block: Popup.() -> Unit) {
    val group = Group(constraints)
    ui.main.addElement(group)
    Popup(group).block()
}