package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.drawCustomBeacon
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks

object KingRelics : Module(
    name = "King Relic",
    description = "Tools for managing M7 relics."
) {
    private val relicHud by HUD("Relic Hud", "Displays the relic timer in the HUD.") { example ->
        if (example) return@HUD textDim("§3Relics: 4.30s", 0, 0)
        if (DungeonUtils.getF7Phase() != M7Phases.P5 || relicTicksToSpawn <= 0) return@HUD 0 to 0
        textDim("§3Relics: ${(relicTicksToSpawn / 20f).toFixed(2)}s", 0, 0)
    }

    private val relicAnnounceTime by BooleanSetting("Relic Time", true, desc = "Announces the time it took to place the relic.")
    private val relicBeacon by BooleanSetting("Relic Beacon", true, desc = "Draws a beacon at the relic cauldron.")
    private val relicSpawnTicks by NumberSetting("Relic Spawn Ticks", 38, 0, 100, desc = "The number of ticks it takes for the relic to spawn.")

    private var currentRelic: Relic? = null
    private var serverTickCounter = 0L
    private var relicPlaceTick = 0L
    private var relicTicksToSpawn = 0
    private var hasAnnouncedSpawn = false

    private val relicPickupRegex = Regex("^\\[BOSS] Necron: All this, for nothing...$")

    private val relicPBs = PersonalBest(this, "Relics")

    init {
        on<ChatPacketEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || !relicPickupRegex.matches(value)) return@on
            relicPlaceTick = serverTickCounter
            relicTicksToSpawn = relicSpawnTicks
            hasAnnouncedSpawn = false
        }

        onSend<ServerboundUseItemOnPacket> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || relicPlaceTick == 0L || hand == InteractionHand.OFF_HAND) return@onSend

            val block = mc.level?.getBlockState(hitResult.blockPos)?.block
            if (!block.equalsOneOf(Blocks.CAULDRON, Blocks.ANVIL)) return@onSend

            Relic.entries.find { it.id == currentRelic?.id }?.let {
                relicPBs.time(it.name, (serverTickCounter - relicPlaceTick) / 20f, message = "§${it.colorCode}${it.name} relic §7placed in §6", sendMessage = relicAnnounceTime)
                relicPlaceTick = 0L
            }
        }

        on<EntityEvent.SetItemSlot> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || currentRelic == null) return@on

            if (stack.item != Items.PLAYER_HEAD) return@on
            Relic.entries.find { it.id == stack.itemId }?.let { relic ->
                if (relicPlaceTick > 0 && !hasAnnouncedSpawn) {
                    modMessage("§${relic.colorCode}${relic.name} relic §7spawned in §6${(serverTickCounter - relicPlaceTick) / 20f}s")
                    hasAnnouncedSpawn = true
                }
            }
        }

        on<RenderEvent.Extract> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || !relicBeacon) return@on

            Relic.entries.forEach {
                if (currentRelic?.id == it.id) {
                    drawCustomBeacon("", it.cauldronPosition, it.color, distance = false)
                }
            }
        }

        on<WorldEvent.Load> {
            relicPlaceTick = 0L
            relicTicksToSpawn = 0
            currentRelic = null
            hasAnnouncedSpawn = false
        }

        on<TickEvent.Server> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5) return@on

            if (relicTicksToSpawn > 0) relicTicksToSpawn--
            currentRelic = Relic.entries.find { mc.player?.inventory?.find { item -> item?.itemId == it.id } != null }
            serverTickCounter++
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