package me.odinclient.utils.skyblock

import me.odinclient.OdinClient.Companion.mc
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.StringUtils.stripControlCodes

object ScoreboardUtils {

    fun cleanSB(scoreboard: String?): String {
        return stripControlCodes(scoreboard).toCharArray().filter { it.code in 21..126 }.joinToString("")
    }

    val sidebarLines: List<String>
        get() {
            val scoreboard = mc.theWorld?.scoreboard ?: return emptyList()
            val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()

            return scoreboard.getSortedScores(objective)
                .filter { it?.playerName?.startsWith("#") == false }
                .let { if (it.size > 15) it.drop(15) else it }
                .map { ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(it.playerName), it.playerName) }
        }
}