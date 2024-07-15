package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.impl.TextInput
import com.github.stivais.ui.events.Mouse
import me.odinmain.utils.skyblock.modMessage


fun basic() = UI {

    TextInput("", "placeholder", size = 20.px, censor = true) {
        modMessage(it.toFloatOrNull())
    }.also { it.registerEvent(Mouse.Clicked(1)) {
        it.censorInput = !it.censorInput
        false
    } }.add()

}