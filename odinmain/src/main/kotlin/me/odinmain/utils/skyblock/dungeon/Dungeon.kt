package me.odinmain.utils.skyblock.dungeon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.impl.dungeon.LeapMenu
import me.odinmain.features.impl.dungeon.LeapMenu.odinSorting
import me.odinmain.features.impl.dungeon.Mimic
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getDungeonTeammates
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getDungeonPuzzles
import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.network.play.server.*

// could add some system to look back at previous runs.
class Dungeon(val floor: Floor?) {

    var paul = false
    val inBoss: Boolean get() = getBoss()
    var dungeonTeammates: List<DungeonPlayer> = emptyList()
    var dungeonTeammatesNoSelf: List<DungeonPlayer> = emptyList()
    var leapTeammates = mutableListOf<DungeonPlayer>()
    var dungeonStats = DungeonStats()
    var currentRoom: FullRoom? = null
    var passedRooms = mutableListOf<FullRoom>()
    var puzzles = listOf<Puzzle>()

    private fun getBoss(): Boolean {
        return when (floor?.floorNumber) {
            1 -> posX > -71 && posZ > -39
            in 2..4 -> posX > -39 && posZ > -39
            in 5..6 -> posX > -39 && posZ > -7
            7 -> posX > -7 && posZ > -7
            else -> false
        }
    }

    init {
        scope.launch(Dispatchers.IO) {
            paul = hasBonusPaulScore()
        }
    }

    fun enterDungeonRoom(event: RoomEnterEvent) {
        currentRoom = event.room ?: return
        if (passedRooms.any { it.room.data.name == event.room.room.data.name }) return
        event.room.let { passedRooms.add(it) }
        val roomSecrets = ScanUtils.getRoomSecrets(currentRoom?.room?.data?.name ?: return)
        dungeonStats.knownSecrets = dungeonStats.knownSecrets?.plus(roomSecrets) ?: roomSecrets
    }

    fun onPacket(event: PacketReceivedEvent) {
        when (event.packet) {
            is S38PacketPlayerListItem -> handleTabListPacket(event.packet)
            is S3EPacketTeams -> handleScoreboardPacket(event.packet)
            is S47PacketPlayerListHeaderFooter -> handleHeaderFooterPacket(event.packet)
            is S02PacketChat -> handleChatPacket(event.packet)
        }
    }

    private fun handleChatPacket(packet: S02PacketChat) {
        val message = packet.chatComponent.unformattedText.noControlCodes
        val doorOpener = Regex("(?:\\[\\w+] )?(\\w+) opened a (?:WITHER|Blood) door!").find(message)
        if (doorOpener != null) dungeonStats.doorOpener = doorOpener.groupValues[1]

        val partyMessage = Regex("Party > .*?: (.+)\$").find(message)?.groupValues?.get(1) ?: return
        if (partyMessage.lowercase().equalsOneOf("mimic killed", "mimic slain", "mimic killed!", "mimic dead", "mimic dead!", "\$skytils-dungeon-score-mimic\$", Mimic.mimicMessage))
            dungeonStats.mimicKilled = true
        if (partyMessage.lowercase().equalsOneOf("blaze done!", "blaze done")) { //more completion messages may be necessary.
            puzzles.find { it == Puzzle.Blaze }.let { it?.status = PuzzleStatus.Completed }
        }
    }

    private fun handleHeaderFooterPacket(packet: S47PacketPlayerListHeaderFooter) {
        Blessing.entries.forEach { blessing ->
            blessing.regex.find(packet.footer.unformattedText.noControlCodes)?.let { match ->
                blessing.current = romanToInt(match.groupValues[1])
            }
        }
    }

    private fun handleScoreboardPacket(packet: S3EPacketTeams) {
        if (packet.action != 2) return
        val text = packet.prefix.plus(packet.suffix)

        val cleared = Regex("^Cleared: §[c6a](\\d+)% §8(?:§8)?\\(\\d+\\)$").find(text)
        if (cleared != null) dungeonStats.percentCleared = cleared.groupValues[1].toInt()

        val time = Regex("^Time Elapsed: §a§a([\\dsmh ]+)$").find(text)
        if (time != null) dungeonStats.elapsedTime = time.groupValues[1]
    }

    private fun handleTabListPacket(packet: S38PacketPlayerListItem) {
        if (packet.action != S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME) return
        
        packet.entries.forEach { entry ->
            val text = entry?.displayName?.formattedText ?: return@forEach
            dungeonStats = updateDungeonStats(text, dungeonStats)
        }

        val tabList = getDungeonTabList() ?: emptyList()

        updateDungeonPuzzles(tabList)
        updateDungeonTeammates(tabList)
    }

    private val secretCountRegex = Regex("^§r Secrets Found: §r§b(\\d+)§r$")
    private val secretPercentRegex = Regex("^§r Secrets Found: §r§[ea]([\\d.]+)%§r$")
    private val cryptRegex = Regex("^§r Crypts: §r§6(\\d+)§r$")
    private val openedRoomsRegex = Regex("^§r Opened Rooms: §r§5(\\d+)§r$")
    private val completedRoomsRegex = Regex("^§r Completed Rooms: §r§d(\\d+)§r$")
    private val deathsRegex = Regex("^§r§a§lTeam Deaths: §r§f(\\d+)§r$")
    private val puzzleCountRegex = Regex("^§r§[a-z]§lPuzzles: §r§f\\((\\d)\\)§r$")

    data class DungeonStats(
        var secretsFound: Int? = null,
        var secretsPercent: Float? = null,
        var knownSecrets: Int? = null,
        var crypts: Int? = null,
        var openedRooms: Int? = null,
        var completedRooms: Int? = null,
        var deaths: Int? = null,
        var percentCleared: Int? = null,
        var elapsedTime: String? = null,
        var mimicKilled: Boolean = false,
        var doorOpener: String? = null,
    )

    private fun updateDungeonStats(text: String, currentStats: DungeonStats): DungeonStats {
        //modMessage(text.replace("§", "&"))
        when {
            secretCountRegex.matches(text) -> {
                val matchResult = secretCountRegex.find(text)
                currentStats.secretsFound = matchResult?.groupValues?.get(1)?.toIntOrNull()
            }
            secretPercentRegex.matches(text) -> {
                val matchResult = secretPercentRegex.find(text)
                currentStats.secretsPercent = matchResult?.groupValues?.get(1)?.toFloatOrNull()
            }
            cryptRegex.matches(text) -> {
                val matchResult = cryptRegex.find(text)
                currentStats.crypts = matchResult?.groupValues?.get(1)?.toIntOrNull()
            }
            openedRoomsRegex.matches(text) -> {
                val matchResult = openedRoomsRegex.find(text)
                currentStats.openedRooms = matchResult?.groupValues?.get(1)?.toIntOrNull()
            }
            completedRoomsRegex.matches(text) -> {
                val matchResult = completedRoomsRegex.find(text)
                currentStats.completedRooms = matchResult?.groupValues?.get(1)?.toIntOrNull()
            }
            deathsRegex.matches(text) -> {
                val matchResult = deathsRegex.find(text)
                currentStats.deaths = matchResult?.groupValues?.get(1)?.toIntOrNull()
            }
            /**puzzleCountRegex.matches(text) -> {
                val matchResult = puzzleCountRegex.find(text)
                currentStats.puzzleCount = matchResult?.groupValues?.get(1)?.toIntOrNull()
            }
            puzzleRegex.matches(text) -> {
                val matchResult = puzzleRegex.find(text)
                val puzzle = Puzzle.allPuzzles.find { it.name == matchResult?.groupValues?.get(1) }?.copy()
                if (puzzle != null) {
                    modMessage(text.replace("§", "&"), false)
                    val status: PuzzleStatus? = when {
                        matchResult?.groupValues?.get(2) == "§r§c§l✖" -> PuzzleStatus.Failed
                        matchResult?.groupValues?.get(2) == "§r§a§l✔" -> PuzzleStatus.Completed
                        matchResult?.groupValues?.get(2) == "§r§6§l✦" -> PuzzleStatus.Incomplete
                        else -> null
                    }

                    if (puzzle !in currentStats.puzzles || (currentStats.puzzles.size != currentStats.puzzleCount && puzzle == Puzzle.Unknown)) {
                        puzzle.status = status
                        currentStats.puzzles.add(puzzle)
                        if (puzzle != Puzzle.Unknown) currentStats.puzzles.remove(currentStats.puzzles.firstOrNull {it == Puzzle.Unknown})
                    } else currentStats.puzzles.find { it == puzzle }?.status = status
                }
            }*/
        }

        return currentStats
    }

    private fun updateDungeonPuzzles(tabList: List<Pair<NetworkPlayerInfo, String>>){
        val tabEntries = tabList.map { it.second }
        modMessage(tabEntries.map { it.replace("§", "&") })
        val puzzleText = tabEntries.find { puzzleCountRegex.matches(it) } ?: return modMessage("Puzzle Text not in Tab Entries")
        modMessage(puzzleText.replace("§", "&"))
        val index = tabEntries.indexOf(puzzleText)
        val matchResult = puzzleCountRegex.find(puzzleText)?.groupValues?.get(1)?.toIntOrNull() ?: return
        val puzzleData = tabList.filterIndexed { i, _ -> i in index + 1..index + matchResult }
        modMessage(puzzleData)
        puzzles = getDungeonPuzzles(puzzleData.map { it.second })
    }

    private fun updateDungeonTeammates(tabList: List<Pair<NetworkPlayerInfo, String>>) {
        dungeonTeammates = getDungeonTeammates(dungeonTeammates, tabList)
        dungeonTeammatesNoSelf = dungeonTeammates.filter { it.entity != mc.thePlayer }

        leapTeammates =
            when (LeapMenu.type) {
                0 -> odinSorting(dungeonTeammatesNoSelf.sortedBy { it.clazz.priority }).toMutableList()
                1 -> dungeonTeammatesNoSelf.sortedWith(compareBy({ it.clazz.ordinal }, { it.name })).toMutableList()
                2 -> dungeonTeammatesNoSelf.sortedBy { it.name }.toMutableList()
                else -> dungeonTeammatesNoSelf.toMutableList()
            }
    }
}