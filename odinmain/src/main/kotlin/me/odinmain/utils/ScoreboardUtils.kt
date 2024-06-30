package me.odinmain.utils

import com.google.common.collect.ComparisonChain
import me.odinmain.OdinMain.mc
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.world.WorldSettings
import java.util.concurrent.CopyOnWriteArrayList


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

fun getLines(): List<String> {
    return mc.theWorld?.scoreboard?.run {
        getSortedScores(getObjectiveInDisplaySlot(1) ?: return emptyList())
            .filter { it?.playerName?.startsWith("#") == false }
            .let { if (it.size > 15) it.drop(15) else it }
            .map { ScorePlayerTeam.formatPlayerName(getPlayersTeam(it.playerName), it.playerName) }
    } ?: emptyList()
}


// Tablist utils

val getTabList: CopyOnWriteArrayList<Pair<NetworkPlayerInfo, String>>
    get() {
        val playerInfoList = CopyOnWriteArrayList(mc.thePlayer?.sendQueue?.playerInfoMap ?: emptyList())
        return CopyOnWriteArrayList(playerInfoList.sortedWith(tabListOrder)
            .map { Pair(it, mc.ingameGUI.tabList.getPlayerName(it)) })
    }


val tabListOrder = Comparator<NetworkPlayerInfo> { o1, o2 ->
    if (o1 == null) return@Comparator -1
    if (o2 == null) return@Comparator 0
    return@Comparator ComparisonChain.start().compareTrueFirst(
        o1.gameType != WorldSettings.GameType.SPECTATOR,
        o2.gameType != WorldSettings.GameType.SPECTATOR
    ).compare(
        o1.playerTeam?.registeredName ?: "",
        o2.playerTeam?.registeredName ?: ""
    ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
}

fun getDungeonTabList(): CopyOnWriteArrayList<Pair<NetworkPlayerInfo, String>>? {
    val tabEntries = getTabList
    if (tabEntries.size < 18 || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) return null
    return tabEntries
}
