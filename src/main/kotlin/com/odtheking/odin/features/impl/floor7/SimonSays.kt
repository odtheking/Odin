package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.AABB

object SimonSays : Module(
    name = "Simon Says",
    description = "Shows a solution for the Simon Says device."
) {
    private val firstColor by ColorSetting("First Color", Colors.MINECRAFT_GREEN.withAlpha(0.5f), true, desc = "The color of the first button.")
    private val secondColor by ColorSetting("Second Color", Colors.MINECRAFT_GOLD.withAlpha(0.5f), true, desc = "The color of the second button.")
    private val thirdColor by ColorSetting("Third Color", Colors.MINECRAFT_RED.withAlpha(0.5f), true, desc = "The color of the buttons after the second.")
    private val style by SelectorSetting("Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "The style of the box rendering.")
    private val announceProgress by BooleanSetting("Announce Progress", true, desc = "Sends a message in chat when you click a button, showing your progress.")
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, desc = "Blocks wrong clicks, shift will override this.")
    private val blockWrongStart by BooleanSetting("Block Wrong on Start", false, desc = "Blocks wrong clicks on the start button during first phase.")
    private val maxStartClicks by NumberSetting("Max Start Clicks", 4, 1, 10, 1, desc = "Maximum number of start button clicks allowed during first phase.").withDependency { blockWrongStart }
    private val customClickSounds by BooleanSetting("Custom Click Sounds", false, desc = "Custom Click Sounds for blocked and unblocked clicks.")
    private val soundsDropdown by DropdownSetting("Custom Sounds Dropdown")
    private val correctClick = createSoundSettings("Correct Sound", "entity.experience_orb.pickup") { soundsDropdown }
    private val blockedClick = createSoundSettings("Wrong Sound", "entity.blaze.hurt") { soundsDropdown && blockWrong }

    private val startButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var lastLanternTick = -1
    private var clickNeeded = 0
    private var firstPhase = true
    private var startClickCounter = 0

    private fun resetSolution() {
        clickInOrder.clear()
        clickNeeded = 0
        lastLanternTick = -1
    }

    init {
        on<WorldEvent.Load> {
            resetSolution()
            firstPhase = true
            startClickCounter = 0
        }

        on<ChatPacketEvent> {
            if (value == "[BOSS] Goldor: Who dares trespass into my domain?") startClickCounter = 0
        }

        on<BlockUpdateEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            if (pos == startButton && updated.block == Blocks.STONE_BUTTON && updated.getValue(BlockStateProperties.POWERED)) {
                resetSolution()
                firstPhase = true
                return@on
            }

            if (pos.y !in 120..123 || pos.z !in 92..95) return@on

            when (pos.x) {
                111 ->
                    if (updated.block == Blocks.OBSIDIAN && old.block == Blocks.SEA_LANTERN && pos !in clickInOrder) {
                        clickInOrder.add(pos.immutable())
                        lastLanternTick = 0
                        if (!firstPhase) return@on
                        devMessage(if (clickInOrder.size == 2) "size == 2 reverse." else if (clickInOrder.size == 3) "size == 3 reverse again + skip first" else return@on)
                        when (clickInOrder.size) {
                            2 -> clickInOrder.reverse()
                            3 -> clickInOrder.removeAt(clickInOrder.lastIndex - 1)
                        }
                    }

                110 ->
                    if (updated.block == Blocks.AIR) resetSolution()
                    else if (old.block == Blocks.STONE_BUTTON && updated.getValue(BlockStateProperties.POWERED)) {
                        clickNeeded = clickInOrder.indexOf(pos.east()) + 1
                        if (clickNeeded >= clickInOrder.size) {
                            resetSolution()
                            firstPhase = false
                        }
                    }
            }
        }

        on<TickEvent.Server> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !firstPhase) return@on

            if (lastLanternTick++ > 10 && grid.count { mc.level?.getBlockState(it)?.block == Blocks.STONE_BUTTON } > 8) {
                devMessage("Grid reset detected. (${clickInOrder.size})")
                firstPhase = false
                startClickCounter = 0
            }
        }

        on<BlockInteractEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            if (pos == startButton && firstPhase && blockWrongStart) {
                if (startClickCounter++ >= maxStartClicks && mc.player?.isShiftKeyDown == false) {
                    if (customClickSounds) playSoundSettings(blockedClick())
                    cancel()
                    return@on
                } else if (customClickSounds) playSoundSettings(correctClick())
            }

            if (pos.x == 110 && pos.y in 120..123 && pos.z in 92..95) {
                if (announceProgress && clickInOrder.isNotEmpty()) {
                    if (pos == clickInOrder[clickInOrder.size - 1]) sendCommand("pc SS ${clickInOrder.size}/5")
                }
                if (blockWrong && mc.player?.isShiftKeyDown == false && pos.east() != clickInOrder.getOrNull(clickNeeded)) {
                    if (customClickSounds) playSoundSettings(blockedClick())
                    cancel()
                } else if (customClickSounds) playSoundSettings(correctClick())
            }
        }

        on<RenderEvent.Extract> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || clickNeeded >= clickInOrder.size) return@on

            for (index in clickNeeded until clickInOrder.size) {
                with(clickInOrder[index]) {
                    val color = when (index) {
                        clickNeeded -> firstColor
                        clickNeeded + 1 -> secondColor
                        else -> thirdColor
                    }

                    drawStyledBox(AABB(x + 0.05, y + 0.37, z + 0.3, x - 0.15, y + 0.63, z + 0.7), color, style, true)
                }
            }
        }
    }

    private val grid = setOf(
        BlockPos(110, 123, 92), BlockPos(110, 123, 93), BlockPos(110, 123, 94), BlockPos(110, 123, 95),
        BlockPos(110, 122, 92), BlockPos(110, 122, 93), BlockPos(110, 122, 94), BlockPos(110, 122, 95),
        BlockPos(110, 121, 92), BlockPos(110, 121, 93), BlockPos(110, 121, 94), BlockPos(110, 121, 95),
        BlockPos(110, 120, 92), BlockPos(110, 120, 93), BlockPos(110, 120, 94), BlockPos(110, 120, 95),
    )
}