package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalSounds : Module(
    name = "Terminal Sounds",
    description = "Plays a sound whenever you click in a terminal"
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "random.pop", "random.orb", "random.break", "mob.guardian.land.hit", "Custom")
    private val sound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you click in a terminal")
    private val customSound by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 }
    private val volume by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.")
    private val pitch by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.")
    val reset by ActionSetting("Play sound") { playTerminalSound() }

    private var lastPlayed = System.currentTimeMillis()

    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent){
        with(event.packet) {
            if (
                this !is S29PacketSoundEffect ||
                currentTerm == TerminalTypes.NONE ||
                customSound == "note.pling" ||
                soundName != "note.pling" ||
                volume != 8f ||
                pitch != 4.047619f
            ) return
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