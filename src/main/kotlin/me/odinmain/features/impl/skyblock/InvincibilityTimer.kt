package me.odinmain.features.impl.skyblock

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.constraints.impl.size.AspectRatio
import com.github.stivais.aurora.dsl.constrain
import com.github.stivais.aurora.dsl.px
import com.github.stivais.aurora.elements.Layout.Companion.divider
import com.github.stivais.aurora.elements.impl.Text.Companion.textSupplied
import com.github.stivais.aurora.utils.color
import me.odinmain.events.impl.GuiEvent.DrawSlotOverlayEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.skyblockID
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.getFont
import me.odinmain.utils.ui.image
import me.odinmain.utils.ui.makeFontSetting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Provides visual information about your invincibility items."
) {
    private val font by makeFontSetting()
    private val shadow by BooleanSetting("Shadow", true, description = "Whether to display a shadow behind the text.")

    private val invincibilityAnnounce by BooleanSetting("Announce Invincibility", default = true, description = "Announces when you get invincibility.")
    private val showCooldown by BooleanSetting("Durability Cooldown", default = true, description = "Shows the durability of the mask in the inventory as a durability bar.")
    private val compactMode by BooleanSetting("Compact Mode", default = false, description = "Displays the HUD in a more compact way.")
    private val removeWhenBlank by BooleanSetting("Remove Blank", default = false, description = "Removes the HUD when there is no active invincibility.")

    private val HUD by HUD("Invincibility HUD") {
        needs { DungeonUtils.inDungeons }
        column {
            InvincibilityType.entries.forEach { type ->
                needs { !preview && removeWhenBlank && type.activeTime == 0 && type.currentCooldown == 0 }
                image("huds/${type.name.lowercase()}.png".image(), constrain(x = Pixel.ZERO, w = 100.px, h = AspectRatio(1f)))
                textSupplied(
                    supplier = {
                        when {
                            type.activeTime > 0 -> "${String.format(Locale.US, "%.2f", type.activeTime / 20.0)}s"
                            type.currentCooldown > 0 -> "${String.format(Locale.US, "%.2f", type.currentCooldown / 20.0)}s"
                            else -> "√"
                        } },
                    getFont(font), color { if (type.activeTime == 0 && type.currentCooldown == 0) Colors.MINECRAFT_GREEN.rgba else if (type.activeTime > 0) Colors.MINECRAFT_GOLD.rgba else Colors.MINECRAFT_RED.rgba }
                )
                divider(5.px)
            }
        }
    }.registerSettings(::font, ::shadow, ::compactMode, ::removeWhenBlank
    ).setting(description = "Displays information about your invincibility items.")

    init {
        onWorldLoad {
            InvincibilityType.entries.forEach { it.reset() }
        }

        onMessage(Regex(".*")) {
            InvincibilityType.entries.firstOrNull { type -> it.matches(type.regex) }?.let { type ->
                if (invincibilityAnnounce) partyMessage("${type.name.lowercase().capitalizeFirst()} Procced!")
                type.proc()
            }
        }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        InvincibilityType.entries.forEach { it.tick() }
    }

    @SubscribeEvent
    fun onRenderSlotOverlay(event: DrawSlotOverlayEvent) {
        if (!LocationUtils.isInSkyblock || !showCooldown) return

        val durability = when (event.stack.skyblockID) {
            "BONZO_MASK", "STARRED_BONZO_MASK" -> InvincibilityType.BONZO.currentCooldown.toDouble() / InvincibilityType.BONZO.maxCooldownTime
            "SPIRIT_MASK", "STARRED_SPIRIT_MASK" -> InvincibilityType.SPIRIT.currentCooldown.toDouble() / InvincibilityType.SPIRIT.maxCooldownTime
            else -> return
        }.takeIf { it < 1.0 } ?: return

        RenderUtils.renderDurabilityBar(event.x ?: return, event.y ?: return, durability)
    }

    enum class InvincibilityType(val regex: Regex, private val maxInvincibilityTime: Int, val maxCooldownTime: Int, val color: Color = Color.WHITE) {
        PHOENIX(Regex("^Your Phoenix Pet saved you from certain death!$"), 80, 1200, Colors.MINECRAFT_DARK_RED),
        BONZO(Regex("^Your (?:. )?Bonzo's Mask saved your life!$"), 60, 3600, Colors.MINECRAFT_BLUE),
        SPIRIT(Regex("^Second Wind Activated! Your Spirit Mask saved your life!\$"), 30, 600, Colors.MINECRAFT_DARK_PURPLE);

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