package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.matchesOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.event.ClickEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ExtraStats : Module(
    name = "Extra Stats",
    description = "Shows additional dungeon stats at the end of the run in chat.",
    category = Category.DUNGEON,
) {
    private val extraStats = PostDungeonStats()

    private fun printEndStats() {
        val dungeonMessage = if (DungeonUtils.floor.isInMM) "§cMaster Mode" else "§cThe Catacombs"

        val defeatedText = if (extraStats.bossKilled == null) "§c§lFAILED §a- §e${DungeonUtils.dungeonTime}" else
            "§aDefeated §c${extraStats.bossKilled} §ain §e${DungeonUtils.dungeonTime}${if (extraStats.timePB) " §d§l(NEW RECORD!)" else ""}"

        val scoreMessage = "§aScore: §6${extraStats.score} §a(§b${extraStats.scoreLetter}§a${if (extraStats.scorePB) " §d§l(NEW RECORD!)" else ""})"

        modMessage(getChatBreak(), prefix = false)
        modMessage("", prefix = false)
        modMessage(getCenteredText(dungeonMessage + " §r- §e${DungeonUtils.floor.name}"), prefix = false)
        modMessage("", prefix = false)
        modMessage(getCenteredText(defeatedText), prefix = false)
        modMessage(getCenteredText(scoreMessage), prefix = false)
        modMessage(getCenteredText("${extraStats.xp.firstOrNull()}"), prefix = false, chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, extraStats.xp.joinToString("\n")))
        modMessage(getCenteredText("§b${extraStats.secretsFound}§r-§6${DungeonUtils.cryptCount}§r-§c${DungeonUtils.deathCount}"), prefix = false)
        modMessage(getCenteredText(if (DungeonUtils.dungeonTeammatesNoSelf.isNotEmpty()) DungeonUtils.dungeonTeammatesNoSelf.joinToString(separator = "§r, ") { "§${it.clazz.colorCode}${it.name}" } else "§3Solo"), prefix = false)
        modMessage("", prefix = false)
        modMessage(getChatBreak(), prefix = false, chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, "Passed rooms: \n${DungeonUtils.passedRooms.joinToString("\n") { "§a${it.room.data.name}" }}"))
    }

    init {
        onMessage(Regex(" {29}> EXTRA STATS <")) {
            sendCommand("showextrastats")
        }

        onMessage(Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$")) {
            Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$").matchEntire(it)?.let {
                extraStats.bossKilled = it.groupValues[1]
                extraStats.timePB = it.groupValues[3].isNotEmpty()
            }
        }

        onMessage(Regex("^\\s*Team Score: (\\d+) \\((.{1,2})\\)\\s?(\\(NEW RECORD!\\))?\$")) { event ->
            Regex("^\\s*Team Score: (\\d+) \\((.{1,2})\\)\\s?(\\(NEW RECORD!\\))?\$").matchEntire(event)?.let {
                extraStats.score = it.groupValues[1].toIntOrNull() ?: 0
                extraStats.scoreLetter = it.groupValues[2]
                extraStats.scorePB = it.groupValues[3].isNotEmpty()
            }
        }

        onMessage(Regex("^\\s*(\\+[\\d,.]+\\s?\\w+ Experience)\\s?(?:\\(.+\\))?\$")) { event ->
            Regex("^\\s*(\\+[\\d,.]+\\s?\\w+ Experience)\\s?(?:\\(.+\\))?\$").matchEntire(event)?.let { it.groupValues[1] }
        }

        onMessage(Regex("^\\s*Secrets Found: (\\d+)\$")) { event ->
           Regex("^\\s*Secrets Found: (\\d+)\$").matchEntire(event)?.let {
                extraStats.secretsFound = it.groupValues[1].toIntOrNull() ?: 0
                printEndStats()
            }
        }

        onWorldLoad {
            extraStats.reset()
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (DungeonUtils.inDungeons && event.message.unformattedText.noControlCodes.matchesOneOf(
                Regex(" {29}> EXTRA STATS <"),
                Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$"),
                Regex("^\\s*Team Score: \\d+ \\(.{1,2}\\)\\s?(?:\\(NEW RECORD!\\))?\$"),
                Regex("^\\s*(\\+[\\d,.]+\\s?\\w+ Experience)\\s?(?:\\(.+\\))?\$"),
                Regex("^\\s*(Master Mode)?(?:The)? Catacombs - (Entrance|Floor .{1,3})\$"),
                Regex("^\\s*Secrets Found: \\d+\$"),

                Regex("^▬+\$"),
                Regex("^\\s*Total Damage as .+: [\\d,.]+\\s?(?:\\(NEW RECORD!\\))?\$"),
                Regex("^\\s*Ally Healing: [\\d,.]+\\s?(?:\\(NEW RECORD!\\))?\$"),
                Regex("^\\s*\\+0 Experience \\(No Class Milestone Reached\\)\$"),
                Regex("^\\s*The Catacombs - .+ Stats\$"),
                Regex("^\\s*Deaths: \\d+\$"),
                Regex("^\\s*Master Mode Catacombs - .+ Stats\$"),
                Regex("^\\s*\\+(\\d+) Bits\$"),
                Regex("^\\s*Enemies Killed: \\d+\\s?(?:\\(NEW RECORD!\\))?\$"),)
            ) event.isCanceled = true
    }

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
            score = 0
            scoreLetter = null
            bossKilled = null
            xp.clear()
            secretsFound = 0
            timePB = false
            scorePB = false
        }
    }
}
