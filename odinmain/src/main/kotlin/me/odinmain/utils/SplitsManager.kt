package me.odinmain.utils

import me.odinmain.utils.skyblock.PersonalBest

data class Split(val regex: Regex, val name: String, var time: Long = 0L)
data class SplitsGroup(val splits: List<Split>, val personalBest: PersonalBest)

object SplitsManager {

    var currentSplits: SplitsGroup = SplitsGroup(emptyList(), PersonalBest("Unknown", 0))

    fun getSplitTimes(currentSplits: SplitsGroup): Pair<List<Long>, Int> {
        if (currentSplits.splits.isEmpty() || currentSplits.splits[0].time == 0L) return List(currentSplits.splits.size) { 0L } to -1
        val latestTime = if (currentSplits.splits.last().time == 0L) System.currentTimeMillis() else currentSplits.splits.last().time
        val times = MutableList(currentSplits.splits.size) { 0L }.apply { this[size - 1] = latestTime - currentSplits.splits.first().time }
        var current = currentSplits.splits.size
        for (i in 0 until currentSplits.splits.size - 1) {
            if (currentSplits.splits[i + 1].time != 0L)
                times[i] = currentSplits.splits[i + 1].time - currentSplits.splits[i].time
            else {
                current = i
                times[i] = latestTime - currentSplits.splits[i].time
                break
            }
        }
        return times to current
    }

    fun handleMessage(msg: String, splitsGroup: SplitsGroup) {
        val currentSplit = splitsGroup.splits.find { it.regex.matches(msg) } ?: return
        currentSplit.time = System.currentTimeMillis()

        val index = splitsGroup.splits.indexOf(currentSplit).takeIf { it != 0 } ?: return
        val currentSplitTime = (currentSplit.time - splitsGroup.splits[index - 1].time) / 1000.0

        splitsGroup.personalBest.time(index - 1, currentSplitTime, "s§7!", "§6${splitsGroup.splits[index - 1].name} §7took §6", addPBString = true, addOldPBString = true)
    }
}


private val entryRegexes = listOf(
    Regex("^\\[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable\\.$"),
    Regex("^\\[BOSS] Scarf: This is where the journey ends for you, Adventurers\\.$"),
    Regex("^\\[BOSS] The Professor: I was burdened with terrible news recently\\.\\.\\.$"),
    Regex("^\\[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!$"),
    Regex("^\\[BOSS] Livid: Welcome, you've arrived right on time\\. I am Livid, the Master of Shadows\\.$"),
    Regex("^\\[BOSS] Sadan: So you made it all the way here\\.\\.\\. Now you wish to defy me\\? Sadan\\?!$"),
    Regex("^\\[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!$")
)


val singlePlayerSplitGroup = mutableListOf(
    Split(Regex("aaa"), "Blood Open"),
    Split(Regex("bbb"), "Blood Clear"),
    Split(Regex("ccc"), "Boss Entry"),
    Split(Regex("ddd"), "Cleared"),
    Split(Regex("eee"), ""),
)

private val entranceSplitGroup = mutableListOf<Split>()

private val floor1SplitGroup = mutableListOf(
    Split(entryRegexes[0], "§cBonzo's Sike"),
    Split(Regex("\\[BOSS] Bonzo: Sike"), "§4Cleared"),
)

private val floor2SplitGroup = mutableListOf(
    Split(entryRegexes[1], "§cScarf's minions"),
    Split(Regex("^\\[BOSS] Scarf: Did you forget\\? I was taught by the best! Let's dance\\.$"), "§4Cleared"),
)

private val floor3SplitGroup = mutableListOf(
    Split(entryRegexes[2], "§cProfessor's Guardians"),
    Split(Regex("^\\[BOSS] The Professor: Oh\\? You found my Guardians' one weakness\\?$"), "§aThe Professor"),
    Split(Regex("^\\[BOSS] The Professor: I see\\. You have forced me to use my ultimate technique$"), "§9Professor dying"),
    Split(Regex("^\\[BOSS] The Professor: What\\?! My Guardian power is unbeatable!$"), "§4Cleared"),
)

private val floor4SplitGroup = mutableListOf(
    Split(entryRegexes[3], "§4Cleared"),
)

private val floor5SplitGroup = mutableListOf(
    Split(entryRegexes[4], "§4Cleared"),
)

private val floor6SplitGroup = mutableListOf(
    Split(entryRegexes[5], "§cTerracottas"),
    Split(Regex("^\\[BOSS] Sadan: ENOUGH!$"), "§aGiants"),
    Split(Regex("^\\[BOSS] Sadan: You did it\\. I understand now, you have earned my respect\\.$"), "§4Cleared"),
)

private val floor7SplitGroup = mutableListOf(
    Split(entryRegexes[6], "§5Maxor"),
    Split(Regex("\\[BOSS] Storm: Pathetic Maxor, just like expected\\."), "§3Storm"),
    Split(Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?"), "§6Terminals"),
    Split(Regex("The Core entrance is opening!"), "§7Goldor"),
    Split(Regex("\\[BOSS] Necron: You went further than any human before, congratulations\\."), "§cNecron"),
    Split(Regex("\\[BOSS] Necron: All this, for nothing\\.\\.\\."), "§4Cleared"),
)

val dungeonSplits = mapOf(
    0 to entranceSplitGroup,
    1 to floor1SplitGroup,
    2 to floor2SplitGroup,
    3 to floor3SplitGroup,
    4 to floor4SplitGroup,
    5 to floor5SplitGroup,
    6 to floor6SplitGroup,
    7 to floor7SplitGroup,
)
