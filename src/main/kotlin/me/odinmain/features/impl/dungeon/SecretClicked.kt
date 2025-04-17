package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.positionVector
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toAABB
import me.odinmain.utils.toBlockPos
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object SecretClicked : Module(
    name = "Secret Clicked",
    desc = "Provides both audio and visual feedback when a secret is clicked."
) {
    private val boxesDropdown by DropdownSetting("Secret Boxes Dropdown")
    private val boxes by BooleanSetting("Secret Boxes", true, desc = "Whether or not to render boxes around clicked secrets.").withDependency { boxesDropdown }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { boxesDropdown && boxes }
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.4f), allowAlpha = true, desc = "The color of the box.").withDependency { boxesDropdown && boxes }
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.").withDependency { boxesDropdown && boxes }
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.").withDependency { boxesDropdown && boxes }
    private val lockedColor by ColorSetting("Locked Color", Colors.MINECRAFT_RED.withAlpha(.4f), allowAlpha = true, desc = "The color of the box when the chest is locked.").withDependency { boxesDropdown && boxes }
    private val timeToStay by NumberSetting("Time To Stay", 7, 1, 20, 0.2, desc = "The time the chests should remain highlighted.", unit = "s").withDependency { boxesDropdown && boxes }
    private val useRealSize by BooleanSetting("Use Real Size", true, desc = "Whether or not to use the real size of the block.").withDependency { boxesDropdown && boxes }
    private val boxInBoss by BooleanSetting("Box In Boss", false, desc = "Highlight clicks in boss.").withDependency { boxesDropdown && boxes }
    private val toggleItems by BooleanSetting("Item Boxes", true, desc = "Render boxes for collected items.").withDependency { boxesDropdown && boxes }

    private val chimeDropdownSetting by DropdownSetting("Secret Chime Dropdown")
    private val chime by BooleanSetting("Secret Chime", true, desc = "Whether or not to play a sound when a secret is clicked.").withDependency { chimeDropdownSetting }
    private val defaultSounds = arrayListOf("mob.blaze.hit", "fire.ignite", "random.orb", "random.break", "mob.guardian.land.hit", "note.pling", "Custom")
    private val sound by SelectorSetting("Sound", "mob.blaze.hit", defaultSounds, desc = "Which sound to play when you get a secret.").withDependency { chimeDropdownSetting && chime }
    private val customSound by StringSetting("Custom Sound", "mob.blaze.hit",
        desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting. Do not use the bat death sound or your game will freeze!", length = 32
    ).withDependency { sound == defaultSounds.size - 1 && chimeDropdownSetting && chime}
    private val volume by NumberSetting("Volume", 1f, 0, 1, .01f, desc = "Volume of the sound.").withDependency { chimeDropdownSetting && chime }
    private val pitch by NumberSetting("Pitch", 2f, 0, 2, .01f, desc = "Pitch of the sound.").withDependency { chimeDropdownSetting && chime }
    private val reset by ActionSetting("Play Sound", desc = "Plays the sound with the current settings.") {
        PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], volume, pitch)
    }.withDependency { chimeDropdownSetting && chime }
    private val chimeInBoss by BooleanSetting("Chime In Boss", false, desc = "Prevent playing the sound if in boss room.").withDependency { chimeDropdownSetting && chime }

    private data class Secret(val pos: BlockPos, var locked: Boolean = false)
    private val clickedSecretsList = CopyOnWriteArrayList<Secret>()
    private var lastPlayed = System.currentTimeMillis()

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!boxes || !DungeonUtils.inDungeons || (DungeonUtils.inBoss && !boxInBoss) || clickedSecretsList.isEmpty()) return

        clickedSecretsList.forEach {
            val currentColor = if (it.locked) lockedColor else color
            if (useRealSize) Renderer.drawStyledBlock(it.pos, currentColor, style, lineWidth, depthCheck)
            else Renderer.drawStyledBox(it.pos.toAABB(), currentColor, style, lineWidth, depthCheck)
        }
    }

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent) {
        when {
            event is SecretPickupEvent.Interact            -> secretBox(event.blockPos)
            event is SecretPickupEvent.Bat                 -> secretBox(event.packet.positionVector.toBlockPos())
            event is SecretPickupEvent.Item && toggleItems -> secretBox(event.entity.positionVector.toBlockPos())
        }
        secretChime()
    }

    private fun secretChime() {
        if (!chime || (DungeonUtils.inBoss && !chimeInBoss) || System.currentTimeMillis() - lastPlayed <= 10) return
        PlayerUtils.playLoudSound(if (sound == defaultSounds.size - 1) customSound else defaultSounds[sound], volume, pitch)
        lastPlayed = System.currentTimeMillis()
    }

    private fun secretBox(pos: BlockPos) {
        if (!boxes || (DungeonUtils.inBoss && !boxInBoss) || clickedSecretsList.any { it.pos == pos }) return
        clickedSecretsList.add(Secret(pos))
        runIn(timeToStay * 20) { clickedSecretsList.removeFirstOrNull() }
    }

    init {
        onWorldLoad { clickedSecretsList.clear() }

        onMessage(Regex("That chest is locked!")) { clickedSecretsList.lastOrNull()?.locked = true }
    }
}
