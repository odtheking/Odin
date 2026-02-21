package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.StringUtil

fun playSoundSettings(soundSettings: Triple<String, Float, Float>) {
    val (soundName, volume, pitch) = soundSettings
    val soundEvent = SoundEvent.createVariableRangeEvent(ResourceLocation.parse(StringUtil.filterText(soundName))) ?: return
    playSoundAtPlayer(soundEvent, volume, pitch)
}

fun playSoundAtPlayer(event: SoundEvent, volume: Float = 1f, pitch: Float = 1f) = mc.execute {
    mc.soundManager.playDelayed(SimpleSoundInstance.forUI(event, pitch, volume), 0)
}

fun setTitle(title: String) {
    mc.gui.setTimes(0, 20, 5)
    mc.gui.setTitle(Component.literal(title))
}

fun alert(title: String, playSound: Boolean = true) {
    setTitle(title)
    if (playSound) playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
}

fun getPositionString(): String {
    with(mc.player?.blockPosition() ?: BlockPos(0, 0, 0)) {
        return "x: $x, y: $y, z: $z"
    }
}