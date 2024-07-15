package com.github.stivais.ui.impl

import com.github.stivais.ui.UIScreen.Companion.open
import com.github.stivais.ui.renderer.impl.NVGRenderer
import me.odinmain.commands.commodore

val `ui command` = commodore("ui") {
    runs {
        open(basic())
    }
}