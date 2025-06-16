package me.odinmain.utils

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.impl.skyblock.Splits
import me.odinmain.features.impl.skyblock.Splits.sendSplits
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonListener
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

data class Split(val regex: Regex, val name: String, var time: Long = 0L)
data class SplitsGroup(val splits: List<Split>, val personalBest: PersonalBest?)

object SplitsManager {

    var currentSplits: SplitsGroup = SplitsGroup(emptyList(), null)

    @SubscribeEvent(receiveCanceled = true)
    fun onChatPacket(event: ChatPacketEvent) {
        val currentSplit = currentSplits.splits.find { it.regex.matches(event.message) } ?: return
        if (currentSplit.time != 0L) return
        currentSplit.time = System.currentTimeMillis()

        val index = currentSplits.splits.indexOf(currentSplit).takeIf { it != 0 } ?: return
        val currentSplitTime = (currentSplit.time - currentSplits.splits[index - 1].time) / 1000.0

        if (index == currentSplits.splits.size - 1) {
            val (times, _) = getAndUpdateSplitsTimes(currentSplits)
            runIn(10) {
                currentSplits.personalBest?.time(index - 1, currentSplitTime, "s§7!", "§6${currentSplits.splits[index - 1].name} §7took §6", addPBString = true, addOldPBString = true, alwaysSendPB = true, sendOnlyPB = Splits.sendOnlyPB, sendMessage = Splits.enabled)
                currentSplits.personalBest?.time(index, times.last() / 1000.0, "s§7!", "§6Total time §7took §6", addPBString = true, addOldPBString = true, alwaysSendPB = true, sendOnlyPB = Splits.sendOnlyPB, sendMessage = Splits.enabled)
                times.forEachIndexed { i, it ->
                    val name = if (i == currentSplits.splits.size - 1) "Total" else currentSplits.splits.getSafe(i)?.name
                    if (sendSplits && Splits.enabled) modMessage("§6$name §7took §6${formatTime(it)} §7to complete.")
                }
            }
        } else currentSplits.personalBest?.time(index - 1, currentSplitTime, "s§7!", "§6${currentSplits.splits[index - 1].name} §7took §6", addPBString = true, addOldPBString = true, alwaysSendPB = true, sendOnlyPB = Splits.sendOnlyPB, sendMessage = Splits.enabled)
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChat(event: ChatPacketEvent) {
        if (event.message != "Starting in 1 second.") return

        currentSplits = when (LocationUtils.currentArea) {
            Island.Dungeon -> {
                val floor = DungeonListener.floor ?: return modMessage("§cFailed to get dungeon floor!")

                with(dungeonSplits[floor.floorNumber].toMutableList()) {
                    addAll(0, listOf(
                        Split(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!"), "§2Blood Open") ,
                        Split(Regex(BLOOD_OPEN_REGEX), "§bBlood Clear"),
                        Split(Regex("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\."), "§dPortal Entry")
                    ))
                    add(Split(Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$"), "§1Total"))
                    SplitsGroup(map { it.copy(time = 0L) }, floor.personalBest)
                }
            }

            Island.Kuudra -> when (KuudraUtils.kuudraTier) {
                5 -> SplitsGroup(kuudraT5SplitsGroup.map { it.copy(time = 0L) }, kuudraT5PBs)
                4 -> SplitsGroup(kuudraSplitsGroup.map   { it.copy(time = 0L) }, kuudraT4PBs)
                3 -> SplitsGroup(kuudraSplitsGroup.map   { it.copy(time = 0L) }, kuudraT3PBs)
                2 -> SplitsGroup(kuudraSplitsGroup.map   { it.copy(time = 0L) }, kuudraT2PBs)
                1 -> SplitsGroup(kuudraSplitsGroup.map   { it.copy(time = 0L) }, kuudraT1PBs)
                else -> SplitsGroup(emptyList(), null)
            }

            else -> SplitsGroup(emptyList(), null)
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

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentSplits = SplitsGroup(mutableListOf(), null)
    }
}

private val kuudraT5PBs = PersonalBest("KuudraT5", 7)
private val kuudraT4PBs = PersonalBest("KuudraT4", 5)
private val kuudraT3PBs = PersonalBest("KuudraT3", 5)
private val kuudraT2PBs = PersonalBest("KuudraT2", 5)
private val kuudraT1PBs = PersonalBest("KuudraT1", 5)

val kuudraT5SplitsGroup = mutableListOf(
    Split(Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$"), "§2Supplies"),
    Split(Regex("^\\[NPC] Elle: OMG! Great work collecting my supplies!$"), "§bBuild"),
    Split(Regex("^\\[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!$"), "§dEaten"),
    Split(Regex("^(?!Elle has been eaten by Kuudra!\$)(.{1,16}) has been eaten by Kuudra!$"), "§cStun"),
    Split(Regex("^(.{1,16}) destroyed one of Kuudra's pods!\$"), "§4DPS"),
    Split(Regex("^\\[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!\$"), "§4Cleared"),
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

private val entranceSplitGroup = mutableListOf<Split>()

private val floor1SplitGroup = mutableListOf(
    Split(entryRegexes[0], "§cBonzo's Sike"),
    Split(Regex("\\[BOSS] Bonzo: Oh I'm dead!"), "§4Cleared"),
)

private val floor2SplitGroup = mutableListOf(
    Split(entryRegexes[1], "§cScarf's minions"),
    Split(Regex("^\\[BOSS] Scarf: Did you forget\\? I was taught by the best! Let's dance\\.$"), "§4Cleared"),
)

private val floor3SplitGroup = mutableListOf(
    Split(entryRegexes[2], "§cThe Guardians"),
    Split(Regex("^\\[BOSS] The Professor: Oh\\? You found my Guardians' one weakness\\?$"), "§aThe Professor"),
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

// https://regex101.com/r/BXKhOI/1
private const val BLOOD_OPEN_REGEX = "^\\[BOSS] The Watcher: (Congratulations, you made it through the Entrance\\.|Ah, you've finally arrived\\.|Ah, we meet again\\.\\.\\.|So you made it this far\\.\\.\\. interesting\\.|You've managed to scratch and claw your way here, eh\\?|I'm starting to get tired of seeing you around here\\.\\.\\.|Oh\\.\\. hello\\?|Things feel a little more roomy now, eh\\?)$|^The BLOOD DOOR has been opened!$"