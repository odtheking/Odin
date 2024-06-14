package me.odinmain.utils.skyblock.dungeon

import me.odinmain.utils.render.Color
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation


/**
 * Data class representing a player in a dungeon, including their name, class, skin location, and associated player entity.
 *
 * @property name The name of the player.
 * @property clazz The player's class, defined by the [Classes] enum.
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

enum class Blessings(
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

