package me.odinmain.utils.skyblock.dungeon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.RoomEnterEvent
import me.odinmain.features.impl.dungeon.LeapMenu
import me.odinmain.features.impl.dungeon.LeapMenu.odinSorting
import me.odinmain.features.impl.dungeon.MapInfo.shownTitle
import me.odinmain.features.impl.dungeon.Mimic
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.hasBonusPaulScore
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.romanToInt
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getDungeonTeammates
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter
import net.minecraftforge.event.entity.EntityJoinWorldEvent

// could add some system to look back at previous runs.
class Dungeon {

    var dungeonTeammates: ArrayList<DungeonPlayer> = ArrayList(5)
    var dungeonTeammatesNoSelf: List<DungeonPlayer> = ArrayList(4)
    var leapTeammates: List<DungeonPlayer> = ArrayList(4)

    inline val passedRooms: MutableSet<Room> get() = ScanUtils.passedRooms
    inline val currentRoom: Room? get() = ScanUtils.currentRoom
    private var expectingBloodUpdate = false
    val inBoss: Boolean get() = getBoss()
    var dungeonStats = DungeonStats()
    var puzzles = ArrayList<Puzzle>()
    var floor: Floor? = null
    var paul = false

    private fun getBoss(): Boolean = when (floor?.floorNumber) {
        1             -> posX > -71 && posZ > -39
        in 2..4 -> posX > -39 && posZ > -39
        in 5..6 -> posX > -39 && posZ > -7
        7             -> posX > -7  && posZ > -7
        else -> false
    }

    init {
        scope.launch(Dispatchers.IO) { paul = hasBonusPaulScore() }

        Blessing.entries.forEach { it.reset() }
        shownTitle = false
    }

    fun enterDungeonRoom(event: RoomEnterEvent) {
        val room = event.room?.takeUnless { room -> passedRooms.any { it.data.name == room.data.name } } ?: return
        dungeonStats.knownSecrets += room.data.secrets
    }

    fun onPacket(event: PacketEvent.Receive) {
        when (event.packet) {
            is S38PacketPlayerListItem -> {
                if (!event.packet.action.equalsOneOf(S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, S38PacketPlayerListItem.Action.ADD_PLAYER)) return
                val tabListEntries = event.packet.entries?.mapNotNull { it.displayName?.unformattedText?.noControlCodes } ?: return
                updateDungeonTeammates(tabListEntries)
                updateDungeonStats(tabListEntries)
                getDungeonPuzzles(tabListEntries)
            }

            is S3EPacketTeams -> {
                if (event.packet.action != 2) return
                val text = event.packet.prefix?.plus(event.packet.suffix) ?: return

                floorRegex.find(text.noControlCodes)?.groupValues?.get(1)?.let { floor = Floor.valueOf(it) }

                clearedRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()?.let {
                    if (dungeonStats.percentCleared != it && expectingBloodUpdate) dungeonStats.bloodDone = true
                    dungeonStats.percentCleared = it
                }
            }

            is S47PacketPlayerListHeaderFooter -> {
                Blessing.entries.forEach { blessing ->
                    blessing.regex.find(event.packet.footer?.unformattedText?.noControlCodes ?: return@forEach)?.let { blessing.current = romanToInt(it.groupValues[1]) }
                }
            }

            is S02PacketChat -> {
                val message = event.packet.chatComponent?.unformattedText?.noControlCodes ?: return
                if (expectingBloodRegex.matches(message)) expectingBloodUpdate = true
                doorOpenRegex.find(message)?.let { dungeonStats.doorOpener = it.groupValues[1] }
                deathRegex.find(message)?.let { match ->
                    dungeonTeammates.find { teammate -> teammate.name == (match.groupValues[1].takeUnless { it == "You" } ?: mc.thePlayer?.name) }?.deaths?.inc()
                }

                when (partyMessageRegex.find(message)?.groupValues?.get(1)?.lowercase() ?: return) {
                    "mimic killed", "mimic slain", "mimic killed!", "mimic dead", "mimic dead!", "\$skytils-dungeon-score-mimic\$", Mimic.mimicMessage ->
                        dungeonStats.mimicKilled = true

                    "blaze done!", "blaze done", "blaze puzzle solved!" ->
                        puzzles.find { it.name == Puzzle.Blaze.name }.let { it?.status = PuzzleStatus.Completed }
                }
            }
        }
    }

    fun onEntityJoin(event: EntityJoinWorldEvent) {
        val teammate = dungeonTeammatesNoSelf.find { it.name == event.entity.name } ?: return
        teammate.entity = event.entity as? EntityPlayer ?: return
    }

    private fun getDungeonPuzzles(tabList: List<String>) {
        for (entry in tabList) {
            val (name, status) = puzzleRegex.find(entry)?.destructured ?: continue
            val puzzle = Puzzle.allPuzzles.find { it.name == name }?.copy() ?: continue
            if (puzzle !in puzzles) puzzles.add(puzzle)

            puzzle.status = when {
                DungeonUtils.puzzles.find { it.name == puzzle.name }?.status == PuzzleStatus.Completed -> PuzzleStatus.Completed
                status == "✖" -> PuzzleStatus.Failed
                status == "✔" -> PuzzleStatus.Completed
                status == "✦" -> PuzzleStatus.Incomplete
                else -> {
                    modMessage(entry)
                    continue
                }
            }
        }
    }

    private val puzzleRegex = Regex("^ (\\w+(?: \\w+)*|\\?\\?\\?): \\[([✖✔✦])] ?(?:\\((\\w+)\\))?$")
    private val expectingBloodRegex = Regex("^\\[BOSS] The Watcher: You have proven yourself. You may pass.")
    private val doorOpenRegex = Regex("^(?:\\[\\w+] )?(\\w+) opened a (?:WITHER|Blood) door!")
    private val secretPercentRegex = Regex("^§r Secrets Found: §r§[ea]([\\d.]+)%§r$")
    private val clearedRegex = Regex("^Cleared: §[c6a](\\d+)% §8(?:§8)?\\(\\d+\\)$")
    private val timeRegex = Regex("§r Time: §r§6((?:\\d+h ?)?(?:\\d+m ?)?\\d+s)§r")
    private val completedRoomsRegex = Regex("^§r Completed Rooms: §r§d(\\d+)§r$")
    private val deathRegex = Regex("^ ☠ You died and became a ghost\\.\$")
    private val secretCountRegex = Regex("^§r Secrets Found: §r§b(\\d+)§r$")
    private val openedRoomsRegex = Regex("^§r Opened Rooms: §r§5(\\d+)§r$")
    private val deathsRegex = Regex("^§r§a§lTeam Deaths: §r§f(\\d+)§r$")
    private val floorRegex = Regex("The Catacombs \\((\\w+)\\)\$")
    private val partyMessageRegex = Regex("^Party > .*?: (.+)\$")
    private val cryptRegex = Regex("^§r Crypts: §r§6(\\d+)§r$")

    data class DungeonStats(
        var secretsFound: Int = 0,
        var secretsPercent: Float = 0f,
        var knownSecrets: Int = 0,
        var crypts: Int = 0,
        var openedRooms: Int = 0,
        var completedRooms: Int = 0,
        var deaths: Int = 0,
        var percentCleared: Int = 0,
        var elapsedTime: String = "0s",
        var mimicKilled: Boolean = false,
        var doorOpener: String = "Unknown",
        var bloodDone: Boolean = false,
    )

    private fun updateDungeonStats(text: List<String>) {
        for (entry in text) {
            with (dungeonStats) {
                secretsPercent = secretPercentRegex.find(entry)?.groupValues?.get(1)?.toFloatOrNull() ?: secretsPercent
                completedRooms = completedRoomsRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: completedRooms
                secretsFound = secretCountRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: secretsFound
                openedRooms = openedRoomsRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: openedRooms
                deaths = deathsRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: deaths
                crypts = cryptRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: crypts
                elapsedTime = timeRegex.find(entry)?.groupValues?.get(1) ?: elapsedTime
            }
        }
    }

    private fun updateDungeonTeammates(tabList: List<String>) {
        dungeonTeammates = getDungeonTeammates(dungeonTeammates, tabList)
        dungeonTeammatesNoSelf = dungeonTeammates.filter { it.entity != mc.thePlayer }

        leapTeammates =
            when (LeapMenu.type) {
                0 -> odinSorting(dungeonTeammatesNoSelf.sortedBy { it.clazz.priority }).toList()
                1 -> dungeonTeammatesNoSelf.sortedWith(compareBy({ it.clazz.ordinal }, { it.name }))
                2 -> dungeonTeammatesNoSelf.sortedBy { it.name }
                3 -> dungeonTeammatesNoSelf.sortedBy { DungeonUtils.customLeapOrder.indexOf(it.name.lowercase()).takeIf { index -> index != -1 } ?: Int.MAX_VALUE }
                else -> dungeonTeammatesNoSelf
            }
    }
}