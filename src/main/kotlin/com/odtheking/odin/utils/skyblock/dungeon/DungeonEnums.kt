package com.odtheking.odin.utils.skyblock.dungeon

import com.odtheking.odin.features.impl.dungeon.map.DungMap.mapCenter
import com.odtheking.odin.features.impl.dungeon.map.DungMap.roomSize
import com.odtheking.odin.features.impl.dungeon.map.Vec2i
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player

/**
 * Data class representing a player in a dungeon, including their name, class, skin location, and associated player entity.
 *
 * @property name The name of the player.
 * @property clazz The player's class, defined by the [DungeonClass] enum.
 * @property locationSkin The resource location of the player's skin.
 * @property isDead The player's death status. Defaults to `false`.
 */
data class DungeonPlayer(
    val name: String,
    val clazz: DungeonClass,
    val clazzLvl: Int,
    val locationSkin: Identifier?,
    var entity: Player? = null,
    var isDead: Boolean = false,
    var deaths: Int = 0,
    var mapPos: Vec2i = Vec2i(0, 0),
    var yaw: Float = 0f,
) {
    fun mapRenderPosition(): Pair<Float, Float> =
        entity?.let {
            ((it.x + 201f) / (32f / 20f)).toFloat() to ((it.z + 201f) / (32f / 20f)).toFloat()
        } ?: run {
            roomSize?.let { size ->
               mapCenter.add(mapPos.multiply(32.0 / (((size + 4.0) * 2)))).add(Vec2i(201, 201)).divide(32.0 / 20.0).let { it.x.toFloat() to it.z.toFloat() }
            } ?: Pair(0f, 0f)
        }

    fun mapRenderYaw(): Float = entity?.yRot ?: yaw
}

/**
 * Enumeration representing puzzles in a dungeon.
 *
 * @property displayName The display name of the puzzle.
 * @property status The current status of the puzzle. Defaults to `null`.
 */
enum class Puzzle(
    val displayName: String,
    var status: PuzzleStatus? = null
) {
    UNKNOWN("???"),
    BLAZE("Higher Or Lower"),
    BEAMS("Creeper Beams"),
    WEIRDOS("Three Weirdos"),
    TTT("Tic Tac Toe"),
    WATER_BOARD("Water Board"),
    TP_MAZE("Teleport Maze"),
    BOULDER("Boulder"),
    ICE_FILL("Ice Fill"),
    ICE_PATH("Ice Path"),
    QUIZ("Quiz"),
    BOMB_DEFUSE("Bomb Defuse");
}

sealed class PuzzleStatus {
    data object Completed : PuzzleStatus()
    data object Failed : PuzzleStatus()
    data object Incomplete : PuzzleStatus()
}

/**
 * Enumeration representing player classes in a dungeon setting.
 *
 * Each class is associated with a specific code and color used for formatting in the game. The classes include Archer,
 * Mage, Berserk, Healer, and Tank.
 *
 * @property color The color associated with the class.
 * @property defaultQuadrant The default quadrant for the class.
 * @property priority The priority of the class.
 *
 */
enum class DungeonClass(
    val color: Color,
    val colorCode: Char,
    val defaultQuadrant: Int,
    var priority: Int,
) {
    Archer(Colors.MINECRAFT_GOLD, '6', 0, 2),
    Berserk(Colors.MINECRAFT_DARK_RED, '4', 1, 0),
    Healer(Colors.MINECRAFT_LIGHT_PURPLE, 'd', 2, 2),
    Mage(Colors.MINECRAFT_AQUA, 'b', 3, 2),
    Tank(Colors.MINECRAFT_DARK_GREEN, '2', 3, 1),
    Unknown(Colors.WHITE, 'f', 0, 0)
}

enum class Blessing(
    var regex: Regex,
    val displayString: String,
    var current: Int = 0
) {
    POWER(Regex("Blessing of Power (X{0,3}(IX|IV|V?I{0,3}))"), "Power"),
    LIFE(Regex("Blessing of Life (X{0,3}(IX|IV|V?I{0,3}))"), "Life"),
    WISDOM(Regex("Blessing of Wisdom (X{0,3}(IX|IV|V?I{0,3}))"), "Wisdom"),
    STONE(Regex("Blessing of Stone (X{0,3}(IX|IV|V?I{0,3}))"), "Stone"),
    TIME(Regex("Blessing of Time (V)"), "Time");

    fun reset() {
        current = 0
    }
}

/**
 * Enumeration representing different floors in a dungeon.
 *
 * This enum class defines various floors, including both regular floors (F1 to F7) and master mode floors (M1 to M7).
 * Each floor has an associated floor number and an indicator of whether it is a master mode floor.
 *
 * @property floorNumber The numerical representation of the floor, where E represents the entrance floor.
 * @property isMM Indicates whether the floor is a master mode floor (M1 to M7).
 * @property secretPercentage The percentage of secrets required.
 */
enum class Floor(val secretPercentage: Float = 1f) {
    E(0.3f),
    F1(0.3f),
    F2(0.4f),
    F3(0.5f),
    F4(0.6f),
    F5(0.7f),
    F6(0.85f),
    F7,
    M1,
    M2,
    M3,
    M4,
    M5,
    M6,
    M7;

    /**
     * Gets the numerical representation of the floor.
     *
     * @return The floor number. E has a floor number of 0, F1 to F7 have floor numbers from 1 to 7, and M1 to M7 have floor numbers from 1 to 7.
     */
    inline val floorNumber: Int
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
     * Indicates whether the floor is a master mode floor.
     *
     * @return `true` if the floor is a master mode floor (M1 to M7), otherwise `false`.
     */
    inline val isMM: Boolean
        get() {
            return when (this) {
                E, F1, F2, F3, F4, F5, F6, F7 -> false
                M1, M2, M3, M4, M5, M6, M7 -> true
            }
        }
}

enum class M7Phases(val displayName: String) {
    P1("P1"), P2("P2"), P3("P3"), P4("P4"), P5("P5"), Unknown("Unknown");
}

