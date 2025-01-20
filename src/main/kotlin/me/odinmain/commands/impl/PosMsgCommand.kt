package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.OdinMain.mc
import me.odinmain.config.Config
import me.odinmain.features.impl.dungeon.PosMessages
import me.odinmain.features.impl.dungeon.PosMessages.findParser
import me.odinmain.features.impl.dungeon.PosMessages.parsedStrings
import me.odinmain.features.impl.dungeon.PosMessages.posMessageStrings
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.modMessage

val PosMsgCommand = Commodore("posmsg") {
    literal("add") {
        literal("at").runs { x: Double, y: Double, z: Double, delay: Long, distance: Double, message: GreedyString ->
            val saveData = "x: ${x}, y: ${y}, z: ${z}, delay: ${delay}, distance: ${distance}, message: \"${message}\""
            if (posMessageStrings.contains(saveData)) return@runs modMessage("This message already exists!")
            modMessage("Message \"${message}\" added at $x, $y, $z, with ${delay}ms delay, triggered up to $distance blocks away.")
            parsedStrings.add(PosMessages.PosMessage(x, y, z, null, null, null, delay, distance, message.string))
            posMessageStrings.add(saveData)
            Config.save()
        }
        literal("in").runs { x: Double, y: Double, z: Double, x2: Double, y2: Double, z2: Double, delay: Long, message: GreedyString ->
            val saveData = "x: ${x}, y: ${y}, z: ${z}, x2: ${x2}, y2: ${y2}, z2: ${z2}, delay: ${delay}, message: \"${message}\""
            if (posMessageStrings.contains(saveData)) return@runs modMessage("This message already exists!")
            modMessage("Message \"${message}\" added in $x, $y, $z, $x2, $y2, $z2, with ${delay}ms delay.")
            parsedStrings.add(PosMessages.PosMessage(x, y, z, x2, y2, z2, delay, null, message.string))
            posMessageStrings.add(saveData)
            Config.save()
        }
        literal("atself").runs { delay: Long, distance: Double, message: GreedyString ->
            val x = mc.thePlayer.posX.round(2).toDouble()
            val y = mc.thePlayer.posY.round(2).toDouble()
            val z = mc.thePlayer.posZ.round(2).toDouble()
            val saveData = "x: ${x}, y: ${y}, z: ${z}, delay: ${delay}, distance: ${distance}, message: \"${message}\""
            if (posMessageStrings.contains(saveData)) return@runs modMessage("This message already exists!")
            modMessage("Message \"${message}\" added at ${x}, ${y}, ${z}, with ${delay}ms delay, triggered up to $distance blocks away.")
            parsedStrings.add(PosMessages.PosMessage(x, y, z, null, null, null, delay, distance, message.string))
            posMessageStrings.add(saveData)
            Config.save()
        }
    }

    literal("remove").runs { index: Int ->
        val message = posMessageStrings.getOrNull(index-1) ?: return@runs modMessage("Theres no message in position #$index")
        val posMessage = findParser(message, false)
        parsedStrings.remove(posMessage)
        modMessage("Removed Positional Message #$index")
        posMessageStrings.removeAt(index-1)
        Config.save()
    }

    literal("clear").runs {
        modMessage("Cleared List")
        parsedStrings.clear()
        posMessageStrings.clear()
        Config.save()
    }

    literal("list").runs {
        val output = posMessageStrings.joinToString(separator = "\n") {
            "${posMessageStrings.indexOf(it) + 1}: " + it
        }
        modMessage(if(posMessageStrings.isEmpty()) "Positional Message list is empty!" else "Positonal Message list:\n$output")
    }
}
