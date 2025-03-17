package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinmain.config.Config
import me.odinmain.features.impl.render.VisualWords
import me.odinmain.utils.skyblock.modMessage

val visualWordsCommand = Commodore("visualwords") {

    literal("add").runs { command: GreedyString ->
        if (!command.string.contains("replace")) return@runs modMessage("You are missing the 'replace' keyword")
        val actualText = command.string.replace("&", "§").substringBefore("replace").trim()
        val replaceText = command.string.replace("&", "§").substringAfter("replace").trim()
        val replaceTextForChat = "\"${actualText.substring(0, actualText.length / 2)}⛏${actualText.substring(actualText.length / 2)}\""
        VisualWords.wordsMap[actualText] = replaceText
        modMessage("Replacing $replaceTextForChat with \"$replaceText\"")
        Config.save()
    }

    literal("remove").runs { command: GreedyString ->
        val actualText = command.string.replace("&", "§").substringBefore("replace").trim()
        if (!VisualWords.wordsMap.containsKey(actualText)) return@runs modMessage("This element is not in the list")
        VisualWords.wordsMap.remove(actualText)
        modMessage("Removed \"$actualText\" from the list")
        Config.save()
    }

    literal("clear").runs {
        VisualWords.wordsMap.clear()
        modMessage("Visual Word list cleared")
        Config.save()
    }

    literal("list").runs {
        if (VisualWords.wordsMap.isEmpty()) return@runs modMessage("Visual Word list is empty")
        for (actualText in VisualWords.wordsMap.keys) {
            val replaceText = VisualWords.wordsMap[actualText]
            val actualTextForChat = "\"${actualText.substring(0, actualText.length / 2)}⛏${actualText.substring(actualText.length / 2)}\""
            modMessage("$actualTextForChat -> \"$replaceText\"")
        }
    }

}