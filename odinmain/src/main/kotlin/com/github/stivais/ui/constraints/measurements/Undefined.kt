package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

data object Undefined : Measurement {
    override fun get(element: Element, type: Type): Float {
        return 0f
    }
}