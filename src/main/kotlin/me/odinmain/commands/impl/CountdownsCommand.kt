package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.skyblock.Countdowns.countdownTriggers
import me.odinmain.features.impl.skyblock.Countdowns.CountdownTrigger
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toFixed

val CountdownsCommand = Commodore("countdowns") {
    literal("add").runs { prefix: String, time: Int, message: String ->
        countdownTriggers.add(CountdownTrigger(prefix, time, message).takeUnless { it in countdownTriggers }
            ?: return@runs modMessage("This message already exists!"))
        modMessage("$prefix${time.toFixed(divisor = 20)}, Triggers by \"$message\"")
        Config.save()
    }

    literal("remove").runs { index: Int ->
        if (countdownTriggers.size < index) return@runs modMessage("Theres no countdown trigger in position #$index")
        countdownTriggers.removeAt(index - 1)
        modMessage("Removed Countdown Trigger #$index")
        Config.save()
    }

    literal("clear").runs {
        countdownTriggers.clear()
        modMessage("Cleared List")
        Config.save()
    }

    literal("list").runs {
        var i = 0
        val output = countdownTriggers.joinToString("\n") {
            "${++i}: ${it.prefix}${it.time.toFixed(divisor = 20)}, \"${it.message}\""
        }
        modMessage(if (countdownTriggers.isEmpty()) "The list is empty!" else "Countdown Trigger list:\n$output")
    }
}