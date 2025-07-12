package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.DropdownSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.SkyblockPlayer
import me.odinmain.utils.ui.drawStringWidth
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

object PlayerDisplay : Module(
    name = "Player Display",
    description = "Allows to customize the player stat displays (health, strength and more)."
) {
    private val hideElements by DropdownSetting("Hide Elements")
    private val hideArmor by BooleanSetting("Hide Armor", true, desc = "Hides the armor bar.").withDependency { hideElements }
    private val hideFood by BooleanSetting("Hide Food", true, desc = "Hides the food bar.").withDependency { hideElements }
    private val hideHearts by BooleanSetting("Hide Hearts", true, desc = "Hides the hearts.").withDependency { hideElements }
    private val hideXP by BooleanSetting("Hide XP Level", true, desc = "Hides the XP level.").withDependency { hideElements }
    private val hideActionBar by DropdownSetting("Hide Action Bar Elements")
    private val hideHealth by BooleanSetting("Hide Health", true, desc = "Hides the health bar.").withDependency { hideActionBar }
    private val hideMana by BooleanSetting("Hide Mana", true, desc = "Hides the mana bar.").withDependency { hideActionBar }
    private val hideOverflow by BooleanSetting("Hide Overflow Mana", true, desc = "Hides the overflow mana bar.").withDependency { hideActionBar }
    private val hideDefense by BooleanSetting("Hide Defense", true, desc = "Hides the defense bar.").withDependency { hideActionBar }
    private val overflow by DropdownSetting("Overflow Mana")
    private val separateOverflow by BooleanSetting("Separate Overflow Mana", true, desc = "Separates the overflow mana from the mana bar.").withDependency { overflow }
    private val hideZeroSF by BooleanSetting("Hide 0 Overflow", true, desc = "Hides the overflow mana when it's 0.").withDependency { overflow && separateOverflow }

    private val showIcons by BooleanSetting("Show Icons", true, desc = "Shows icons indicating what the number means.")

    private val healthHud by HUD("Health Hud", "Displays the player's health.") { example ->
        val text = when {
            example -> 3000 to 4000
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.currentHealth != 0 && SkyblockPlayer.maxHealth != 0 -> SkyblockPlayer.currentHealth to SkyblockPlayer.maxHealth
            else -> return@HUD 0f to 0f
        }

        return@HUD drawStringWidth(generateText(text.first, text.second, "❤"), 1f, 1f, healthColor) + 2f to 10f
    }
    private val healthColor by ColorSetting("Health Color", Colors.MINECRAFT_RED, true, desc = "The color of the health text.")

    private val manaHud by HUD("Mana Hud", "Displays the player's mana.") { example ->
        val text = when {
            example -> generateText(2000, 20000, "✎") + (if(!separateOverflow) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.maxMana != 0 -> when {
                SkyblockPlayer.currentMana == 0 && separateOverflow -> return@HUD 0f to 0f
                else -> generateText(SkyblockPlayer.currentMana, SkyblockPlayer.maxMana, "✎") +
                        (if(!separateOverflow && overflowManaHud.enabled) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")
            }
            else -> return@HUD 0f to 0f
        }

        return@HUD drawStringWidth(text, 2, 2, manaColor) + 2f to 10f
    }
    private val manaColor by ColorSetting("Mana Color", Colors.MINECRAFT_BLUE, true, desc = "The color of the mana text.")

    private val overflowManaHud by HUD("Overflow Mana Hud", "Displays the player's overflow mana.") { example ->
        val text = when {
            example -> 333
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            separateOverflow -> SkyblockPlayer.overflowMana
            else -> return@HUD 0f to 0f
        }

        return@HUD drawStringWidth(generateText(text, "ʬ", hideZeroSF), 1f, 1f, overflowManaColor) + 2f to 10f
    }
    private val overflowManaColor by ColorSetting("Overflow Mana Color", Colors.MINECRAFT_DARK_AQUA, true, desc = "The color of the overflow mana text.")

    private val defenseHud by HUD("Defense Hud", "Displays the player's defense.") { example ->
        val text = when {
            example -> 1000
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.currentDefense != 0 -> SkyblockPlayer.currentDefense
            else -> return@HUD 0f to 0f
        }

        return@HUD drawStringWidth(generateText(text, "❈", true), 1f, 1f, defenseColor) + 2f to 10f
    }
    private val defenseColor by ColorSetting("Defense Color", Colors.MINECRAFT_GREEN, true, desc = "The color of the defense text.")

    private val ehpHUD by HUD("EHP HUD", "Displays the player's effective health (EHP).") { example ->
        val text = when {
            example -> 1000000
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.effectiveHP != 0 -> SkyblockPlayer.effectiveHP
            else -> return@HUD 0f to 0f
        }

        return@HUD drawStringWidth(generateText(text, "", true), 1f, 1f, ehpColor) + 2f to 10f
    }
    private val ehpColor by ColorSetting("EffectiveHealth Color", Colors.MINECRAFT_DARK_GREEN, true, desc = "The color of the effective health text.")

    private val HEALTH_REGEX = Regex("[\\d|,]+/[\\d|,]+❤")
    private val MANA_REGEX = Regex("[\\d|,]+/[\\d|,]+✎( Mana)?")
    private val OVERFLOW_MANA_REGEX = Regex("§?[\\d|,]+ʬ")
    private val DEFENSE_REGEX = Regex("[\\d|,]+§a❈ Defense")

    @JvmStatic
    fun modifyText(text: String): String {
        if (!enabled) return text
        var toReturn = text
        toReturn = if (hideHealth) toReturn.replace(HEALTH_REGEX, "") else toReturn
        toReturn = if (hideMana) toReturn.replace(MANA_REGEX, "") else toReturn
        toReturn = if (hideOverflow) toReturn.replace(OVERFLOW_MANA_REGEX, "") else toReturn
        toReturn = if (hideDefense) toReturn.replace(DEFENSE_REGEX, "") else toReturn
        return toReturn.trim()
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

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.isCanceled) return // don't override other mods cancelling the event.
        if(!LocationUtils.isInSkyblock) return
        event.isCanceled = when (event.type) {
            RenderGameOverlayEvent.ElementType.ARMOR -> hideArmor
            RenderGameOverlayEvent.ElementType.HEALTH -> hideHearts
            RenderGameOverlayEvent.ElementType.FOOD -> hideFood
            RenderGameOverlayEvent.ElementType.EXPERIENCE -> hideXP
            else -> return
        }
    }
}