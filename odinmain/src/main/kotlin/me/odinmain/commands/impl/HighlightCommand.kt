package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.commands.commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.render.CustomHighlight.highlightList
import me.odinmain.utils.skyblock.modMessage

val highlightCommand = commodore("highlight") {
    literal("add").runs { mob: GreedyString ->
        val lowercase = mob.string.lowercase()
        if (lowercase in highlightList) return@runs modMessage("$mob is already in the highlight list.")

        modMessage("Added $mob to the highlight list.")
        highlightList.add(lowercase)
        Config.save()
    }

    literal("remove").runs { mob: GreedyString ->
        val lowercase = mob.string.lowercase()
        if (lowercase !in highlightList) return@runs modMessage("$mob isn't in the highlight list.")

        modMessage("Removed $mob from the highlight list.")
        highlightList.remove(lowercase)
        Config.save()
    }

    literal("clear").runs {
        modMessage("Highlight list cleared.")
        highlightList.clear()
        Config.save()
    }

    literal("list").runs {
        if (highlightList.isEmpty()) return@runs modMessage("Highlight list is empty")
        modMessage("Highlight list:\n${highlightList.joinToString("\n")}")
    }
}
