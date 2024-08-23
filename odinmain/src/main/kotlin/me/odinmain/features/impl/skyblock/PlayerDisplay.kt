package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.mcTextAndWidth
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.SkyblockPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object PlayerDisplay : Module(
    name = "Player Display",
    description = "Displays info about the skyblock player.",
    category = Category.SKYBLOCK,
) {
    private val hideElements: Boolean by DropdownSetting("Hide Elements")
    private val hideArmor: Boolean by BooleanSetting("Hide Armor", true, description = "Hides the armor bar.").withDependency { hideElements }
    private val hideFood: Boolean by BooleanSetting("Hide Food", true, description = "Hides the food bar.").withDependency { hideElements }
    private val hideHearts: Boolean by BooleanSetting("Hide Hearts", true, description = "Hides the hearts.").withDependency { hideElements }
    private val hideXP: Boolean by BooleanSetting("Hide XP Level", true, description = "Hides the XP level.").withDependency { hideElements }
    private val hideActionBar: Boolean by DropdownSetting("Hide Action Bar Elements")
    private val hideHealth: Boolean by BooleanSetting("Hide Health", true, description = "Hides the health bar.").withDependency { hideActionBar }
    private val hideMana: Boolean by BooleanSetting("Hide Mana", true, description = "Hides the mana bar.").withDependency { hideActionBar }
    private val hideOverflow: Boolean by BooleanSetting("Hide Overflow Mana", true, description = "Hides the overflow mana bar.").withDependency { hideActionBar }
    private val hideDefense: Boolean by BooleanSetting("Hide Defense", true, description = "Hides the defense bar.").withDependency { hideActionBar }
    private val overflow: Boolean by DropdownSetting("Overflow Mana")
    private val separateOverflow: Boolean by BooleanSetting("Separate Overflow Mana", true, description = "Separates the overflow mana from the mana bar.").withDependency { overflow }
    private val hideZeroSF: Boolean by BooleanSetting("Hide 0 Overflow", true, description = "Hides the overflow mana when it's 0.").withDependency { overflow && separateOverflow }

    private val showIcons: Boolean by BooleanSetting("Show Icons", true, description = "Shows icons indicating what the number means.")
    private val thousandSeperator: String by StringSetting("Thousands Separator", "", 1, description = "The Separator between thousands and hundreds.")

    private val healthHud: HudElement by HudSetting("Health Hud", 10f, 10f, 1f, true) { example ->
        val text =
            if (example)
                generateText(5000, 5000, "❤")
            else if(!LocationUtils.inSkyblock) return@HudSetting 0f to 0f
            else if (SkyblockPlayer.currentHealth != 0 && SkyblockPlayer.maxHealth != 0)
                generateText(SkyblockPlayer.currentHealth, SkyblockPlayer.maxHealth, "❤")
            else return@HudSetting 0f to 0f

        return@HudSetting mcTextAndWidth(text, 2, 2, 2, healthColor, center = false) * 2f + 2f to 20f
    }
    private val healthColor: Color by ColorSetting("Health Color", Color.RED, true, description = "The color of the health text.")

    private val manaHud: HudElement by HudSetting("Mana Hud", 10f, 10f, 1f, true) { example ->
        val text = if (example)
            generateText(2000, 20000, "✎") + (if(!separateOverflow) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")
        else if(!LocationUtils.inSkyblock) return@HudSetting 0f to 0f
        else if (SkyblockPlayer.maxMana != 0)
            if(SkyblockPlayer.currentMana == 0 && separateOverflow) return@HudSetting 0f to 0f
            else generateText(SkyblockPlayer.currentMana, SkyblockPlayer.maxMana, "✎") + (if(!separateOverflow && overflowManaHud.enabled) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")
        else return@HudSetting 0f to 0f

        return@HudSetting mcTextAndWidth(text, 2, 2, 2, manaColor, center = false) * 2f + 2f to 20f
    }
    private val manaColor: Color by ColorSetting("Mana Color", Color.BLUE, true, description = "The color of the mana text.")

    private val overflowManaHud: HudElement by HudSetting("Overflow Mana Hud", 10f, 10f, 1f, true) { example ->
        val text = if (example)
            generateText(333, "ʬ", hideZeroSF)
        else if(!LocationUtils.inSkyblock) return@HudSetting 0f to 0f
        else if (separateOverflow)
            generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)
        else return@HudSetting 0f to 0f

        return@HudSetting mcTextAndWidth(text, 2, 2, 2, overflowManaColor, center = false) * 2f + 2f to 20f
    }
    private val overflowManaColor: Color by ColorSetting("Overflow Mana Color", Color.CYAN, true, description = "The color of the overflow mana text.")

    private val defenseHud: HudElement by HudSetting("Defense Hud", 10f, 10f, 1f, true) { example ->
        val text = if (example)
            generateText(1000, "❈", true)
        else if(!LocationUtils.inSkyblock) return@HudSetting 0f to 0f
        else if (SkyblockPlayer.currentDefense != 0)
            generateText(SkyblockPlayer.currentDefense, "❈", true)
        else return@HudSetting 0f to 0f


        return@HudSetting mcTextAndWidth(text, 2, 2, 2, defenseColor, center = false) * 2f + 2f to 20f
    }
    private val defenseColor: Color by ColorSetting("Defense Color", Color.GREEN, true, description = "The color of the defense text.")

    private val eHPHud: HudElement by HudSetting("EffectiveHealth Hud", 10f, 10f, 1f, true) { example ->
        val text = if (example)
            generateText(1000000, "", true)
        else if(!LocationUtils.inSkyblock) return@HudSetting 0f to 0f
        else if (SkyblockPlayer.effectiveHP != 0)
            generateText(SkyblockPlayer.effectiveHP, "", true)
        else return@HudSetting 0f to 0f


        return@HudSetting mcTextAndWidth(text, 2, 2, 2, ehpColor, center = false) * 2f + 2f to 20f
    }
    private val ehpColor: Color by ColorSetting("EffectiveHealth Color", Color.DARK_GREEN, true, description = "The color of the effective health text.")


    fun modifyText(text: String): String {
        if (!enabled) return text
        var toReturn = text
        toReturn = if (hideHealth) toReturn.replace("[\\d|,]+/[\\d|,]+❤".toRegex(), "") else toReturn
        toReturn = if (hideMana) toReturn.replace("[\\d|,]+/[\\d|,]+✎( Mana)?".toRegex(), "") else toReturn
        toReturn = if (hideOverflow) toReturn.replace("§?[\\d|,]+ʬ".toRegex(), "") else toReturn
        toReturn = if (hideDefense) toReturn.replace("[\\d|,]+§a❈ Defense".toRegex(), "") else toReturn
        return toReturn
    }

    private fun generateText(current: Int, max: Int, icon: String): String {
        return "${formatNumberWithCustomSeparator(current)}/${formatNumberWithCustomSeparator(max)}${if (showIcons) icon else ""}"
    }

    private fun generateText(current: Int, icon: String, hideZero: Boolean): String{
        if(hideZero && current == 0) return ""
        return "${formatNumberWithCustomSeparator(current)}${if (showIcons) icon else ""}"
    }

    private fun formatNumberWithCustomSeparator(number: Int): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = thousandSeperator.toCharArray().firstOrNull() ?: return number.toString()
        }
        val formatter = DecimalFormat("#,###", symbols)

        return formatter.format(number)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.isCanceled) return // don't override other mods cancelling the event.
        if(!LocationUtils.inSkyblock) return
        event.isCanceled = when (event.type) {
            RenderGameOverlayEvent.ElementType.ARMOR -> hideArmor
            RenderGameOverlayEvent.ElementType.HEALTH -> hideHearts
            RenderGameOverlayEvent.ElementType.FOOD -> hideFood
            RenderGameOverlayEvent.ElementType.EXPERIENCE -> hideXP
            else -> return
        }
    }
}