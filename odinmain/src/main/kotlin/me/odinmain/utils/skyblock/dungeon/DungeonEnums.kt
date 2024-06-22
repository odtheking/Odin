package me.odinmain.utils.skyblock.dungeon

import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.PersonalBest
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
    var clazz: DungeonClass,
    val locationSkin: ResourceLocation = ResourceLocation("textures/entity/steve.png"),
    val entity: EntityPlayer? = null,
    var isDead: Boolean = false
)

/**
 * Enumeration representing player classes in a dungeon setting.
 *
 * Each class is associated with a specific code and color used for formatting in the game. The classes include Archer,
 * Mage, Berserk, Healer, and Tank.
 *
 * @property color The color associated with the class.
 * @property defaultQuadrant The default quadrant for the class.
 * @property prio The priority of the class.
 *
 */
enum class DungeonClass(
    val color: Color,
    val defaultQuadrant: Int,
    var prio: Int,
) {
    Archer(Color.ORANGE, 0, 2),
    Berserk(Color.DARK_RED,1, 0),
    Healer(Color.PINK, 2, 2),
    Mage(Color.BLUE, 3, 2),
    Tank(Color.DARK_GREEN, 3, 1),
    Unknown(Color.WHITE, 0, 0)
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
 * This enum class defines various floors, including both regular floors (F1 to F7) and special mini-boss floors (M1 to M7).
 * Each floor has an associated floor number and an indicator of whether it is a mini-boss floor.
 *
 * @property floorNumber The numerical representation of the floor, where E represents the entrance floor.
 * @property isInMM Indicates whether the floor is a mini-boss floor (M1 to M7).
 */
enum class Floor(val personalBest: PersonalBest, val secretPercentage: Float = 1f) {
    E(PersonalBest("Entrance", 4), 0f),
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
    M7(PersonalBest("Master 7", 10));

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

enum class M7Phases(val displayName: String) {
    P1("P1"), P2("P2"), P3("P3"), P4("P4"), P5("P5"), Unknown("Unknown");
}

