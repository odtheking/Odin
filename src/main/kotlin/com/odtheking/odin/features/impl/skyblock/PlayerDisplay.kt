package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.SkyblockPlayer
import net.minecraft.network.chat.Component
import kotlin.math.abs

enum class OverlayType {
    ARMOR,
    HEARTS,
    FOOD,
    XP;
}

object PlayerDisplay : Module(
    name = "Player Display",
    description = "Allows to customize the player stat displays (health, defense and more)."
) {
    private val hideElements by DropdownSetting("Hide Elements")
    private val hideArmor by BooleanSetting("Hide Armor", false, desc = "Hides the armor bar.").withDependency { hideElements }
    private val hideFood by BooleanSetting("Hide Food", false, desc = "Hides the food bar.").withDependency { hideElements }
    private val hideHearts by BooleanSetting("Hide Hearts", false, desc = "Hides the hearts.").withDependency { hideElements }
    private val hideXP by BooleanSetting("Hide XP Level", false, desc = "Hides the XP level.").withDependency { hideElements }
    private val hideActionBar by DropdownSetting("Hide Action Bar Elements")
    private val hideHealth by BooleanSetting("Hide Health", true, desc = "Hides the health bar.").withDependency { hideActionBar }
    private val hideMana by BooleanSetting("Hide Mana", true, desc = "Hides the mana bar.").withDependency { hideActionBar }
    private val hideOverflow by BooleanSetting("Hide Overflow Mana", true, desc = "Hides the overflow mana bar.").withDependency { hideActionBar }
    private val hideDefense by BooleanSetting("Hide Defense", true, desc = "Hides the defense bar.").withDependency { hideActionBar }
    private val overflow by DropdownSetting("Overflow Mana")
    private val separateOverflow by BooleanSetting("Separate Overflow Mana", true, desc = "Separates the overflow mana from the mana bar.").withDependency { overflow }
    private val hideZeroSF by BooleanSetting("Hide 0 Overflow", true, desc = "Hides the overflow mana when it's 0.").withDependency { overflow && separateOverflow }

    private val showIcons by BooleanSetting("Show Icons", true, desc = "Shows icons indicating what the number means.")

    private val healthHud by HUD("Health HUD", "Displays the player's health.") { example ->
        val text = when {
            example -> 3000 to 4000
            !LocationUtils.isInSkyblock -> return@HUD 0 to 0
            SkyblockPlayer.currentHealth != 0 && SkyblockPlayer.maxHealth != 0 -> SkyblockPlayer.currentHealth to SkyblockPlayer.maxHealth
            else -> return@HUD 0 to 0
        }
        return@HUD textDim(generateText(text.first, text.second, "❤"), 0, 0, healthColor)
    }
    private val healthColor by ColorSetting("Health Color", Colors.MINECRAFT_RED, true, "The color of the health text.")

    private val manaHud by HUD("Mana HUD", "Displays the player's mana.") { example ->
        val text = when {
            example -> generateText(2000, 20000, "✎") + (if (!separateOverflow) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")

            !LocationUtils.isInSkyblock -> return@HUD 0 to 0
            SkyblockPlayer.maxMana != 0 -> when {
                SkyblockPlayer.currentMana == 0 && separateOverflow -> return@HUD 0 to 0
                else -> generateText(SkyblockPlayer.currentMana, SkyblockPlayer.maxMana, "✎") +
                        (if (!separateOverflow && overflowManaHud.enabled) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")
            }

            else -> return@HUD 0 to 0
        }
        return@HUD textDim(text, 0, 0, manaColor)
    }
    private val manaColor by ColorSetting("Mana Color", Colors.MINECRAFT_AQUA, true, "The color of the mana text.")

    private val overflowManaHud by HUD("Overflow Mana HUD", "Displays the player's overflow mana.") { example ->
        val text = when {
            example -> 333
            !LocationUtils.isInSkyblock -> return@HUD 0 to 0
            separateOverflow -> SkyblockPlayer.overflowMana
            else -> return@HUD 0 to 0
        }
        return@HUD textDim(generateText(text, "ʬ", hideZeroSF), 0, 0, overflowManaColor)
    }
    private val overflowManaColor by ColorSetting("Overflow Mana Color", Colors.MINECRAFT_DARK_AQUA, true, desc = "The color of the overflow mana text.")

    private val defenseHud by HUD("Defense HUD", "Displays the player's defense.") { example ->
        val text = when {
            example -> 1000
            !LocationUtils.isInSkyblock -> return@HUD 0 to 0
            SkyblockPlayer.currentDefense != 0 -> SkyblockPlayer.currentDefense
            else -> return@HUD 0 to 0
        }
        return@HUD textDim(generateText(text, "❈", true), 0, 0, defenseColor)
    }
    private val defenseColor by ColorSetting("Defense Color", Colors.MINECRAFT_GREEN, true, desc = "The color of the defense text.")

    private val ehpHud by HUD("EHP HUD", "Displays the player's effective health (EHP).") { example ->
        val text = when {
            example -> 1000000
            !LocationUtils.isInSkyblock -> return@HUD 0 to 0
            SkyblockPlayer.effectiveHP != 0 -> SkyblockPlayer.effectiveHP
            else -> return@HUD 0 to 0
        }
        return@HUD textDim(generateText(text, "", true), 0, 0, ehpColor)
    }
    private val ehpColor by ColorSetting("EHP color", Colors.MINECRAFT_DARK_GREEN, true, "The color of the effective health text.")

    private val speedHud by HUD("Speed HUD", "Displays the player's speed buff.") { example ->
        val text = when {
            example -> 100
            !LocationUtils.isInSkyblock -> return@HUD 0 to 0
            SkyblockPlayer.currentSpeed != 0 -> SkyblockPlayer.currentSpeed
            else -> return@HUD 0 to 0
        }
        return@HUD textDim(generateText(text, "✦", true), 0, 0, speedColor)
    }
    private val speedColor by ColorSetting("Speed color", Colors.WHITE, true, "The color of the speed text.")

    private val HEALTH_REGEX = Regex("[\\d|,]+/[\\d|,]+\\uE010")
    private val MANA_REGEX = Regex("[\\d|,]+/[\\d|,]+\\uE003( Mana)?")
    private val OVERFLOW_MANA_REGEX = Regex("§?[\\d|,]+\\uE017")
    private val DEFENSE_REGEX = Regex("[\\d|,]+§a\\uE008 Defense")

    @JvmStatic
    fun modifyText(text: Component): Component {
        if (!enabled) return text
        var toReturn = text.string
        toReturn = if (hideHealth) toReturn.replace(HEALTH_REGEX, "") else toReturn
        toReturn = if (hideMana) toReturn.replace(MANA_REGEX, "") else toReturn
        toReturn = if (hideOverflow) toReturn.replace(OVERFLOW_MANA_REGEX, "") else toReturn
        toReturn = if (hideDefense) toReturn.replace(DEFENSE_REGEX, "") else toReturn
        return Component.literal(toReturn.trim())
    }

    private fun generateText(current: Int, max: Int, icon: String): String =
        "${formatNumber(current)}/${formatNumber(max)}${if (showIcons) icon else ""}"

    private fun generateText(current: Int, icon: String, hideZero: Boolean): String =
        if (!hideZero || current != 0) "${formatNumber(current)}${if (showIcons) icon else ""}" else ""

    private fun formatNumber(n: Int): String {
        val absStr = abs(n).toString()
        val len = absStr.length
        val sb = StringBuilder(len + len / 3)
        val rem = len % 3
        if (rem != 0) sb.append(absStr, 0, rem)
        for (i in rem until len step 3) {
            if (sb.isNotEmpty()) sb.append(',')
            sb.append(absStr, i, i + 3)
        }
        return if (n < 0) "-$sb" else sb.toString()
    }

    @JvmStatic
    fun shouldCancelOverlay(type: OverlayType): Boolean {
        if (!enabled || !LocationUtils.isInSkyblock) return false
        return when (type) {
            OverlayType.ARMOR -> hideArmor
            OverlayType.HEARTS -> hideHearts
            OverlayType.FOOD -> hideFood
            OverlayType.XP -> hideXP
        }
    }
}