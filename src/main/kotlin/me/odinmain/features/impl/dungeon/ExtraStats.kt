package me.odinmain.features.impl.dungeon

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.features.Module
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.formatNumber
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.event.ClickEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ExtraStats : Module(
    name = "Extra Stats",
    description = "Shows additional dungeon stats at the end of the run in chat."
) {
    private val extraStats = PostDungeonStats()
    private val showBits by BooleanSetting("Show Bits", true, desc = "Show bits earned.")
    private val showClassEXP by BooleanSetting("Show Class EXP", true, desc = "Show class experience.")
    private val showCombatStats by BooleanSetting("Show Combat Stats", true, desc = "Show damage, enemy kills and healing.")
    private val teamStats by SelectorSetting("Show Team Stats", "both", options = arrayListOf("Off", "Personal", "Team", "Both"), desc = "Toggle how show team stats.")
    private val showTeammates by BooleanSetting("Show Teammates", false, desc = "Show teammates.")

    private fun printEndStats() {
        val defeatedText = if (extraStats.bossKilled == null) "§c§lFAILED §a- §e${DungeonUtils.dungeonTime}"
            else "§aDefeated §c${extraStats.bossKilled} §ain §e${DungeonUtils.dungeonTime}${if (extraStats.timePB) " §d§l(NEW RECORD!)" else ""}"

        modMessage(getChatBreak(), prefix = "")
        modMessage("", prefix = "")
        modMessage(getCenteredText((if (DungeonUtils.floor?.isMM == true) "§cMaster Mode" else "§cThe Catacombs") + " §r- §e${DungeonUtils.floor?.name}"), prefix = "")
        modMessage("", prefix = "")
        modMessage(getCenteredText(defeatedText), prefix = "")
        modMessage(getCenteredText("§aScore: §6${extraStats.score} §a(§b${extraStats.scoreLetter}§a)${if (extraStats.scorePB) " §d§l(NEW RECORD!)" else ""}${if (extraStats.bits!=null && showBits) "    §b${extraStats.bits}" else ""}"), prefix = "")
        modMessage(getCenteredText("${extraStats.xp.firstOrNull()}${if (showClassEXP) "  ${extraStats.xp.getOrNull(1)}" else "" }"), prefix = "", chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, extraStats.xp.joinToString("\n")))
        if (showCombatStats) modMessage(getCenteredText("§e${extraStats.damage}§r-§b${extraStats.enemyKill}§r-§a${extraStats.heal}"), prefix = "", chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, extraStats.combatStats.joinToString("\n")))

        if (teamStats != 0)
            modMessage(getCenteredText(
                (if (teamStats==1 || teamStats==3) "§b${extraStats.secretsFound}§r-§c${extraStats.deaths}" else "") + (if (teamStats==3) " §r/ " else "") +
                        (if (teamStats==2 || teamStats==3) "§b${DungeonUtils.secretCount}§r-§6${DungeonUtils.cryptCount}§r-§c${DungeonUtils.deathCount}" else "")
            ), prefix = "" , chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, extraStats.skillStats.joinToString("\n")))

        if (showTeammates) modMessage(getCenteredText(if (DungeonUtils.dungeonTeammatesNoSelf.isNotEmpty()) DungeonUtils.dungeonTeammatesNoSelf.joinToString(separator = "§r, ") { "§${it.clazz.colorCode}${it.name}" } else "§3Solo"), prefix = "")
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

        onMessage(Regex("^\\s*(\\+\\d+ Bits)\$")) {
            extraStats.bits = it.groupValues[1]
        }

        onMessage(Regex("^\\s*(Total Damage as .+: ([\\d,.]+)\\s?(\\(NEW RECORD!\\))?)\$")) {
            extraStats.damagePB = it.groupValues[3].isNotEmpty()
            extraStats.damage = formatNumber(it.groupValues[2]) + if (extraStats.damagePB) "(NEW PB!)" else ""
            extraStats.combatStats.add("§e${it.groupValues[1]}")
        }

        onMessage(Regex("^\\s*(Ally Healing: ([\\d,.]+)\\s?(\\(NEW RECORD!\\))?)\$")) {
            extraStats.healPB = it.groupValues[3].isNotEmpty()
            extraStats.heal = formatNumber(it.groupValues[2]) + if (extraStats.healPB) "(NEW PB!)" else ""
            extraStats.combatStats.add("§a${it.groupValues[1]}")
        }

        onMessage(Regex("^\\s*(Enemies Killed: (\\d+)\\s?(\\(NEW RECORD!\\))?)\$")) {
            extraStats.enemyKillPB = it.groupValues[3].isNotEmpty()
            extraStats.enemyKill = (it.groupValues[2].toIntOrNull()?:0).toString() + if (extraStats.enemyKillPB) "(NEW PB!)" else ""
            extraStats.combatStats.add(1,"§b${it.groupValues[1]}")
        }

        onMessage(Regex("^\\s*(Deaths: (\\d+))\$")) {
            extraStats.deaths = it.groupValues[2].toIntOrNull() ?: 0
            if (teamStats == 1 || teamStats == 3) extraStats.skillStats.add("§c${it.groupValues[1]}")
        }

        onMessage(Regex("^\\s*(Secrets Found: (\\d+))\$")) {
            extraStats.secretsFound = it.groupValues[2].toIntOrNull() ?: 0
            if (teamStats == 1 || teamStats == 3) extraStats.skillStats.add(0,"§b"+it.groupValues[1])
            if (teamStats == 3) extraStats.skillStats.add("")
            if (teamStats==2 || teamStats==3) {
                extraStats.skillStats.add("§bTeam Secrets Found: ${DungeonUtils.secretCount}")
                extraStats.skillStats.add("§6Crypt: ${DungeonUtils.cryptCount}")
                extraStats.skillStats.add("§cTeam Deaths: ${DungeonUtils.deathCount}")
            }
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
        var timePB: Boolean = false,
        var scorePB: Boolean = false,
        var combatStats: MutableList<String> = mutableListOf(),
        var damage: String? = "0",
        var damagePB: Boolean = false,
        var enemyKill : String? = "0",
        var enemyKillPB : Boolean = false,
        var heal : String? = "0",
        var healPB : Boolean = false,
        var skillStats: MutableList<String> = mutableListOf(),
        var deaths: Int =0,
        var secretsFound: Int = 0,
        var bits : String? =null
    ) {
        fun reset() {
            scoreLetter = null
            bossKilled = null
            secretsFound = 0
            scorePB = false
            timePB = false
            xp.clear()
            score = 0
            deaths = 0
            heal = "0"
            healPB = false
            damage = "0"
            damagePB = false
            enemyKill = "0"
            enemyKillPB = false
            bits = null
            skillStats.clear()
            combatStats.clear()
        }
    }
}
