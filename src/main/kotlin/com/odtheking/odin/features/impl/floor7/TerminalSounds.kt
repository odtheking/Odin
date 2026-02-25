package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.createSoundSettings
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.playSoundSettings
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.sounds.SoundEvents

object TerminalSounds : Module(
    name = "Terminal Sounds",
    description = "Plays a sound whenever you click a correct item in a terminal."
){
    val clickSounds by BooleanSetting("Click Sounds", true, desc = "Replaces the click sounds in terminals.")
    private val clickSoundSettings = createSoundSettings("Click Sound", "entity.blaze.hurt") { clickSounds }
    private val completeSounds by BooleanSetting("Complete Sounds", false, desc = "Plays a sound when you complete a terminal.")
    private val completeSoundSettings = createSoundSettings("Completion Sound", "entity.experience_orb.pickup") { completeSounds }
    private val cancelLastClick by BooleanSetting("Cancel Last Click", false, desc = "Cancels the last click sound instead of playing both click and completion sound.").withDependency { clickSounds && completeSounds }

    private val coreRegex = Regex("^The Core entrance is opening!$")
    private val gateRegex = Regex("^The gate has been destroyed!$")
    private var lastPlayed = System.currentTimeMillis()

    init {
        on<TerminalEvent.Solve> {
            if (shouldReplaceSounds && (!completeSounds && !clickSounds)) playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value(), 8f, 4f)
            else if (shouldReplaceSounds && completeSounds && !clickSounds) playSoundSettings(completeSoundSettings())
        }

        on<GuiEvent.SlotClick> (EventPriority.HIGHEST) {
            if (shouldReplaceSounds) playSoundForSlot(slotId, button)
        }

        on<GuiEvent.CustomTermGuiClick> (EventPriority.HIGHEST) {
            if (shouldReplaceSounds) playSoundForSlot(slot, button)
        }

        onReceive<ClientboundSoundPacket> {
            if (sound.value() == SoundEvents.NOTE_BLOCK_PLING.value() && volume == 8f && pitch == 4.047619f && shouldReplaceSounds)
                it.cancel()
        }

        on<ChatPacketEvent> {
            if (!DungeonUtils.inDungeons || !shouldReplaceSounds) return@on
            when {
                value.matches(gateRegex) -> playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
                value.matches(coreRegex) -> playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
            }
        }
    }

    private fun playSoundForSlot(slot: Int, button: Int) {
        with(TerminalUtils.currentTerm ?: return) {
            if ((isClicked && type != TerminalTypes.MELODY) || !canClick(slot, button)) return
            if ((solution.size == 1 || (type == TerminalTypes.MELODY && slot == 43)) && completeSounds) {
                if (!cancelLastClick) playTerminalSound()
                playSoundSettings(completeSoundSettings())
            } else playTerminalSound()
        }
    }

    private fun playTerminalSound() {
        if (System.currentTimeMillis() - lastPlayed <= 2) return
        playSoundSettings(clickSoundSettings())
        lastPlayed = System.currentTimeMillis()
    }

    private inline val shouldReplaceSounds get() = TerminalUtils.currentTerm != null && clickSounds
}