package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalSounds : Module(
    name = "Terminal Sounds",
    category = Category.FLOOR7,
    description = "Plays a sound whenever you click in a terminal."
){
    private val defaultSounds = arrayListOf("mob.blaze.hit", "random.pop", "random.orb", "random.break", "mob.guardian.land.hit", "Custom")

    val clickSounds by BooleanSetting("Click Sounds", default = true, description = "Replaces the click sounds in terminals.")
    private val sound by SelectorSetting("Click Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you click in a terminal.").withDependency { clickSounds }
    private val customSound by StringSetting("Custom Click Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && clickSounds }
    private val clickVolume by NumberSetting("Click Volume", 1f, 0, 1, .01f, description = "Volume of the sound.").withDependency { clickSounds }
    private val clickPitch by NumberSetting("Click Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.").withDependency { clickSounds }
    val reset by ActionSetting("Play sound", description = "Plays the sound with the current settings.") {
        PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], clickVolume, clickPitch)
    }
    val completeSounds by BooleanSetting("Complete Sounds", default = false, description = "Plays a sound when you complete a terminal.")
    private val cancelLastClick by BooleanSetting("Cancel Last Click", default = false, description = "Cancels the last click sound instead of playing both click and completion sound.").withDependency { clickSounds && completeSounds }
    private val completedSound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you complete the terminal.").withDependency { completeSounds }
    private val customCompleteSound by StringSetting("Custom Completion Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32
    ).withDependency { completedSound == defaultSounds.size - 1 && completeSounds }
    private val completeVolume by NumberSetting("Completion Volume", 1f, 0, 1, .01f, description = "Volume of the sound.").withDependency { completeSounds }
    private val completePitch by NumberSetting("Completion Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.").withDependency { completeSounds }
    val playCompleteSound by ActionSetting("Play sound", description = "Plays the sound with the current settings.") {
        PlayerUtils.playLoudSound(if (completedSound == defaultSounds.size - 1) customCompleteSound else defaultSounds[completedSound], completeVolume, completePitch)
    }

    private var lastPlayed = System.currentTimeMillis()

    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent){
        with(event.packet) {
            if (
                this is S29PacketSoundEffect &&
                soundName == "note.pling" &&
                volume == 8f &&
                pitch == 4.047619f &&
                shouldReplaceSounds
            ) event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiEvent.GuiMouseClickEvent) {
        if (!shouldReplaceSounds) return
        val slot = (event.gui as? GuiChest)?.slotUnderMouse?.slotIndex ?: return
        clickSlot(slot)
    }

    @SubscribeEvent
    fun onCustomSlotClick(event: GuiEvent.CustomTermGuiClick) {
        if (!shouldReplaceSounds) return
        clickSlot(event.slot)
    }

    @SubscribeEvent
    fun onTermComplete(event: TerminalSolvedEvent) {
        if (shouldReplaceSounds && event.playerName != mc.thePlayer?.name || (!completeSounds && !clickSounds)) mc.thePlayer.playSound("note.pling", 8f, 4f)
        else if (shouldReplaceSounds && completeSounds && !clickSounds) playCompleteSound()
    }

    init {
        onMessage("The gate has been destroyed!", false, { enabled && shouldReplaceSounds }) { mc.thePlayer.playSound("note.pling", 8f, 4f) }

        onMessage("The Core entrance is opening!", false, { enabled && shouldReplaceSounds }) { mc.thePlayer.playSound("note.pling", 8f, 4f) }
    }

    private fun clickSlot(slot: Int) {
        if (
            (!currentTerm.type.equalsOneOf(TerminalTypes.MELODY, TerminalTypes.ORDER) && slot !in TerminalSolver.currentTerm.solution) ||
            (currentTerm.type == TerminalTypes.ORDER && slot != TerminalSolver.currentTerm.solution.first()) ||
            (currentTerm.type == TerminalTypes.MELODY && slot !in arrayOf(43, 34, 25, 16))
        ) return
        if ((TerminalSolver.currentTerm.solution.size == 1 || (currentTerm.type == TerminalTypes.MELODY && slot == 43)) && completeSounds) {
            if (!cancelLastClick) playTerminalSound()
            playCompleteSound()
        } else playTerminalSound()
    }

    fun playCompleteSound() {
        val sound = if (completedSound == defaultSounds.size - 1) customCompleteSound else defaultSounds[completedSound]
        PlayerUtils.playLoudSound(sound, completeVolume, completePitch)
    }

    private fun playTerminalSound() {
        if (System.currentTimeMillis() - lastPlayed <= 2) return
        val sound = if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound]
        PlayerUtils.playLoudSound(sound, clickVolume, clickPitch)
        lastPlayed = System.currentTimeMillis()
    }

    private val shouldReplaceSounds get() = (currentTerm.type != TerminalTypes.NONE && clickSounds)
}