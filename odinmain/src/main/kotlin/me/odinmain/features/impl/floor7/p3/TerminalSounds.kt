package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalSounds : Module(
    name = "Terminal Sounds",
    category = Category.FLOOR7,
    description = "Plays a sound whenever you click in a terminal"
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "Custom")
    private val sound: Int by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you get a secret.")
    private val customSound: String by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting. Do not use the bat death sound or your game will freeze!", length = 32
    ).withDependency { sound == defaultSounds.size - 1 }
    private val volume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.")
    private val pitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.")
    val reset: () -> Unit by ActionSetting("Play sound") { playTerminalSound() }

    private var lastPlayed = System.currentTimeMillis()

    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent){
        with(event.packet) {
            if (currentTerm == TerminalTypes.NONE || this !is S29PacketSoundEffect || customSound == "note.pling" ||
                this.soundName != "note.pling" || this.volume != 8f || this.pitch != 4.047619f) return@onPacket
            playTerminalSound()
            event.isCanceled = true
        }
    }

    fun playTerminalSound() {
        if (System.currentTimeMillis() - lastPlayed <= 2) return
        val sound = if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound]
        PlayerUtils.playLoudSound(sound, volume, pitch)
        lastPlayed = System.currentTimeMillis()
    }

}