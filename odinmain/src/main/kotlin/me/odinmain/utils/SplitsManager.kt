package me.odinmain.utils

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.SkyblockJoinIslandEvent
import me.odinmain.features.impl.skyblock.Splits
import me.odinmain.features.impl.skyblock.Splits.sendSplits
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

data class Split(val regex: Regex, val name: String, var time: Long = 0L)
data class SplitsGroup(val splits: List<Split>, val personalBest: PersonalBest?)

object SplitsManager {

    var currentSplits: SplitsGroup = SplitsGroup(emptyList(), null)

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        val currentSplit = currentSplits.splits.find { it.regex.matches(event.message) } ?: return
        currentSplit.time = System.currentTimeMillis()

        val index = currentSplits.splits.indexOf(currentSplit).takeIf { it != 0 } ?: return
        val currentSplitTime = (currentSplit.time - currentSplits.splits[index - 1].time) / 1000.0

        currentSplits.personalBest?.time(index - 1, currentSplitTime, "s§7!", "§6${currentSplits.splits[index - 1].name} §7took §6", addPBString = true, addOldPBString = true, alwaysSendPB = true, sendOnlyPB = Splits.sendOnlyPB, sendMessage = Splits.enabled)

        if (index == currentSplits.splits.size - 1) {
            currentSplits.personalBest?.time(index, currentSplitTime, "s§7!", "§6Total time §7took §6", addPBString = true, addOldPBString = true, alwaysSendPB = true, sendOnlyPB = Splits.sendOnlyPB, sendMessage = Splits.enabled)

            getAndUpdateSplitsTimes(currentSplits).first.forEachIndexed { i, it ->
                val timeString = formatTime(it)
                val name = if (i == currentSplits.splits.size - 1) "Total" else currentSplits.splits[i].name
                if (sendSplits && Splits.enabled) modMessage("§6$name §7took §6$timeString §7to complete.")
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentSplits = SplitsGroup(mutableListOf(), null)
    }

    @SubscribeEvent
    fun onJoinSkyblockIsland(event: SkyblockJoinIslandEvent) {
        val currentSplits = initializeSplits(event.island) ?: return
        if (Splits.enabled) modMessage("Loading splits for ${LocationUtils.currentArea.name}")
        SplitsManager.currentSplits = currentSplits
    }

    private fun initializeSplits(island: Island): SplitsGroup? {
        return when (island) {
            Island.SinglePlayer -> SplitsGroup(singlePlayerSplitGroup, singlePlayerPBs)

            Island.Dungeon -> {
                val split = dungeonSplits[DungeonUtils.floor.floorNumber].toMutableList()

                split.add(0, Split(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\."), "§2Blood Open"))
                split.add(1, Split(Regex("The BLOOD DOOR has been opened!"), "§bBlood Clear"))
                split.add(2, Split(Regex("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\."), "§dBoss Entry"))
                split.add(Split(Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$"), "§1Total"))

                SplitsGroup(split, DungeonUtils.floor.personalBest)
            }

            Island.Kuudra -> {
                when (LocationUtils.kuudraTier) {
                    5 -> SplitsGroup(kuudraT5SplitsGroup, kuudraT5PBs)
                    4 -> SplitsGroup(kuudraSplitsGroup, kuudraT4PBs)
                    3 -> SplitsGroup(kuudraSplitsGroup, kuudraT3PBs)
                    2 -> SplitsGroup(kuudraSplitsGroup, kuudraT2PBs)
                    1 -> SplitsGroup(kuudraSplitsGroup, kuudraT1PBs)
                    else -> null
                }
            }
            else -> null
        }
    }

    fun getAndUpdateSplitsTimes(currentSplits: SplitsGroup): Pair<List<Long>, Int> {
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
}

private val singlePlayerPBs = PersonalBest("SinglePlayer", 5)
private val kuudraT5PBs = PersonalBest("KuudraT5", 6)
private val kuudraT4PBs = PersonalBest("KuudraT4", 5)
private val kuudraT3PBs = PersonalBest("KuudraT3", 5)
private val kuudraT2PBs = PersonalBest("KuudraT2", 5)
private val kuudraT1PBs = PersonalBest("KuudraT1", 5)

val kuudraT5SplitsGroup = mutableListOf(
    Split(Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$"), "§2Supplies"),
    Split(Regex("^\\[NPC] Elle: OMG! Great work collecting my supplies!$"), "§bBuild"),
    Split(Regex("^\\[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!$"), "§dEaten"),
    Split(Regex("^(?!Elle has been eaten by Kuudra!\$)(.{1,16}) has been eaten by Kuudra!$"), "§cStun"),
    Split(Regex("^(.{1,16}) destroyed one of Kuudra's pods!\$"), "§4Cleared"),
    Split(Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$"), "Total"))

val kuudraSplitsGroup = mutableListOf(
    Split(Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$"), "§2Supplies"),
    Split(Regex("^\\[NPC] Elle: OMG! Great work collecting my supplies!$"), "§bBuild"),
    Split(Regex("^\\[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!$"), "§cStun"),
    Split(Regex("^\\[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!\$"), "§4Cleared"),
    Split(Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$"), "Total"))

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

val dungeonSplits = listOf(
     entranceSplitGroup,
     floor1SplitGroup,
    floor2SplitGroup,
     floor3SplitGroup,
     floor4SplitGroup,
    floor5SplitGroup,
   floor6SplitGroup,
     floor7SplitGroup,
)
