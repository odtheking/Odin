package com.github.stivais.ui.impl

import com.github.stivais.ui.UIScreen.Companion.open
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.impl.huds.HUD
import com.github.stivais.ui.impl.huds.HUDManager
import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.LeapMenu.leapMenu

lateinit var hud: HUD

val `ui command` = commodore("ui") {

    hud = HUD(0f, 0f) {
        if (preview) {
            text("hello world", size = 20.px)
        } else {
            text("bye world", size = 20.px)
        }

    }

    literal("hud").runs {
        HUDManager.openEditor()
    }

    literal("test").runs {
        open(basic())
    }
    literal("leap").runs {
        open(leapMenu())
    }
}