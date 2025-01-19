package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.commands.commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.render.CustomHighlight.currentEntities
import me.odinmain.features.impl.render.CustomHighlight.highlightList
import me.odinmain.utils.skyblock.modMessage

val highlightCommand = commodore("highlight") {
    literal("add").runs { mob: GreedyString, hex: String? ->
        val colorRegex = Regex("^#?([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")

        val parts = mob.string.trim().split(" ")
        val color = parts.lastOrNull()?.takeIf { it.matches(colorRegex) }?.removePrefix("#")
        val mobName = if (color != null) parts.dropLast(1).joinToString(" ") else mob.string
        val lowercase = mobName.lowercase()

        if (highlightList.any { it.key == lowercase }) return@runs modMessage("$mobName is already in the highlight list.")

        modMessage("Added $mobName to the highlight list${color?.let { " with color #$it" } ?: ""}.")
        highlightList[lowercase] = color ?: ""
        Config.save()
    }

    literal("remove").runs { mob: GreedyString ->
        val lowercase = mob.string.lowercase()
        if (highlightList.none { it.key == lowercase }) return@runs modMessage("$mob isn't in the highlight list.")

        modMessage("Removed $mob from the highlight list.")
        highlightList.remove(lowercase)
        Config.save()
    }

    literal("clear").runs {
        modMessage("Highlight list cleared.")
        highlightList.clear()
        currentEntities.clear()
        Config.save()
    }

    literal("list").runs {
        if (highlightList.isEmpty()) return@runs modMessage("Highlight list is empty")
        modMessage("Highlight list:\n${highlightList.entries.joinToString("\n")}")
    }
}
