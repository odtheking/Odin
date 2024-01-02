package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.EntityLeaveWorldEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.passive.EntityBat
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Plays a sound whenever you get a secret. Do not use the bat death sound or your game will freeze!
 * @author Aton
 */
object SecretChime : Module(
    name = "Secret Chime",
    category = Category.DUNGEON,
    description = "Plays a sound whenever you get a secret. Do not use the bat death sound or your game will freeze!",
    tag = TagType.NEW
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound: Int by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you get a secret.")
    private val customSound: String by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting."
    ).withDependency { sound == defaultSounds.size - 1 }
    private val volume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.")
    private val pitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.")

    private var lastPlayed = System.currentTimeMillis()
    private val drops = listOf(
        "Health Potion VIII Splash Potion", //"§5Health Potion VIII Splash Potion"
        "Healing Potion 8 Splash Potion",
        "Healing Potion VIII Splash Potion",
        "Decoy", //"§aDecoy"
        "Inflatable Jerry", //  "§fInflatable Jerry"
        "Spirit Leap", // "§9Spirit Leap"
        "Trap", // "§aTrap"
        "Training Weights", // "§aTraining Weights"
        "Defuse Kit", // "§aDefuse Kit"
        "Dungeon Chest Key", // "§9Dungeon Chest Key"
        "Treasure Talisman", // Name: "§9Treasure Talisman"
        "Revive Stone",
    )

    /**
     * Registers right-clicking a secret.
     */
    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!(DungeonUtils.inDungeons && !DungeonUtils.inBoss) && event.pos != null) return

        if (DungeonUtils.isSecret(mc.theWorld?.getBlockState(event.pos) ?: return, event.pos)) {
            playSecretSound()
        }
    }

    /**
     * For item pickup detection. The forge event for item pickups cant be used, because item pickups are handled server side.
     */
    @SubscribeEvent
    fun onRemoveEntity(event: EntityLeaveWorldEvent) {
        if ((DungeonUtils.inDungeons && !DungeonUtils.inBoss) || mc.thePlayer.getDistanceToEntity(event.entity) > 6) return

        // Check the item name to filter for secrets.
        if ((event.entity is EntityItem && drops.any {
            event.entity.entityItem.displayName.contains(it)
        }) || event.entity is EntityBat) playSecretSound()
    }

    private fun playSecretSound() {
        if (System.currentTimeMillis() - lastPlayed > 10) {
            val sound = if (sound == defaultSounds.size - 1) {
                customSound
            } else defaultSounds[sound]
            PlayerUtils.playLoudSound(sound, volume, pitch)
            lastPlayed = System.currentTimeMillis()
        }
    }
}