package me.odinmain.utils.skyblock.dungeon

import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.cleanSB
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.sidebarLines

// In future maybe add stats about the dungeon like time elapsed, deaths, total secrets etc.
// could add some system to look back at previous runs.
class Dungeon {

    lateinit var floor: Floor
    var inBoss = false
    /**
     * Sets the value of the `inBoss` variable based on the current dungeon floor and player position.
     *
     * This function determines whether the player is currently in a boss area by evaluating the player's position (`posX` and `posZ`)
     * in relation to specific coordinates for different dungeon floors. It updates the `inBoss` variable accordingly.
     *
     * The boss area criteria for each floor are as follows:
     * - Floor 1: `posX > -71 && posZ > -39`
     * - Floors 2, 3, 4: `posX > -39 && posZ > -39`
     * - Floors 5, 6: `posX > -39 && posZ > -7`
     * - Floor 7: `posX > -7 && posZ > -7`
     * - Other floors: `false`
     */
    fun setBoss() {
        inBoss = when (floor.floorNumber) {
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

}