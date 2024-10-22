package me.odinmain.utils

import me.odinmain.OdinMain.mc
import net.minecraft.scoreboard.ScorePlayerTeam

fun cleanSB(scoreboard: String?): String {
    return scoreboard.noControlCodes.filter { it.code in 21..126 }
}

/**
 * Retrieves a list of strings representing lines on the sidebar of the Minecraft scoreboard.
 *
 * This property returns a list of player names or formatted entries displayed on the sidebar of the Minecraft scoreboard.
 * It filters out entries starting with "#" and limits the result to a maximum of 15 lines. The player names are formatted
 * based on their team affiliation using the ScorePlayerTeam class.
 *
 * @return A list of strings representing lines on the scoreboard sidebar. Returns an empty list if the scoreboard or
 * objective is not available, or if the list is empty after filtering.
 */
val sidebarLines: List<String>
    get() {
        val scoreboard = mc.theWorld?.scoreboard ?: return emptyList()
        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()

        return scoreboard.getSortedScores(objective)
            .filter { it?.playerName?.startsWith("#") == false }
            .let { if (it.size > 15) it.drop(15) else it }
            .map { ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(it.playerName), it.playerName) }
    }

fun cleanLine(scoreboard: String): String = scoreboard.noControlCodes.filter { it.code in 32..126 }

// Tablist utils

val getTabList: List<String>
    get() {
        val playerInfoMap = mc.thePlayer?.sendQueue?.playerInfoMap ?: return emptyList()
        return playerInfoMap.toMutableList().map { mc.ingameGUI.tabList.getPlayerName(it) }
    }