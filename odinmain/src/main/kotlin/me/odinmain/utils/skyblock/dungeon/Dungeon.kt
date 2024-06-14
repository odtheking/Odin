package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.impl.dungeon.LeapMenu
import me.odinmain.features.impl.dungeon.LeapMenu.odinSorting
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getDungeonTeammates
import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.network.play.server.*

// In future maybe add stats about the dungeon like time elapsed, deaths, total secrets etc.
// could add some system to look back at previous runs.
class Dungeon {

    lateinit var floor: Floor
    val inBoss: Boolean get() = getBoss()
    var dungeonTeammates: List<DungeonPlayer> = emptyList()
    var dungeonTeammatesNoSelf: List<DungeonPlayer> = emptyList()
    var leapTeammates = mutableListOf<DungeonPlayer>()
    var dungeonStats = DungeonStats()
    var currentRoom: FullRoom? = null
    var passedRooms = mutableSetOf<FullRoom>()

    private fun getBoss(): Boolean {
        return when (floor.floorNumber) {
            1 -> posX > -71 && posZ > -39
            in 2..4 -> posX > -39 && posZ > -39
            in 5..6 -> posX > -39 && posZ > -7
            7 -> posX > -7 && posZ > -7
            else -> false
        }
    }

    init {
        getCurrentFloor()
    }

    private fun getCurrentFloor() {
        for (i in sidebarLines) {
            val line = cleanSB(i)

            if (line.contains("The Catacombs (")) {
                runCatching { floor = Floor.valueOf(line.substringAfter("(").substringBefore(")")) }
                    .onFailure { modMessage("Could not get correct floor. Please report this.") }
            }
        }
    }

    fun enterDungeonRoom(event: EnteredDungeonRoomEvent) {
        currentRoom = event.room
        event.room?.let { passedRooms.add(it) }
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
        if (partyMessage.lowercase().equalsOneOf("mimic killed", "mimic slain", "mimic killed!", "mimic dead", "mimic dead!", "\$skytils-dungeon-score-mimic\$"))
            dungeonStats.mimicKilled = true
    }

    private fun handleHeaderFooterPacket(packet: S47PacketPlayerListHeaderFooter) {
        Blessings.entries.forEach { blessing ->
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
        updateDungeonTeammates()
    }

    /**
     * Enumeration representing different floors in a dungeon.
     *
     * This enum class defines various floors, including both regular floors (F1 to F7) and special mini-boss floors (M1 to M7).
     * Each floor has an associated floor number and an indicator of whether it is a mini-boss floor.
     *
     * @property floorNumber The numerical representation of the floor, where E represents the entrance floor.
     * @property isInMM Indicates whether the floor is a mini-boss floor (M1 to M7).
     */
    enum class Floor {
        E, F1, F2, F3, F4, F5, F6, F7,
        M1, M2, M3, M4, M5, M6, M7;

        /**
         * Gets the numerical representation of the floor.
         *
         * @return The floor number. E has a floor number of 0, F1 to F7 have floor numbers from 1 to 7, and M1 to M7 have floor numbers from 1 to 7.
         */
        val floorNumber: Int
            get() {
                return when (this) {
                    E -> 0
                    F1, M1 -> 1
                    F2, M2 -> 2
                    F3, M3 -> 3
                    F4, M4 -> 4
                    F5, M5 -> 5
                    F6, M6 -> 6
                    F7, M7 -> 7
                }
            }

        /**
         * Indicates whether the floor is a mini-boss floor.
         *
         * @return `true` if the floor is a mini-boss floor (M1 to M7), otherwise `false`.
         */
        val isInMM: Boolean
            get() {
                return when (this) {
                    E, F1, F2, F3, F4, F5, F6, F7 -> false
                    M1, M2, M3, M4, M5, M6, M7 -> true
                }
            }
    }

    private val secretCountRegex = Regex("^§r Secrets Found: §r§b(\\d+)§r$")
    private val secretPercentRegex = Regex("^§r Secrets Found: §r§[ea]([\\d.]+)%§r$")
    private val cryptRegex = Regex("^§r Crypts: §r§6(\\d+)§r$")
    private val openedRoomsRegex = Regex("^§r Opened Rooms: §r§5(\\d+)§r$")
    private val completedRoomsRegex = Regex("^§r Completed Rooms: §r§d(\\d+)§r$")
    private val deathsRegex = Regex("^§r§a§lTeam Deaths: §r§f(\\d+)§r$")

    data class DungeonStats(
        var secretsFound: Int? = null,
        var secretsPercent: Float? = null,
        var crypts: Int? = null,
        var openedRooms: Int? = null,
        var completedRooms: Int? = null,
        var deaths: Int? = null,
        var percentCleared: Int? = null,
        var elapsedTime: String? = null,
        var mimicKilled: Boolean = false,
        var doorOpener: String? = null
    )

    private fun updateDungeonStats(text: String, currentStats: DungeonStats): DungeonStats {
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
        }

        return currentStats
    }

    private fun updateDungeonTeammates() {
        dungeonTeammates = getDungeonTeammates(dungeonTeammates)
        dungeonTeammatesNoSelf = dungeonTeammates.filter { it.entity != mc.thePlayer }

        leapTeammates =
            when (LeapMenu.type) {
                0 -> odinSorting(dungeonTeammatesNoSelf.sortedBy { it.clazz.prio }).toMutableList()
                1 -> dungeonTeammatesNoSelf.sortedWith(compareBy({ it.clazz.ordinal }, { it.name })).toMutableList()
                2 -> dungeonTeammatesNoSelf.sortedBy { it.name }.toMutableList()
                else -> dungeonTeammatesNoSelf.toMutableList()
            }
    }
}