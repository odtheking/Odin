package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.events.impl.TerminalSolvedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGui
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalSounds : Module(
    name = "Terminal Sounds",
    category = Category.FLOOR7,
    description = "Plays a sound whenever you click in a terminal"
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "random.pop", "random.orb", "random.break", "mob.guardian.land.hit", "Custom")

    val clickSounds: Boolean by BooleanSetting("Click Sounds", default = true, description = "Replaces the click sounds in terminals")
    private val sound: Int by SelectorSetting("Click Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you click in a terminal").withDependency { clickSounds }
    private val customSound: String by StringSetting("Custom Click Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && clickSounds }
    private val clickVolume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.").withDependency { clickSounds }
    private val clickPitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.").withDependency { clickSounds }
    val reset: () -> Unit by ActionSetting("Play sound") { playTerminalSound() }.withDependency { clickSounds }
    val completeSounds: Boolean by BooleanSetting("Complete Sounds", default = false, description = "Plays a sound when you complete a terminal")
    private val completedSound: Int by SelectorSetting("Complete Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you complete the terminal").withDependency { completeSounds }
    private val customCompleteSound: String by StringSetting("Custom Completion Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { completedSound == defaultSounds.size - 1 && completeSounds }
    private val completeVolume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.").withDependency { completeSounds }
    private val completePitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.").withDependency { completeSounds }
    val playCompleteSound: () -> Unit by ActionSetting("Play sound") { playCompleteSound() }.withDependency { completeSounds }

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

    @SubscribeEvent
    fun onTermComplete(event: TerminalSolvedEvent) {
        if (event.type == TerminalTypes.NONE || mc.currentScreen is TermSimGui || event.playerName != mc.thePlayer?.name) return
        playCompleteSound()
    }

    fun playCompleteSound() {
        val sound = if (completedSound == defaultSounds.size - 1) customCompleteSound else defaultSounds[completedSound]
        mc.addScheduledTask { PlayerUtils.playLoudSound(sound, completeVolume, completePitch) }
    }

    fun playTerminalSound() {
        if (System.currentTimeMillis() - lastPlayed <= 2) return
        val sound = if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound]
        mc.addScheduledTask { PlayerUtils.playLoudSound(sound, clickVolume, clickPitch) }
        lastPlayed = System.currentTimeMillis()
    }
}