package me.odinmain.utils.skyblock.dungeon

import me.odinmain.utils.cleanSB
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.sidebarLines
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.network.NetworkPlayerInfo

// In future maybe add stats about the dungeon like time elapsed, deaths, total secrets etc.
// could add some system to look back at previous runs.
class Dungeon {

    lateinit var floor: Floor
    val inBoss: Boolean get() = getBoss()
    var deathCount = 0
    var secretCount = 0
    var cryptsCount = 0

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
        Executor(500) {
            DungeonUtils.getDungeonTabList()?.let { updateRunInformation(it) }
        }
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



    private val deathsPattern = Regex("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")

    private fun updateRunInformation(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        tabEntries.forEach {
            val text = it.second
            when {
                text.contains("Deaths: ") -> {
                    val matcher = deathsPattern.find(text) ?: return@forEach
                    deathCount = matcher.groups["deaths"]?.value?.toIntOrNull() ?: deathCount
                }
                text.contains("Secrets Found: ") && !text.contains("%") -> {
                    val matcher = secretsFoundPattern.find(text) ?: return@forEach
                    secretCount = matcher.groups["secrets"]?.value?.toIntOrNull() ?: secretCount
                }
                text.contains("Crypts: ") -> {
                    val matcher = cryptsPattern.find(text) ?: return@forEach
                    cryptsCount = matcher.groups["crypts"]?.value?.toIntOrNull() ?: cryptsCount
                }
            }
        }
    }

}