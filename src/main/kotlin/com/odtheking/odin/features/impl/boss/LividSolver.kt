package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.ServerTick
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.BoxStyle
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.renderBoundingBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object LividSolver : Module(
    name = "Livid Solver",
    description = "Provides a visual cue for the correct Livid's location in the boss fight."
) {
    private val hud by TextHUD("Invulnerability Timer", "Shows time remaining on Livid's invulnerability.", example = { invulnText(340) }) {
        val invulnTime = (invulnEndTick - ServerTick.now)
        if (DungeonUtils.inBoss && DungeonUtils.isFloor(5) && invulnTime > 0) invulnText(invulnTime) else null
    }

    private fun invulnText(time: Long): String =
        "§bLivid: ${when {
            time > 260 -> "§a"
            time > 130 -> "§e"
            else -> "§c"
        }}${time}t"

    private val highlightColor by ColorSetting("Highlight Color", Colors.MINECRAFT_LIGHT_PURPLE, true, desc = "Color of the highlight box around Livid.")

    private val lividStartRegex = Regex("^\\[BOSS] Livid: Welcome, you've arrived right on time\\. I am Livid, the Master of Shadows\\.$")
    private val woolLocation = BlockPos(5, 108, 43)
    private var currentLivid = Livid.HOCKEY
    private var invulnEndTick = 0L

    init {
        on<MessageEvent.Chat> {
            if (!DungeonUtils.inDungeons || !DungeonUtils.isFloor(5)) return@on
            if (message.matches(lividStartRegex)) invulnEndTick = ServerTick.now + 340
        }

        on<BlockUpdateEvent> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || pos != woolLocation) return@on
            currentLivid = Livid.entries.find { livid -> livid.wool.defaultBlockState() == updated.block.defaultBlockState() } ?: return@on
            modMessage("Found Livid: §${currentLivid.colorCode}${currentLivid.entityName}")
        }

        on<EntityEvent.SetData> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5)) return@on
            currentLivid.entity = (entity as? Player)?.takeIf { it.name.string == "${currentLivid.entityName} Livid" } ?: return@on
        }

        on<RenderExtractEvent> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || mc.player?.getEffect(MobEffects.BLINDNESS) != null) return@on
            currentLivid.entity?.let { entity ->
                drawStyledBox(entity.renderBoundingBox, highlightColor, BoxStyle.FILLED_OUTLINE, true)
            }
        }

        on<LevelEvent.Load> {
            Livid.entries.forEach { it.entity = null }
            currentLivid = Livid.HOCKEY
            invulnEndTick = 0L
        }
    }

    private enum class Livid(val entityName: String, val colorCode: Char, val color: Color, val wool: Block) {
        VENDETTA("Vendetta", 'f', Colors.WHITE, Blocks.WOOL.pick(DyeColor.WHITE)),
        CROSSED("Crossed", 'd', Colors.MINECRAFT_DARK_PURPLE, Blocks.WOOL.pick(DyeColor.MAGENTA)),
        ARCADE("Arcade", 'e', Colors.MINECRAFT_YELLOW, Blocks.WOOL.pick(DyeColor.YELLOW)),
        SMILE("Smile", 'a', Colors.MINECRAFT_GREEN, Blocks.WOOL.pick(DyeColor.LIME)),
        DOCTOR("Doctor", '7', Colors.MINECRAFT_GRAY, Blocks.WOOL.pick(DyeColor.GRAY)),
        PURPLE("Purple", '5', Colors.MINECRAFT_DARK_PURPLE, Blocks.WOOL.pick(DyeColor.PURPLE)),
        SCREAM("Scream", '9', Colors.MINECRAFT_BLUE, Blocks.WOOL.pick(DyeColor.BLUE)),
        FROG("Frog", '2', Colors.MINECRAFT_DARK_GREEN, Blocks.WOOL.pick(DyeColor.GREEN)),
        HOCKEY("Hockey", 'c', Colors.MINECRAFT_RED, Blocks.WOOL.pick(DyeColor.RED));

        var entity: Player? = null
    }
}