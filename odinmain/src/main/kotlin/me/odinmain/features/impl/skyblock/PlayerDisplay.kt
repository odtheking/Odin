package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.SkyblockPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PlayerDisplay : Module(
    name = "Player Display",
    description = "Displays info about the skyblock player.",
    category = Category.SKYBLOCK,
) {
    private val hideElements: Boolean by DropdownSetting("Hide Elements")
    private val hideArmor: Boolean by BooleanSetting("Hide Armor").withDependency { hideElements }
    private val hideFood: Boolean by BooleanSetting("Hide Food").withDependency { hideElements }
    private val hideHearts: Boolean by BooleanSetting("Hide Hearts").withDependency { hideElements }
    private val hideXP: Boolean by BooleanSetting("Hide XP Level").withDependency { hideElements }
    private val hideActionBar: Boolean by DropdownSetting("Hide Action Bar Elements")
    private val hideHealth: Boolean by BooleanSetting("Hide Health", true).withDependency { hideActionBar }
    private val hideMana: Boolean by BooleanSetting("Hide Mana", true).withDependency { hideActionBar }
    private val hideDefense: Boolean by BooleanSetting("Hide Defense", true).withDependency { hideActionBar }

    private val healthHud: HudElement by HudSetting("Health Hud", 10f, 10f, 1f, true) {
        val text =
            if (it)
                "§c5000/5000❤"
            else if (SkyblockPlayer.currentHealth != 0 && SkyblockPlayer.maxHealth != 0)
                "§c${SkyblockPlayer.currentHealth}/${SkyblockPlayer.maxHealth}❤"
            else return@HudSetting 0f to 0f


        return@HudSetting mcTextAndWidth(text, 2, 2, 2, Color.RED, center = false) * 2f + 2f to 20f
    }
    private val manaHud: HudElement by HudSetting("Mana Hud", 10f, 10f, 1f, true) {
        val text = if (it)
            "§b2000/2000✎"
        else if (SkyblockPlayer.currentMana != 0 && SkyblockPlayer.maxMana != 0)
            "§b${SkyblockPlayer.currentMana}/${SkyblockPlayer.maxMana}✎"
        else return@HudSetting 0f to 0f

        return@HudSetting mcTextAndWidth(text, 2, 2, 2, Color.BLUE, center = false) * 2f + 2f to 20f
    }

    private val defenseHud: HudElement by HudSetting("Defense Hud", 10f, 10f, 1f, true) {
        val text = if (it)
            "§a1000❈"
        else if (SkyblockPlayer.currentDefense != 0)
            "§a${SkyblockPlayer.currentDefense}❈"
        else return@HudSetting 0f to 0f

        return@HudSetting mcTextAndWidth(text, 2, 2, 2, Color.GREEN, center = false) * 2f + 2f to 20f
    }


    fun modifyText(text: String): String {
        if (!enabled) return text
        var toReturn = text
        toReturn = if (hideHealth) toReturn.replace("[\\d|,]+/[\\d|,]+❤".toRegex(), "") else toReturn
        toReturn = if (hideMana) toReturn.replace("[\\d|,]+/[\\d|,]+✎ Mana".toRegex(), "") else toReturn
        toReturn = if (hideDefense) toReturn.replace("[\\d|,]+§a❈ Defense".toRegex(), "") else toReturn
        return toReturn
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.isCanceled) return // don't override other mods cancelling the event.
        event.isCanceled = when (event.type) {
            RenderGameOverlayEvent.ElementType.ARMOR -> hideArmor
            RenderGameOverlayEvent.ElementType.HEALTH -> hideHearts
            RenderGameOverlayEvent.ElementType.FOOD -> hideFood
            RenderGameOverlayEvent.ElementType.EXPERIENCE -> hideXP
            else -> return
        }
    }
}