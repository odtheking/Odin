package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Provides visual information about your invincibility items."
) {
    private val invincibilityAnnounce by BooleanSetting("Announce Invincibility", true, desc = "Announces when you get invincibility.")
    private val showCooldown by BooleanSetting("Durability Cooldown", true, desc = "Shows the durability of the mask in the inventory as a durability bar.")

    private val hud by HUD("Invincibility HUD", "Shows the invincibility time in the HUD.") { example ->
        if (!DungeonUtils.inDungeons && !example) return@HUD 0f to 0f

        var width = 0f

        InvincibilityType.entries.forEachIndexed { index, type ->
            drawItem(type.itemStack, -2f, -1f + index * 14f)

            drawStringWidth(
                text = when {
                    type.activeTime      > 0 -> "${(type.activeTime / 20f).toFixed()}s"
                    type.currentCooldown > 0 -> "${(type.currentCooldown / 20f).toFixed()}s"
                    else -> "✔"
                },
                18f, index * 14f + 3f,
                color = if (type.activeTime == 0 && type.currentCooldown == 0) Colors.MINECRAFT_GREEN
                        else if (type.activeTime > 0) Colors.MINECRAFT_GOLD
                        else Colors.MINECRAFT_RED
            ).let { if (it > width) width = it }
        }

        width + 20 to 14 * 3
    }

    init {
        onWorldLoad {
            InvincibilityType.entries.forEach { it.reset() }
        }

        onMessage(Regex(".*")) {
            InvincibilityType.entries.firstOrNull { type -> it.value.matches(type.regex) }?.let { type ->
                if (invincibilityAnnounce) partyMessage("${type.name.lowercase().capitalizeFirst()} Procced!")
                type.proc()
            }
        }

        onPacket<S32PacketConfirmTransaction> {
            InvincibilityType.entries.forEach { it.tick() }
        }
    }

    @SubscribeEvent
    fun onRenderSlotOverlay(event: GuiEvent.DrawSlotOverlay) {
        if (!LocationUtils.isInSkyblock || !showCooldown) return

        val durability = when (event.stack.skyblockID) {
            "BONZO_MASK", "STARRED_BONZO_MASK" -> InvincibilityType.BONZO.currentCooldown.toDouble() / InvincibilityType.BONZO.maxCooldownTime
            "SPIRIT_MASK", "STARRED_SPIRIT_MASK" -> InvincibilityType.SPIRIT.currentCooldown.toDouble() / InvincibilityType.SPIRIT.maxCooldownTime
            else -> return
        }.takeIf { it < 1.0 } ?: return

        RenderUtils.renderDurabilityBar(event.x ?: return, event.y ?: return, durability)
    }

    enum class InvincibilityType(val regex: Regex, private val maxInvincibilityTime: Int, val maxCooldownTime: Int, val color: Color, val itemStack: ItemStack) {
        PHOENIX(Regex("^Your Phoenix Pet saved you from certain death!$"), 80, 1200, Colors.MINECRAFT_DARK_RED, skullStackFromUrl("http://textures.minecraft.net/texture/66b1b59bc890c9c97527787dde20600c8b86f6b9912d51a6bfcdb0e4c2aa3c97")),
        BONZO(Regex("^Your (?:. )?Bonzo's Mask saved your life!$"), 60, 3600, Colors.MINECRAFT_BLUE, skullStackFromUrl("http://textures.minecraft.net/texture/12716ecbf5b8da00b05f316ec6af61e8bd02805b21eb8e440151468dc656549c")),
        SPIRIT(Regex("^Second Wind Activated! Your Spirit Mask saved your life!\$"), 30, 600, Colors.MINECRAFT_DARK_PURPLE, skullStackFromUrl("http://textures.minecraft.net/texture/9bbe721d7ad8ab965f08cbec0b834f779b5197f79da4aea3d13d253ece9dec2")),;

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
            if (activeTime > 0)      activeTime--
        }

        fun reset() {
            currentCooldown = 0
            activeTime = 0
        }
    }
}