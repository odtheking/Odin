package me.odinmain.commands.impl

import codes.som.anthony.koffee.types.double
import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.OdinMain.mc
import me.odinmain.commands.commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.dungeon.PosMessages.posMessageStrings
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.modMessage

val PosMsgCommand = commodore("posmsg") {
    literal("add") {
        literal("at").runs { x: Double, y: Double, z: Double, delay: Long, distance: Double, message: GreedyString ->
            val saveData = "x: ${x}, y: ${y}, z: ${z}, delay: ${delay}, distance: ${distance}, message: \"${message}\""
            if (posMessageStrings.contains(saveData)) modMessage("This message already exists!")
            modMessage("Message \"${message}\" added at $x, $y, $z, with ${delay}ms delay, triggered up to $distance blocks away.")
            posMessageStrings.add(saveData)
            Config.save()
        }
        literal("in").runs { x: Double, y: Double, z: Double, x2: Double, y2: Double, z2: Double, delay: Long, message: GreedyString ->
            val saveData = "x: ${x}, y: ${y}, z: ${z}, x2: ${x2}, y2: ${y2}, z2: ${z2}, delay: ${delay}, message: \"${message}\""
            if (posMessageStrings.contains(saveData)) modMessage("This message already exists!")
            modMessage("Message \"${message}\" added in $x, $y, $z, $x2, $y2, $z2, with ${delay}ms delay.")
            posMessageStrings.add(saveData)
            Config.save()
        }
        literal("atself").runs { delay: Long, distance: Double, message: GreedyString ->
            val saveData = "x: ${mc.thePlayer.posX.round(2)}, y: ${mc.thePlayer.posY.round(2)}, z: ${mc.thePlayer.posZ.round(2)}, delay: ${delay}, distance: ${distance}, message: \"${message}\""
            if (posMessageStrings.contains(saveData)) modMessage("This message already exists!")
            modMessage("Message \"${message}\" added at ${mc.thePlayer.posX.round(2)}, ${mc.thePlayer.posY.round(2)}, ${mc.thePlayer.posZ.round(2)}, with ${delay}ms delay, triggered up to $distance blocks away.")
            posMessageStrings.add(saveData)
            Config.save()
        }
    }

    literal("remove").runs { index: Int ->
        if (posMessageStrings.getOrNull(index-1) == null) return@runs modMessage("Theres no message in position #$index")
        modMessage("Removed Positional Message #$index")
        posMessageStrings.removeAt(index-1)
        Config.save()
    }

    literal("clear").runs {
        modMessage("Cleared List")
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
