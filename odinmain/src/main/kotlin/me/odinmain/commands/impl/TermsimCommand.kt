package me.odinmain.commands.impl

import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.features.impl.floor7.p3.termsim.StartGui

object TermsimCommand : Commodore {
    override val command: CommandNode =
        literal("termsim") {
            runs { StartGui.open() }
            runs { ping: Long -> StartGui.open(ping) }
        }
}
