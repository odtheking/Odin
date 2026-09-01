package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.events.ChatMessageEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.skyblock.Splits
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener

data class Split(val regex: Regex, val name: String, var time: Long = 0L, var ticks: Long = 0L)
data class SplitsGroup(val splits: List<Split>, val personalBest: PersonalBest?)
data class SplitRow(val name: String, val time: Long, val tickTime: Long, val isCurrent: Boolean)

object SplitsManager {

    private var currentSplits: SplitsGroup = SplitsGroup(emptyList(), null)
    private var tickCounter: Long = 0L

    init {
        on<ChatMessageEvent> {
            if (value == "Starting in 1 second.") startRun() else onSplitMessage(value)
        }

        on<TickEvent.Server> {
            tickCounter++
        }

        on<LevelEvent.Load> {
            currentSplits = SplitsGroup(emptyList(), null)
            tickCounter = 0L
        }
    }

    private fun startRun() {
        tickCounter = 0L
        currentSplits = when (LocationUtils.currentArea) {
            Island.Dungeon -> buildDungeonSplits() ?: return
            Island.Kuudra -> buildKuudraSplits() ?: return
            else -> SplitsGroup(emptyList(), null)
        }
    }

    private fun buildDungeonSplits(): SplitsGroup? {
        if (Splits.splitLocation == 2) return null
        val floor = DungeonListener.floor ?: return null

        val splits = dungeonSplits[floor.floorNumber].toMutableList().apply {
            addAll(0, listOf(
                Split(MORT_REGEX, "§2Blood Open"),
                Split(BLOOD_OPEN_REGEX, "§bBlood Clear"),
                Split(PORTAL_ENTRY_REGEX, "§dPortal Entry"),
            ))
            add(Split(DUNGEON_CLEARED_REGEX, "§1Total"))
        }
        return SplitsGroup(splits.map { it.copy() }, Splits.dungeonPBsList[floor.ordinal])
    }

    private fun buildKuudraSplits(): SplitsGroup? {
        if (Splits.splitLocation == 1) return null
        return when (KuudraUtils.kuudraTier) {
            5 -> SplitsGroup(kuudraT5SplitsGroup.map { it.copy() }, Splits.kuudraT5PBs)
            4 -> SplitsGroup(kuudraSplitsGroup.map { it.copy() }, Splits.kuudraT4PBs)
            3 -> SplitsGroup(kuudraSplitsGroup.map { it.copy() }, Splits.kuudraT3PBs)
            2 -> SplitsGroup(kuudraSplitsGroup.map { it.copy() }, Splits.kuudraT2PBs)
            1 -> SplitsGroup(kuudraSplitsGroup.map { it.copy() }, Splits.kuudraT1PBs)
            else -> SplitsGroup(emptyList(), null)
        }
    }

    private fun onSplitMessage(message: String) {
        val splits = currentSplits.splits
        val split = splits.find { it.regex.matches(message) } ?: return
        if (split.time != 0L) return

        split.time = System.currentTimeMillis()
        split.ticks = tickCounter

        val index = splits.indexOf(split)
        if (index == 0) return

        val previous = splits[index - 1]
        val segmentTime = (split.time - previous.time) / 1000f

        if (index == splits.lastIndex) finishRun(index, segmentTime)
        else currentSplits.personalBest?.time(previous.name, segmentTime, "s§7!", "§6${previous.name} §7took §6", Splits.enabled)
    }

    private fun finishRun(totalIndex: Int, lastSegmentTime: Float) {
        val rows = currentRows()
        val splits = currentSplits.splits
        val personalBest = currentSplits.personalBest

        schedule(10) {
            if (rows.isEmpty()) return@schedule
            personalBest?.time(splits[totalIndex - 1].name, lastSegmentTime, "s§7!", "§6${splits[totalIndex - 1].name} §7took §6", Splits.enabled)
            personalBest?.time(splits[totalIndex].name, rows.last().time / 1000f, "s§7!", "§6Total time §7took §6", Splits.enabled)
            rows.forEachIndexed { i, row ->
                if (Splits.enabled) modMessage("§6${if (i == rows.lastIndex) "Total" else row.name} §7took §6${formatTime(row.time)}§7.")
            }
        }
    }

    fun currentRows(): List<SplitRow> {
        val splits = currentSplits.splits
        if (splits.isEmpty()) return emptyList()
        if (splits[0].time == 0L) return splits.map { SplitRow(it.name, 0L, 0L, isCurrent = false) }

        val last = splits.last()
        val latestTime = if (last.time != 0L) last.time else System.currentTimeMillis()
        val latestTicks = if (last.ticks != 0L) last.ticks else tickCounter

        val times = LongArray(splits.size).apply { this[splits.lastIndex] = latestTime - splits.first().time }
        val ticks = LongArray(splits.size).apply { this[splits.lastIndex] = latestTicks - splits.first().ticks }

        var currentIndex = -1
        for (i in 0 until splits.lastIndex) {
            val next = splits[i + 1]
            if (next.time != 0L) {
                times[i] = next.time - splits[i].time
                ticks[i] = next.ticks - splits[i].ticks
            } else {
                times[i] = latestTime - splits[i].time
                ticks[i] = latestTicks - splits[i].ticks
                currentIndex = i
                break
            }
        }
        return splits.indices.map { i -> SplitRow(splits[i].name, times[i], ticks[i], isCurrent = i == currentIndex) }
    }
}

val kuudraT5SplitsGroup = mutableListOf(
    Split(Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$"), "§2Supplies"),
    Split(Regex("^\\[NPC] Elle: OMG! Great work collecting my supplies!$"), "§bBuild"),
    Split(Regex("^\\[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!$"), "§dEaten"),
    Split(Regex("^(?!Elle has been eaten by Kuudra!$)(.{1,16}) has been eaten by Kuudra!$"), "§cStun"),
    Split(Regex("^(.{1,16}) destroyed one of Kuudra's pods!$"), "§4DPS"),
    Split(Regex("^\\[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!$"), "§4Cleared"),
    Split(Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$"), "Total"))

val kuudraSplitsGroup = mutableListOf(
    Split(Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$"), "§2Supplies"),
    Split(Regex("^\\[NPC] Elle: OMG! Great work collecting my supplies!$"), "§bBuild"),
    Split(Regex("^\\[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!$"), "§cStun"),
    Split(Regex("^\\[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!$"), "§4Cleared"),
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

val floor7SplitGroup = mutableListOf(
    Split(entryRegexes[6], "§5Maxor"),
    Split(Regex("\\[BOSS] Storm: Pathetic Maxor, just like expected\\."), "§3Storm"),
    Split(Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?"), "§6Terminals"),
    Split(Regex("The Core entrance is opening!"), "§7Goldor"),
    Split(Regex("\\[BOSS] Necron: You went further than any human before, congratulations\\."), "§cNecron"),
    Split(Regex("\\[BOSS] Necron: All this, for nothing\\.\\.\\."), "§4Cleared"),
)

private val dungeonSplits = listOf(
    entranceSplitGroup,
    floor1SplitGroup,
    floor2SplitGroup,
    floor3SplitGroup,
    floor4SplitGroup,
    floor5SplitGroup,
    floor6SplitGroup,
    floor7SplitGroup,
)

private val PORTAL_ENTRY_REGEX = Regex("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\.")
private val DUNGEON_CLEARED_REGEX = Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?$")

// https://regex101.com/r/BXKhOI/1
private val BLOOD_OPEN_REGEX = Regex("^\\[BOSS] The Watcher: (Congratulations, you made it through the Entrance\\.|Ah, you've finally arrived\\.|Ah, we meet again\\.\\.\\.|So you made it this far\\.\\.\\. interesting\\.|You've managed to scratch and claw your way here, eh\\?|I'm starting to get tired of seeing you around here\\.\\.\\.|Oh\\.\\. hello\\?|Things feel a little more roomy now, eh\\?)$|^The BLOOD DOOR has been opened!$")
val MORT_REGEX = Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!")
