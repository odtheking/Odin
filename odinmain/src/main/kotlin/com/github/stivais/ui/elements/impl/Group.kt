package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined


class Group(constraints: Constraints?) : Element(constraints.replaceUndefined(w = Bounding, h = Bounding)) {
    override fun draw() {
//        renderer.hollowRect(x, y, width, height, 1f, java.awt.Color.WHITE.rgb)
    }
}