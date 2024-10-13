package com.github.stivais.ui.impl

import com.github.stivais.ui.UIScreen.Companion.open
import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.LeapMenu.leapMenu


val `ui command` = commodore("ui") {
    literal("test").runs {
        open(basic())
    }
    literal("leap").runs {
        open(leapMenu())
    }
}