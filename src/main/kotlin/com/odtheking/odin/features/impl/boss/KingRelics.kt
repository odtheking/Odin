package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.drawCustomBeacon
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks

object KingRelics : Module(
    name = "King Relic",
    description = "Highlight correct cauldron and track PB for time placement."
) {
    private val relicBeacon by BooleanSetting("Relic Beacon", true, desc = "Draws a beacon at the relic cauldron.")

    private var currentRelic: Relic? = null
    private var relicPlaceTick = -1

    private val relicPBs = PersonalBest(this, "Relics")

    init {
        on<SetSlotEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || menu !== mc.player?.inventoryMenu || slotIndex != 44) return@on
            currentRelic = Relic.entries.find { it.id == itemStack.itemId }
            if (currentRelic != null && relicPlaceTick == -1) relicPlaceTick = 0
        }

        on<BlockInteractEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || relicPlaceTick < 0) return@on

            val block = mc.level?.getBlockState(pos)?.block
            if (!block.equalsOneOf(Blocks.CAULDRON, Blocks.ANVIL)) return@on

            Relic.entries.find { it.id == currentRelic?.id }?.let {
                relicPBs.time(it.name, relicPlaceTick / 20f, message = "§${it.colorCode}${it.name} relic §7placed in §6")
                relicPlaceTick = -2
            }
        }

        on<RenderExtractEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || !relicBeacon) return@on

            Relic.entries.forEach {
                if (currentRelic?.id == it.id) drawCustomBeacon("", it.cauldronPosition, it.color, distance = false)
            }
        }

        on<LevelEvent.Load> {
            relicPlaceTick = -1
            currentRelic = null
        }

        on<TickEvent.Server> {
            if (DungeonUtils.getF7Phase() == M7Phases.P5 && relicPlaceTick >= 0) relicPlaceTick++
        }
    }

    private enum class Relic(
        val id: String,
        val colorCode: Char,
        val color: Color,
        val cauldronPosition: BlockPos
    ) {
        Green("GREEN_KING_RELIC", 'a', Colors.MINECRAFT_GREEN, BlockPos(49, 7, 44)),
        Purple("PURPLE_KING_RELIC", '5', Colors.MINECRAFT_DARK_PURPLE, BlockPos(54, 7, 41)),
        Blue("BLUE_KING_RELIC", 'b', Colors.MINECRAFT_BLUE, BlockPos(59, 7, 44)),
        Orange("ORANGE_KING_RELIC", '6', Colors.MINECRAFT_GOLD, BlockPos(57, 7, 42)),
        Red("RED_KING_RELIC", 'c', Colors.MINECRAFT_RED, BlockPos(51, 7, 42))
    }
}