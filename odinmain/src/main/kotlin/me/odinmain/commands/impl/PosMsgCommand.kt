package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.commands.commodore
import me.odinmain.config.PosMessagesConfig
import me.odinmain.config.PosMessagesConfig.PosMessage
import me.odinmain.config.PosMessagesConfig.PosMessages
import me.odinmain.utils.skyblock.modMessage

val PosMsgCommand = commodore("posmsg") {
    literal("add").runs { x: Double, y: Double, z: Double, delay: Long, message: GreedyString ->
        modMessage("Message \"${message}\" added at $x, $y, $z, with ${delay}ms delay")
        PosMessages.add(PosMessage(x, y, z, delay, message.string))
        PosMessagesConfig.saveConfig()
    }

    literal("remove").runs { index: Int ->
        if (PosMessages.getOrNull(index) == null) return@runs modMessage("Theres no message in position #$index")
        modMessage("Removed Positional Message #$index")
        PosMessages.removeAt(index-1)
        PosMessagesConfig.saveConfig()
    }

    literal("clear").runs {
        modMessage("Cleared List")
        PosMessages.clear()
        PosMessagesConfig.saveConfig()
    }

    literal("list").runs {
        val output = PosMessages.joinToString(separator = "\n") {
            "${PosMessages.indexOf(it) + 1}: x: ${it.x}, y: ${it.y}, z: ${it.z}, delay: ${it.delay}, message: ${it.message}"
        }
        modMessage(if(PosMessages.isEmpty()) "Positional Message list is empty!" else "Positonal Message list:\n$output")
    }
}
