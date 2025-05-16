package me.odinmain.utils.skyblock.dungeon

import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.ui.Colors
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation

/**
 * Data class representing a player in a dungeon, including their name, class, skin location, and associated player entity.
 *
 * @property name The name of the player.
 * @property clazz The player's class, defined by the [DungeonClass] enum.
 * @property locationSkin The resource location of the player's skin.
 * @property entity The optional associated player entity. Defaults to `null`.
 * @property isDead The player's death status. Defaults to `false`.
 */
data class DungeonPlayer(
    val name: String,
    val clazz: DungeonClass,
    val clazzLvl: Int,
    val locationSkin: ResourceLocation = ResourceLocation("textures/entity/steve.png"),
    var entity: EntityPlayer? = null,
    var isDead: Boolean = false,
    var deaths: Int = 0
)

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
 * @property personalBest The personal best time for the floor.
 * @property secretPercentage The percentage of secrets required.
 */
enum class Floor(val personalBest: PersonalBest?, val secretPercentage: Float = 1f) {
    E(PersonalBest("Entrance", 4), 0.3f),
    F1(PersonalBest("Floor 1", 6), 0.3f),
    F2(PersonalBest("Floor 2", 6), 0.4f),
    F3(PersonalBest("Floor 3", 8), 0.5f),
    F4(PersonalBest("Floor 4", 5), 0.6f),
    F5(PersonalBest("Floor 5", 5), 0.7f),
    F6(PersonalBest("Floor 6", 7), 0.85f),
    F7(PersonalBest("Floor 7", 10)),
    M1(PersonalBest("Master 1", 6)),
    M2(PersonalBest("Master 2", 6)),
    M3(PersonalBest("Master 3", 8)),
    M4(PersonalBest("Master 4", 5)),
    M5(PersonalBest("Master 5", 5)),
    M6(PersonalBest("Master 6", 7)),
    M7(PersonalBest("Master 7", 10)),
    None(null);

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
                None -> -1
            }
        }

    /**
     * Indicates whether the floor is a master mode floor.
     *
     * @return `true` if the floor is a master mode floor (M1 to M7), otherwise `false`.
     */
    val isMM: Boolean
        get() {
            return when (this) {
                E, F1, F2, F3, F4, F5, F6, F7, None -> false
                M1, M2, M3, M4, M5, M6, M7 -> true
            }
        }
}

enum class M7Phases(val displayName: String) {
    P1("P1"), P2("P2"), P3("P3"), P4("P4"), P5("P5"), Unknown("Unknown");
}

