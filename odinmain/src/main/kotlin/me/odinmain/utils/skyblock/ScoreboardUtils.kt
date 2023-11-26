package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.noControlCodes
import net.minecraft.scoreboard.ScorePlayerTeam


fun cleanSB(scoreboard: String?): String {
    return scoreboard.noControlCodes.filter { it.code in 21..126 }
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