package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.heldItem
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.init.Items
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SwapSound : Module(
    name = "Swap Sound",
    category = Category.DUNGEON,
    description = "Plays a sound when you successfully stonk swap"
) {
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound: Int by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, description = "Which sound to play when you get a secret.")
    private val customSound: String by StringSetting("Custom Sound", "mob.blaze.hit",
        description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting. Do not use the bat death sound or your game will freeze!", length = 32
    ).withDependency { sound == defaultSounds.size - 1 }
    private val volume: Float by NumberSetting("Volume", 1f, 0, 1, .01f, description = "Volume of the sound.")
    private val pitch: Float by NumberSetting("Pitch", 2f, 0, 2, .01f, description = "Pitch of the sound.")
    val reset: () -> Unit by ActionSetting("Play sound") { PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], volume, pitch) }

    var slot: Int? = null
    private val pickaxes = arrayListOf(Items.diamond_pickaxe, Items.golden_pickaxe, Items.wooden_pickaxe, Items.stone_pickaxe, Items.iron_pickaxe)

    init {
        onPacket(C09PacketHeldItemChange::class.java) {
            slot = it.slotId
        }
    }

    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.LeftClickEvent) {
        if (heldItem?.item in pickaxes && mc.thePlayer?.inventory?.mainInventory?.get(slot ?: return)?.item !in pickaxes) {
            mc.addScheduledTask { PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], volume, pitch) }
        }
    }
}