package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.ItemStateRenderer.Companion.drawItemStack
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Provides visual information about your invincibility items."
) {
    private val invincibilityAlert by BooleanSetting("Invincibility Alert", true, desc = "Plays a sound when you get invincibility.")
    private val invincibilityAnnounce by BooleanSetting("Announce Invincibility", true, desc = "Announces when you get invincibility.")
    private val showWhen by SelectorSetting("Show", "Always", listOf("Always", "Any", "When Active", "On Cooldown"), "Controls when invincibility items are shown.")
    private val equippedMaskColor by ColorSetting("Equipped Mask", Colors.MINECRAFT_DARK_PURPLE, desc = "Color of the equipped mask in the HUD. (Bonzo/Spirit)")

    private val showSpirit by BooleanSetting("Show Spirit Mask", true, desc = "Shows the Spirit Mask in the HUD.")
    private val showBonzo by BooleanSetting("Show Bonzo Mask", true, desc = "Shows the Bonzo Mask in the HUD.")
    private val showPhoenix by BooleanSetting("Show Phoenix Pet", true, desc = "Shows the Phoenix Pet in the HUD.")

    private val hud by HUD(name, "Shows the invincibility time in the HUD.") { example ->
        if ((!DungeonUtils.inDungeons && !example) || (showOnlyInBoss && !DungeonUtils.inBoss && !example)) return@HUD 0 to 0

        val visibleTypes = InvincibilityType.entries.filter { type ->
            (when (type) {
                InvincibilityType.SPIRIT -> showSpirit
                InvincibilityType.BONZO -> showBonzo
                InvincibilityType.PHOENIX -> showPhoenix
            } || example) && (when (showWhen) {
                0 -> true
                1 -> type.activeTime > 0 || type.currentCooldown > 0
                2 -> type.activeTime > 0
                3 -> type.currentCooldown > 0
                else -> true
            } || example)
        }.ifEmpty { return@HUD 0 to 0 }

        var width = 0

        visibleTypes.forEachIndexed { index, type ->
            drawItemStack(type.itemStack, 0, -1 + index * 14)
            val y = index * 14 + 3

            if (type == InvincibilityType.BONZO && mc.player?.getItemBySlot(EquipmentSlot.HEAD)?.itemId?.equalsOneOf("BONZO_MASK", "STARRED_BONZO_MASK") == true ||
                type == InvincibilityType.SPIRIT && mc.player?.getItemBySlot(EquipmentSlot.HEAD)?.itemId?.equalsOneOf("SPIRIT_MASK", "STARRED_SPIRIT_MASK") == true) {
                fill(14, y, 15, y + 9, equippedMaskColor.rgba)
            }
            textDim(
                text = when {
                    type.activeTime > 0 -> "${(type.activeTime / 20f).toFixed()}s"
                    type.currentCooldown > 0 -> "${(type.currentCooldown / 20f).toFixed()}s"
                    else -> "✔"
                },
                16, y,
                if (type.activeTime == 0 && type.currentCooldown == 0) Colors.MINECRAFT_GREEN
                else if (type.activeTime > 0) Colors.MINECRAFT_GOLD else Colors.MINECRAFT_RED
            ).first.let { if (it > width) width = it }
        }

        width + 20 to visibleTypes.size * 14
    }
    private val showOnlyInBoss by BooleanSetting("Show In Boss", false, desc = "Only shows invincibility timers during dungeon boss fights.")

    init {
        on<TickEvent.Server> {
            InvincibilityType.entries.forEach { it.tick() }
        }

        on<ChatPacketEvent> {
            if (!DungeonUtils.inDungeons) return@on
            InvincibilityType.entries.firstOrNull { type -> value.matches(type.regex) }?.let { type ->
                if (invincibilityAnnounce) sendCommand("pc ${type.name.lowercase().capitalizeFirst()} Procced!")
                if (invincibilityAlert) alert(type.name.lowercase().capitalizeFirst())
                type.proc()
            }
        }

        on<WorldEvent.Load> {
            InvincibilityType.entries.forEach { it.reset() }
        }
    }

    private enum class InvincibilityType(
        val regex: Regex,
        private val maxInvincibilityTime: Int,
        val maxCooldownTime: Int,
        val itemStack: ItemStack
    ) {
        SPIRIT(
            Regex("^Second Wind Activated! Your Spirit Mask saved your life!$"),
            30, 600,
            createSkullStack("9bbe721d7ad8ab965f08cbec0b834f779b5197f79da4aea3d13d253ece9dec2")
        ),
        BONZO(
            Regex("^Your (?:. )?Bonzo's Mask saved your life!$"),
            60, 3600,
            createSkullStack("12716ecbf5b8da00b05f316ec6af61e8bd02805b21eb8e440151468dc656549c")
        ),
        PHOENIX(
            Regex("^Your Phoenix Pet saved you from certain death!$"),
            80, 1200,
            createSkullStack("66b1b59bc890c9c97527787dde20600c8b86f6b9912d51a6bfcdb0e4c2aa3c97")
        );

        var activeTime: Int = 0
            private set
        var currentCooldown: Int = 0
            private set

        fun proc() {
            activeTime = maxInvincibilityTime
            currentCooldown = maxCooldownTime
        }

        fun tick() {
            if (currentCooldown > 0) currentCooldown--
            if (activeTime > 0) activeTime--
        }

        fun reset() {
            currentCooldown = 0
            activeTime = 0
        }
    }
}