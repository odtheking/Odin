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
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getDungeonTeammates
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonListener {

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

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        Blessing.entries.forEach { it.reset() }
        dungeonTeammatesNoSelf = emptyList()
        dungeonStats = DungeonStats()
        expectingBloodUpdate = false
        leapTeammates = emptyList()
        dungeonTeammates.clear()
        shownTitle = false
        puzzles.clear()
        floor = null
        paul = false
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun enterDungeonRoom(event: RoomEnterEvent) {
        val room = event.room?.takeUnless { room -> passedRooms.any { it.data.name == room.data.name } } ?: return
        dungeonStats.knownSecrets += room.data.secrets
    }

    @SubscribeEvent
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
                val text = event.packet.prefix?.plus(event.packet.suffix)?.noControlCodes ?: return

                floorRegex.find(text)?.groupValues?.get(1)?.let {
                    scope.launch(Dispatchers.IO) { paul = hasBonusPaulScore() }
                    floor = Floor.valueOf(it)
                }

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
                        puzzles.find { it == Puzzle.BLAZE }.let { it?.status = PuzzleStatus.Completed }
                }
            }
        }
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        val teammate = dungeonTeammatesNoSelf.find { it.name == event.entity.name } ?: return
        teammate.entity = event.entity as? EntityPlayer ?: return
    }

    private fun getDungeonPuzzles(tabList: List<String>) {
        for (entry in tabList) {
            val (name, status) = puzzleRegex.find(entry)?.destructured ?: continue
            val puzzle = Puzzle.entries.find { it.displayName == name }?.takeIf { it != Puzzle.UNKNOWN } ?: continue
            if (puzzle !in puzzles) puzzles.add(puzzle)

            puzzle.status = when (status) {
                "✖" -> PuzzleStatus.Failed
                "✔" -> PuzzleStatus.Completed
                "✦" -> PuzzleStatus.Incomplete
                else -> {
                    devMessage(entry)
                    continue
                }
            }
        }
    }

    private fun updateDungeonStats(text: List<String>) {
        for (entry in text) {
            with (dungeonStats) {
                secretsPercent = secretPercentRegex.find(entry)?.groupValues?.get(1)?.toFloatOrNull() ?: secretsPercent
                completedRooms = completedRoomsRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: completedRooms
                secretsFound = secretCountRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: secretsFound
                openedRooms = openedRoomsRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: openedRooms
                puzzleCount = puzzleCountRegex.find(entry)?.groupValues?.get(1)?.toIntOrNull() ?: puzzleCount
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

    private val puzzleRegex = Regex("^ (\\w+(?: \\w+)*|\\?\\?\\?): \\[([✖✔✦])] ?(?:\\((\\w+)\\))?$")
    private val expectingBloodRegex = Regex("^\\[BOSS] The Watcher: You have proven yourself. You may pass.")
    private val doorOpenRegex = Regex("^(?:\\[\\w+] )?(\\w+) opened a (?:WITHER|Blood) door!")
    private val secretPercentRegex = Regex("^ Secrets Found: ([\\d.]+)%$")
    private val deathRegex = Regex("☠ (\\w{1,16}) .* and became a ghost\\.")
    private val timeRegex = Regex("^ Time: ((?:\\d+h ?)?(?:\\d+m ?)?\\d+s)$")
    private val completedRoomsRegex = Regex("^ Completed Rooms: (\\d+)$")
    private val clearedRegex = Regex("^Cleared: (\\d+)% \\(\\d+\\)$")
    private val secretCountRegex = Regex("^ Secrets Found: (\\d+)$")
    private val openedRoomsRegex = Regex("^ Opened Rooms: (\\d+)$")
    private val floorRegex = Regex("The Catacombs \\((\\w+)\\)\$")
    private val partyMessageRegex = Regex("^Party > .*?: (.+)\$")
    private val puzzleCountRegex = Regex("^Puzzles: \\((\\d+)\\)\$")
    private val deathsRegex = Regex("^Team Deaths: (\\d+)$")
    private val cryptRegex = Regex("^ Crypts: (\\d+)$")

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
        var puzzleCount: Int = 0,
    )
}