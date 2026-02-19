package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.SecretPickupEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.createSoundSettings
import com.odtheking.odin.utils.getBlockBounds
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.playSoundSettings
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toBlockPos
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import java.util.concurrent.CopyOnWriteArrayList

object SecretClicked : Module(
    name = "Secret Clicked",
    description = "Provides both audio and visual feedback when a secret is clicked."
) {
    private val boxesDropdown by DropdownSetting("Secret Boxes Dropdown")
    private val boxes by BooleanSetting("Secret Boxes", true, desc = "Whether or not to render boxes around clicked secrets.").withDependency { boxesDropdown }
    private val style by SelectorSetting("Style", "Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "The style of the box.").withDependency { boxesDropdown && boxes }
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.4f), true, desc = "The color of the box.").withDependency { boxesDropdown && boxes }
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.").withDependency { boxesDropdown && boxes }
    private val lockedColor by ColorSetting("Locked Color", Colors.MINECRAFT_RED.withAlpha(.4f), true, desc = "The color of the box when the chest is locked.").withDependency { boxesDropdown && boxes }
    private val timeToStay by NumberSetting("Time To Stay", 7, 1, 120, 0.5, desc = "The time the chests should remain highlighted.", unit = "s").withDependency { boxesDropdown && boxes }
    private val boxInBoss by BooleanSetting("Box In Boss", false, desc = "Highlight clicks in boss.").withDependency { boxesDropdown && boxes }
    private val toggleItems by BooleanSetting("Item Boxes", true, desc = "Render boxes for collected items.").withDependency { boxesDropdown && boxes }

    private val chimeDropdownSetting by DropdownSetting("Secret Chime Dropdown")
    private val chime by BooleanSetting("Secret Chime", true, desc = "Whether or not to play a sound when a secret is clicked.").withDependency { chimeDropdownSetting }
    private val soundSettings = createSoundSettings("Chime Sound", "entity.blaze.hurt") { chimeDropdownSetting && chime }
    private val chimeInBoss by BooleanSetting("Chime In Boss", false, desc = "Prevent playing the sound if in boss room.").withDependency { chimeDropdownSetting && chime }

    private data class Secret(val aabb: AABB, var locked: Boolean = false)
    private val clickedSecretsList = CopyOnWriteArrayList<Secret>()
    private var lastPlayed = System.currentTimeMillis()

    init {
        on<SecretPickupEvent.Interact> {
            secretBox(blockPos)
            secretChime()
        }

        on<SecretPickupEvent.Bat> {
            secretBox(position.toBlockPos())
            secretChime()
        }

        on<SecretPickupEvent.Item> {
            if (toggleItems) secretBox(entity.blockPosition())
            secretChime()
        }

        on<ChatPacketEvent> {
            if (value == "That chest is locked!") clickedSecretsList.lastOrNull()?.locked = true
        }

        on<RenderEvent.Extract> {
            if (!boxes || !DungeonUtils.inDungeons || (DungeonUtils.inBoss && !boxInBoss) || clickedSecretsList.isEmpty()) return@on

            clickedSecretsList.forEach { secret ->
                drawStyledBox(secret.aabb, if (secret.locked) lockedColor else color, style, depthCheck)
            }
        }

        on<WorldEvent.Load> {
            clickedSecretsList.clear()
        }
    }

    private fun secretChime() {
        if (!chime || (DungeonUtils.inBoss && !chimeInBoss) || System.currentTimeMillis() - lastPlayed <= 10) return
        playSoundSettings(soundSettings())
        lastPlayed = System.currentTimeMillis()
    }

    private fun secretBox(pos: BlockPos) {
        if (!boxes || (DungeonUtils.inBoss && !boxInBoss) || clickedSecretsList.any { it.aabb.intersects(pos) }) return
        clickedSecretsList.add(Secret(pos.getBlockBounds()?.move(pos) ?: AABB(pos)))
        schedule(timeToStay * 20) { clickedSecretsList.removeFirstOrNull() }
    }
}