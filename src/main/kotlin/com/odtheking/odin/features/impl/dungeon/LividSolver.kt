package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object LividSolver : Module(
    name = "Livid Solver",
    description = "Provides a visual cue for the correct Livid's location in the boss fight."
) {
    private val woolLocation = BlockPos(5, 108, 43)
    private var currentLivid = Livid.HOCKEY

    private val hud by HUD("Invulnerability Timer", "Shows time remaining on Livid's invulnerability.") { example ->
        if (!example && (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || invulnTime <= 0)) return@HUD 0 to 0
        val time = if (example) 390 else invulnTime
        val color = when {
            time > 260 -> "§a"
            time > 130 -> "§e"
            else -> "§c"
        }
        textDim("${color}Livid: ${time}t ", 0, 0)
    }

    private var invulnTime = 0
    private val lividStartRegex = Regex("^\\[BOSS] Livid: Welcome, you've arrived right on time\\. I am Livid, the Master of Shadows\\.$")

    init {
        on<ChatPacketEvent> {
            if (!DungeonUtils.inDungeons || !DungeonUtils.isFloor(5)) return@on
            if (value.matches(lividStartRegex)) invulnTime = 390
        }

        on<BlockUpdateEvent> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || pos != woolLocation) return@on
            currentLivid = Livid.entries.find { livid -> livid.wool.defaultBlockState() == updated.block.defaultBlockState() } ?: return@on
            schedule((mc.player?.getEffect(MobEffects.BLINDNESS)?.duration ?: 0) - 20) {
                modMessage("Found Livid: §${currentLivid.colorCode}${currentLivid.entityName}")
            }
        }

        on<EntityEvent.SetData> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5)) return@on
            schedule((mc.player?.getEffect(MobEffects.BLINDNESS)?.duration ?: 0) - 20) {
                currentLivid.entity = (entity as? Player)?.takeIf { it.name.string == "${currentLivid.entityName} Livid" } ?: return@schedule
            }
        }

        on<RenderEvent.Extract> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || mc.player?.getEffect(MobEffects.BLINDNESS) != null) return@on
            currentLivid.entity?.let { entity ->
                drawWireFrameBox(entity.boundingBox, currentLivid.color, 8f, true)
            }
        }

        on<TickEvent.Server> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5)) return@on
            if (invulnTime > 0) invulnTime--
        }

        on<WorldEvent.Load> {
            currentLivid = Livid.HOCKEY
            currentLivid.entity = null
            invulnTime = 0
        }
    }

    private enum class Livid(val entityName: String, val colorCode: Char, val color: Color, val wool: Block) {
        VENDETTA("Vendetta", 'f', Colors.WHITE, Blocks.WHITE_WOOL),
        CROSSED("Crossed", 'd', Colors.MINECRAFT_DARK_PURPLE, Blocks.MAGENTA_WOOL),
        ARCADE("Arcade", 'e', Colors.MINECRAFT_YELLOW, Blocks.YELLOW_WOOL),
        SMILE("Smile", 'a', Colors.MINECRAFT_GREEN, Blocks.LIME_WOOL),
        DOCTOR("Doctor", '7', Colors.MINECRAFT_GRAY, Blocks.GRAY_WOOL),
        PURPLE("Purple", '5', Colors.MINECRAFT_DARK_PURPLE, Blocks.PURPLE_WOOL),
        SCREAM("Scream", '9', Colors.MINECRAFT_BLUE, Blocks.BLUE_WOOL),
        FROG("Frog", '2', Colors.MINECRAFT_DARK_GREEN, Blocks.GREEN_WOOL),
        HOCKEY("Hockey", 'c', Colors.MINECRAFT_RED, Blocks.RED_WOOL);

        var entity: Player? = null
    }
}