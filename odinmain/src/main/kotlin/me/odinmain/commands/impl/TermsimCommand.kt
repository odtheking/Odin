package me.odinmain.commands.impl

import me.odinmain.commands.commodore
import me.odinmain.features.impl.floor7.p3.termsim.StartGui
import me.odinmain.features.impl.floor7.p3.termsim.openTerminal
import me.odinmain.utils.ServerUtils
import kotlin.math.round

val termSimCommand = commodore("termsim") {
    runs { ping: Long?, amount: Long? ->
        if (amount == null) StartGui.open(ping ?: 0)
        else openTerminal(ping ?: 0, amount)

    } suggests {
        listOf(round(ServerUtils.averagePing).toLong().toString())
    }
}
