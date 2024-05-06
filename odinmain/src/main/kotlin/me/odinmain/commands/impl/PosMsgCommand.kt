package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.commands.commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.dungeon.PosMessages.posMessageStringCancer
import me.odinmain.utils.skyblock.modMessage

val PosMsgCommand = commodore("posmsg") {
    literal("add").runs { x: Double, y: Double, z: Double, delay: Long, message: GreedyString ->
        modMessage("Message \"${message}\" added at $x, $y, $z, with ${delay}ms delay")
        val saveData = "x: ${x}, y: ${y}, z: ${z}, delay: ${delay}, message: \"${message}\""
        posMessageStringCancer.add(saveData)
        Config.save()
        //PosMessagesConfig.saveConfig()
    }

    literal("remove").runs { index: Int ->
        if (posMessageStringCancer.getOrNull(index) == null) return@runs modMessage("Theres no message in position #$index")
        modMessage("Removed Positional Message #$index")
        posMessageStringCancer.removeAt(index-1)
        Config.save()
        //PosMessagesConfig.saveConfig()
    }

    literal("clear").runs {
        modMessage("Cleared List")
        posMessageStringCancer.clear()
        Config.save()
        //PosMessagesConfig.saveConfig()
    }

    literal("list").runs {
        val output = posMessageStringCancer.joinToString(separator = "\n") {
            "${posMessageStringCancer.indexOf(it) + 1}: " + it
        }
        modMessage(if(posMessageStringCancer.isEmpty()) "Positional Message list is empty!" else "Positonal Message list:\n$output")
    }
}
