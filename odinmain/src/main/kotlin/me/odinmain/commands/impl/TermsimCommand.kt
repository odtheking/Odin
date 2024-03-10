package me.odinmain.commands.impl

import me.odinmain.commands.commodore
import me.odinmain.features.impl.floor7.p3.termsim.StartGui

val termSimCommand = commodore("termsim") {
    runs { ping: Long? ->
        StartGui.open(ping ?: 0)
    }
}
