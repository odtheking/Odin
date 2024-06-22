package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Plays a sound whenever you get a secret. Do not use the bat death sound or your game will freeze!
 * @author Aton
 */
object SecretChime : Module(
    name = "Secret Chime",
    category = Category.DUNGEON,
    description = "Plays a sound whenever you get a secret."
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound: Int by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you get a secret.")
    private val customSound: String by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting. Do not use the bat death sound or your game will freeze!", length = 32
    ).withDependency { sound == defaultSounds.size - 1 }
    private val volume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.")
    private val pitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.")
    val reset: () -> Unit by ActionSetting("Play sound") { playSecretSound() }
    private val inBoss: Boolean by BooleanSetting("In Boss Room", false, description = "Prevent playing the sound if in boss room.")

    private var lastPlayed = System.currentTimeMillis()

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent) {
        playSecretSound()
    }

    private fun playSecretSound() {
        if (inBoss && DungeonUtils.inBoss) return
        if (System.currentTimeMillis() - lastPlayed <= 10) return
        val sound = if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound]
        PlayerUtils.playLoudSound(sound, volume, pitch)
        lastPlayed = System.currentTimeMillis()
    }
}