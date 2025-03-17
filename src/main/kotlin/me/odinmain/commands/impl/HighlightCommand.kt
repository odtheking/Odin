package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.commands.commodore
import me.odinmain.config.Config
import me.odinmain.features.impl.render.CustomHighlight.currentEntities
import me.odinmain.features.impl.render.CustomHighlight.highlightList
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.modMessage

@OptIn(ExperimentalStdlibApi::class)
val highlightCommand = Commodore("highlight") {
    val colorRegex = Regex("^(.*?)(?:\\s+#?([0-9a-fA-F]{6}|[0-9a-fA-F]{8}))?$")

    literal("add").runs { input: GreedyString ->
        val inputString = input.string.trim()
        val matchResult = colorRegex.matchEntire(inputString) ?: return@runs modMessage("Invalid format. Use: /highlight add <mob name> [#hexcolor]")

        val (mobName, colorCode) = matchResult.destructured
        val mobNameTrimmed = mobName.trim()
        val lowercase = mobNameTrimmed.lowercase()

        if (mobNameTrimmed.isEmpty()) return@runs modMessage("Mob name cannot be empty.")

        if (highlightList.any { it.key == lowercase }) return@runs modMessage("$mobNameTrimmed is already in the highlight list.")

        if (colorCode.isNotEmpty() && !Regex("^[0-9a-fA-F]{6}|[0-9a-fA-F]{8}$").matches(colorCode)) return@runs modMessage("Invalid color format. Use #RRGGBB or #RRGGBBAA.")

        val color = if (colorCode.isNotEmpty()) {
            try {
                Color(colorCode.padEnd(8, 'f'))
            } catch (e: Exception) {
                modMessage("Invalid color format. Use #RRGGBB or #RRGGBBAA.")
                null
            }
        } else null

        highlightList[lowercase] = color
        modMessage("Added $mobNameTrimmed to the highlight list${if (colorCode.isNotEmpty()) " with color #$colorCode" else ""}.")
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
        modMessage("Highlight list:\n${highlightList.entries.joinToString("\n") {
            "${it.key} - ${it.value?.rgba?.toHexString() ?: "default color"}"
        }}")
    }
}