package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalSounds : Module(
    name = "Terminal Sounds",
    desc = "Plays a sound whenever you click a correct item in a terminal."
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "random.pop", "random.orb", "random.break", "mob.guardian.land.hit", "Custom")

    val clickSounds by BooleanSetting("Click Sounds", true, desc = "Replaces the click sounds in terminals.")
    private val sound by SelectorSetting("Click Sound", "mob.blaze.hit", defaultSounds, desc = "Which sound to play when you click in a terminal.").withDependency { clickSounds }
    private val customSound by StringSetting("Custom Click Sound", "mob.blaze.hit",
        desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && clickSounds }
    private val clickVolume by NumberSetting("Click Volume", 1f, 0, 1, .01f, desc = "Volume of the sound.").withDependency { clickSounds }
    private val clickPitch by NumberSetting("Click Pitch", 2f, 0, 2, .01f, desc = "Pitch of the sound.").withDependency { clickSounds }
    private val reset by ActionSetting("Play click sound", desc = "Plays the sound with the current settings.") {
        PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], clickVolume, clickPitch)
    }
    private val completeSounds by BooleanSetting("Complete Sounds", false, desc = "Plays a sound when you complete a terminal.")
    private val cancelLastClick by BooleanSetting("Cancel Last Click", false, desc = "Cancels the last click sound instead of playing both click and completion sound.").withDependency { clickSounds && completeSounds }
    private val completedSound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, desc = "Which sound to play when you complete the terminal.").withDependency { completeSounds }
    private val customCompleteSound by StringSetting("Custom Completion Sound", "mob.blaze.hit",
        desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { completedSound == defaultSounds.size - 1 && completeSounds }
    private val completeVolume by NumberSetting("Completion Volume", 1f, 0, 1, .01f, desc = "Volume of the sound.").withDependency { completeSounds }
    private val completePitch by NumberSetting("Completion Pitch", 2f, 0, 2, .01f, desc = "Pitch of the sound.").withDependency { completeSounds }
    private val playCompleteSound by ActionSetting("Play complete sound", desc = "Plays the sound with the current settings.") {
        PlayerUtils.playLoudSound(if (completedSound == defaultSounds.size - 1) customCompleteSound else defaultSounds[completedSound], completeVolume, completePitch)
    }

    private var lastPlayed = System.currentTimeMillis()

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) = with(event.packet) {
        if (this is S29PacketSoundEffect && soundName == "note.pling" && volume == 8f && pitch == 4.047619f && shouldReplaceSounds)
            event.isCanceled = true
    }

    @SubscribeEvent
    fun onTermComplete(event: TerminalEvent.Solved) {
        if (shouldReplaceSounds && (!completeSounds && !clickSounds)) mc.thePlayer.playSound("note.pling", 8f, 4f)
        else if (shouldReplaceSounds && completeSounds && !clickSounds) playCompleteSound()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSlotClick(event: GuiEvent.MouseClick) {
        if (shouldReplaceSounds) playSoundForSlot((event.gui as? GuiChest)?.slotUnderMouse?.slotIndex ?: return, event.button)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onCustomSlotClick(event: GuiEvent.CustomTermGuiClick) {
        if (shouldReplaceSounds) playSoundForSlot(event.slot, event.button)
    }

    init {
        onMessage(Regex("The gate has been destroyed!"), { enabled && shouldReplaceSounds }) { mc.thePlayer.playSound("note.pling", 8f, 4f) }

        onMessage(Regex("The Core entrance is opening!"), { enabled && shouldReplaceSounds }) { mc.thePlayer.playSound("note.pling", 8f, 4f) }
    }

    private fun playSoundForSlot(slot: Int, button: Int) {
        if (TerminalSolver.currentTerm?.isClicked == true || TerminalSolver.currentTerm?.canClick(slot, button) == false) return
        if ((TerminalSolver.currentTerm?.solution?.size == 1 || (TerminalSolver.currentTerm?.type == TerminalTypes.MELODY && slot == 43)) && completeSounds) {
            if (!cancelLastClick) playTerminalSound()
            playCompleteSound()
        } else playTerminalSound()
    }

    private fun playCompleteSound() {
        PlayerUtils.playLoudSound(if (completedSound == defaultSounds.size - 1) customCompleteSound else defaultSounds[completedSound], completeVolume, completePitch)
    }

    private fun playTerminalSound() {
        if (System.currentTimeMillis() - lastPlayed <= 2) return
        PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], clickVolume, clickPitch)
        lastPlayed = System.currentTimeMillis()
    }

    private inline val shouldReplaceSounds get() = (TerminalSolver.currentTerm != null && clickSounds)
}