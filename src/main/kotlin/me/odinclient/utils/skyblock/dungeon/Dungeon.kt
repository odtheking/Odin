package me.odinclient.utils.skyblock.dungeon

import me.odinclient.utils.Executor
import me.odinclient.utils.Wrappers
import me.odinclient.utils.skyblock.ScoreboardUtils

// In future maybe add stats about the dungeon like time elapsed, deaths, total secrets etc. could add some system to look back at previous runs.
class Dungeon : Wrappers() {

    init {
        getCurrentFloor()

        Executor(500) {
            inBoss = when (floor.floorNumber) {
                1 -> posX > -71 && posZ > -39
                2, 3, 4 -> posX > -39 && posZ > -39
                5, 6 -> posX > -39 && posZ > -7
                7 -> posX > -7 && posZ > -7
                else -> false
            }
        }
    }

    lateinit var floor: Floor
    var inBoss = false

    private fun getCurrentFloor() {
        ScoreboardUtils.sidebarLines.forEach {
            val line = ScoreboardUtils.cleanSB(it)
            if (line.contains("The Catacombs (")) {
                floor = try {
                    Floor.valueOf(line.substringAfter("(").substringBefore(")"))
                } catch (_ : IllegalArgumentException) {
                    return println("This should not be possible, error in dungeonutils of odinclient, please report this!.")
                }
            }
        }
    }

    enum class Floor {
        E, F1, F2, F3, F4, F5, F6, F7, M1, M2, M3, M4, M5, M6, M7;

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

        val isInMM: Boolean
            get() {
                return when (this) {
                    E, F1, F2, F3, F4, F5, F6, F7 -> false
                    M1, M2, M3, M4, M5, M6, M7 -> true
                }
            }
    }
}