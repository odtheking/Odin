package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.EntityLeaveWorldEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Plays a sound whenever you get a secret. Do not use the bat death sound or your game will freeze!
 * @author Aton
 */
object SecretChime : Module(
    name = "Secret Chime",
    category = Category.DUNGEON,
    description = "Plays a sound whenever you get a secret. Do not use the bat death sound or your game will freeze!"
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound: Int by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you get a secret.")
    private val customSound: String by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting."
    ).withDependency { sound == defaultSounds.size - 1 }
    private val volume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.")
    private val pitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.")
    val reset: () -> Unit by ActionSetting("Play sound") {
        playSecretSound()
    }

    private var lastPlayed = System.currentTimeMillis()
    private val drops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone",
    )

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!DungeonUtils.inDungeons || event.pos == null || !DungeonUtils.isSecret(mc.theWorld?.getBlockState(event.pos) ?: return, event.pos)) return

        playSecretSound()
    }

    /**
     * For item pickup detection. The forge event for item pickups cant be used, because item pickups are handled server side.
     */
    @SubscribeEvent
    fun onRemoveEntity(event: EntityLeaveWorldEvent) {
        if (!DungeonUtils.inDungeons || event.entity !is EntityItem || mc.thePlayer.distanceSquaredTo(event.entity) > 36) return

        if (event.entity.entityItem.displayName.noControlCodes.containsOneOf(drops, ignoreCase = true))
            playSecretSound()
    }

    /**
     * For bat death detection
     */
    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundSourceEvent) {
        if (!DungeonUtils.inDungeons || event.name != "mob.bat.death") return
        playSecretSound()
    }

    private fun playSecretSound() {
        if (System.currentTimeMillis() - lastPlayed <= 10) return
        val sound = if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound]
        PlayerUtils.playLoudSound(sound, volume, pitch)
        lastPlayed = System.currentTimeMillis()
    }
}