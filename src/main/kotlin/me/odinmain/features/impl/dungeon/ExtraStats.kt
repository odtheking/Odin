package me.odinmain.features.impl.dungeon

import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.event.ClickEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ExtraStats : Module(
    name = "Extra Stats",
    desc = "Shows additional dungeon stats at the end of the run in chat."
) {
    private val extraStats = PostDungeonStats()

    private fun printEndStats() {
        val defeatedText = if (extraStats.bossKilled == null) "§c§lFAILED §a- §e${DungeonUtils.dungeonTime}"
            else "§aDefeated §c${extraStats.bossKilled} §ain §e${DungeonUtils.dungeonTime}${if (extraStats.timePB) " §d§l(NEW RECORD!)" else ""}"

        modMessage(getChatBreak(), prefix = "")
        modMessage("", prefix = "")
        modMessage(getCenteredText((if (DungeonUtils.floor?.isMM == true) "§cMaster Mode" else "§cThe Catacombs") + " §r- §e${DungeonUtils.floor?.name}"), prefix = "")
        modMessage("", prefix = "")
        modMessage(getCenteredText(defeatedText), prefix = "")
        modMessage(getCenteredText("§aScore: §6${extraStats.score} §a(§b${extraStats.scoreLetter}§a${if (extraStats.scorePB) " §d§l(NEW RECORD!)" else ""})"), prefix = "")
        modMessage(getCenteredText("${extraStats.xp.firstOrNull()}"), prefix = "", chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, extraStats.xp.joinToString("\n")))
        modMessage(getCenteredText("§b${extraStats.secretsFound}§r-§6${DungeonUtils.cryptCount}§r-§c${DungeonUtils.deathCount}"), prefix = "")
        modMessage(getCenteredText(if (DungeonUtils.dungeonTeammatesNoSelf.isNotEmpty()) DungeonUtils.dungeonTeammatesNoSelf.joinToString(separator = "§r, ") { "§${it.clazz.colorCode}${it.name}" } else "§3Solo"), prefix = "")
        modMessage("", prefix = "")
        modMessage(getChatBreak(), prefix = "", chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, "Passed rooms: \n${DungeonUtils.passedRooms.joinToString("\n") { "§a${it.data.name}" }}"))
    }

    init {
        onMessage(Regex(" {29}> EXTRA STATS <")) {
            sendCommand("showextrastats")
        }

        onMessage(Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$")) {
            extraStats.timePB = it.groupValues[3].isNotEmpty()
            extraStats.bossKilled = it.groupValues[1]
        }

        onMessage(Regex("^\\s*Team Score: (\\d+) \\((.{1,2})\\)\\s?(\\(NEW RECORD!\\))?\$")) {
            extraStats.score = it.groupValues[1].toIntOrNull() ?: 0
            extraStats.scorePB = it.groupValues[3].isNotEmpty()
            extraStats.scoreLetter = it.groupValues[2]
        }

        onMessage(Regex("^\\s*(\\+[\\d,.]+\\s?\\w+ Experience)\\s?(?:\\(.+\\))?\$")) {
            extraStats.xp.add("§3${it.groupValues[1].replace("Experience", "EXP").replace("Catacombs", "Cata")}")
        }

        onMessage(Regex("^\\s*Secrets Found: (\\d+)\$")) {
            extraStats.secretsFound = it.groupValues[1].toIntOrNull() ?: 0
            printEndStats()
        }

        onWorldLoad {
            extraStats.reset()
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (DungeonUtils.inDungeons && regexes.any { it.matches(event.message.unformattedText.noControlCodes) }) event.isCanceled = true
    }

    private val regexes = listOf(
        Regex(" {29}> EXTRA STATS <"),
        Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$"),
        Regex("^\\s*Team Score: \\d+ \\(.{1,2}\\)\\s?(?:\\(NEW RECORD!\\))?\$"),
        Regex("^\\s*(\\+[\\d,.]+\\s?\\w+ Experience)\\s?(?:\\(.+\\))?\$"),
        Regex("^\\s*(Master Mode)? ?(?:The)? Catacombs - (Entrance|Floor .{1,3})\$"),
        Regex("^\\s*Secrets Found: \\d+\$"),

        Regex("^▬+\$"),
        Regex("^\\s*Total Damage as .+: [\\d,.]+\\s?(?:\\(NEW RECORD!\\))?\$"),
        Regex("^\\s*Ally Healing: [\\d,.]+\\s?(?:\\(NEW RECORD!\\))?\$"),
        Regex("^\\s*\\+0 Experience \\(No Class Milestone Reached\\)\$"),
        Regex("^\\s*The Catacombs - .+ Stats\$"),
        Regex("^\\s*Deaths: \\d+\$"),
        Regex("^\\s*Master Mode Catacombs - .+ Stats\$"),
        Regex("^\\s*Master Mode The Catacombs - .+ Stats\$"),
        Regex("^\\s*\\+(\\d+) Bits\$"),
        Regex("^\\s*Enemies Killed: \\d+\\s?(?:\\(NEW RECORD!\\))?\$")
    )

    private data class PostDungeonStats(
        var score: Int = 0,
        var scoreLetter: String? = null,
        var bossKilled: String? = null,
        var xp: MutableList<String> = mutableListOf(),
        var secretsFound: Int = 0,
        var timePB: Boolean = false,
        var scorePB: Boolean = false
    ) {
        fun reset() {
            scoreLetter = null
            bossKilled = null
            secretsFound = 0
            scorePB = false
            timePB = false
            xp.clear()
            score = 0
        }
    }
}
