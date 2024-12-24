package me.odinmain.commands.impl

import me.odinmain.commands.commodore
import me.odinmain.features.impl.floor7.p3.termsim.*
import me.odinmain.utils.skyblock.modMessage

val termSimCommand = commodore("termsim") {
    runs { ping: Long? ->
        StartGui.open(ping ?: 0)
    }

    literal("type").runs { string: String, ping: Long? ->
        val ping = ping ?: 0
        when (string) {
            "start", "startwith", "startswith" -> StartsWith().open(ping)
            "order", "numbers", "n", "o"       -> InOrder.open(ping)
            "panes", "pane", "p"               -> CorrectPanes.open(ping)
            "select", "selectall"              -> SelectAll().open(ping)
            "melody", "harp", "m"              -> Melody.open(ping)
            "rubix", "r"                       -> Rubix.open(ping)
            else -> modMessage("§cInvalid terminal name: $string §5(valid: pane, rubix, order, start, select, melody)")
        }
    } suggests {
        listOf("pane", "rubix", "order", "start", "select", "melody")
    }
}
