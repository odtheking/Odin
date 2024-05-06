package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.PosMessages
import me.odinmain.features.impl.dungeon.PosMessages.posMessages
import me.odinmain.utils.skyblock.modMessage

val PosMsgCommand = commodore("posmsg") {
    literal("add").runs { x: Double, y: Double, z: Double, delay: Long, message: GreedyString ->
        modMessage("Message \"${message}\" added at $x, $y, $z, with ${delay}ms delay")
        posMessages.add(PosMessages.posMessagesData(x, y, z, delay, message))
    }
    literal("remove").runs { index: Int ->
        modMessage("Removed Positional Message #$index")
        posMessages.removeAt(index-1)

    }
    literal("list")
}
