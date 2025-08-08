package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.config.Config
import me.odinmain.features.impl.skyblock.Countdowns.countdownTriggers
import me.odinmain.features.impl.skyblock.Countdowns.CountdownTrigger
import me.odinmain.utils.addOrNull
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toFixed

val CountdownsCommand = Commodore("countdowns") {
    literal("add").runs { prefix: String, time: Int, message: GreedyString ->
        val prefix = prefix.replace("&", "§")
        countdownTriggers.addOrNull(CountdownTrigger(prefix.replace("&", "§"), time, false, message.string))
            ?: return@runs modMessage("This thing already exists!")
        modMessage("$prefix${time.toFixed(divisor = 20)}, Triggers by \"$message\"")
        Config.save()
    }

    literal("addregex").runs { prefix: String, time: Int, message: GreedyString ->
        val prefix = prefix.replace("&", "§")
        runCatching {
            Regex(message.string)
        }.onSuccess {
            countdownTriggers.addOrNull(CountdownTrigger(prefix, time, true, message.string))
                ?: return@runs modMessage("This thing already exists!")
            modMessage("$prefix${time.toFixed(divisor = 20)}, Triggers by regex \"$message\"")
            Config.save()
        }.onFailure {
            modMessage("Bad regex!")
        }
    }

    literal("remove").runs { index: Int ->
        runCatching {
            countdownTriggers.removeAt(index)
        }.onSuccess {
            modMessage("Removed Countdown Trigger #$index")
            Config.save()
        }.onFailure {
            modMessage("Theres no countdown trigger in position #$index")
        }
    }

    literal("clear").runs {
        countdownTriggers.clear()
        modMessage("Cleared List")
        Config.save()
    }

    literal("list").runs {
        val output = countdownTriggers.withIndex().joinToString("\n") { (i, it) ->
            "$i: ${it.prefix}${it.time.toFixed(divisor = 20)}&r, ${if (it.regex) "regex" else "normal"} \"${it.message}\""
        }
        modMessage(if (countdownTriggers.isEmpty()) "The list is empty!" else "Countdown Trigger list:\n$output")
    }
}
