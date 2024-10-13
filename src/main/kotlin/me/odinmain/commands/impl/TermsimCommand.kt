package me.odinmain.commands.impl

import me.odinmain.commands.commodore
import me.odinmain.features.impl.floor7.p3.termsim.*
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.getRandom
import net.minecraft.item.EnumDyeColor
import kotlin.math.round

val termSimCommand = commodore("termsim") {
    runs { ping: Long?, amount: Long? ->
        if (amount == null) StartGui.open(ping ?: 0)
        else openTerminal(ping ?: 0, amount)

    } suggests {
        listOf(round(ServerUtils.averagePing).toLong().toString())
    }

    runs{ string: String, ping: Long? ->
        when (string) {
            "pains" -> CorrectPanes.open(ping ?: 0, 1)
            "rubix" -> Rubix.open(ping ?: 0, 1)
            "order" -> InOrder.open(ping ?: 0, 1)
            "sw" -> StartsWith(StartsWith.letters.shuffled().first()).open(ping ?: 0, 1)
            "select" -> SelectAll(EnumDyeColor.entries.getRandom().name.replace("_", " ").uppercase()).open(ping ?: 0, 1)
        }
    }
}
