package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SpiritBear : Module(
    name = "Spirit Bear",
    description = "Displays the current state of Spirit Bear."
) {
    private val hud by HUD("Hud", "Displays the current state of Spirit Bear in the HUD.", false) { example ->
        when {
            example -> "§e1.45s"
            !DungeonUtils.isFloor(4) || !DungeonUtils.inBoss -> null
            timer < 0 -> "§d$kills/$maxKills"
            timer > 0 -> "§e${(timer / 20f).toFixed()}s"
            else -> "§aAlive!"
        }?.let { text ->
            drawStringWidth("§6Spirit Bear: $text", 1f, 1f, Colors.WHITE) + 2f to 10f
        } ?: (0f to 0f)
    }

    private val f4BlockLocations = hashSetOf(
        BlockPos(-3, 77, 33), BlockPos(-9, 77, 31), BlockPos(-16, 77, 26), BlockPos(-20, 77, 20), BlockPos(-23, 77, 13),
        BlockPos(-24, 77, 6), BlockPos(-24, 77, 0), BlockPos(-22, 77, -7), BlockPos(-18, 77, -13), BlockPos(-12, 77, -19),
        BlockPos(-5, 77, -22), BlockPos(1, 77, -24), BlockPos(8, 77, -24), BlockPos(14, 77, -23), BlockPos(21, 77, -19),
        BlockPos(27, 77, -14), BlockPos(31, 77, -8), BlockPos(33, 77, -1), BlockPos(34, 77, 5), BlockPos(33, 77, 12),
        BlockPos(31, 77, 19), BlockPos(27, 77, 25), BlockPos(20, 77, 30), BlockPos(14, 77, 33), BlockPos(7, 77, 34)
    )
    private val m4BlockLocations = hashSetOf(
        BlockPos(-2, 77, 33), BlockPos(-7, 77, 32), BlockPos(-13, 77, 28), BlockPos(-17, 77, 24), BlockPos(-21, 77, 18),
        BlockPos(-23, 77, 13), BlockPos(-24, 77, 7), BlockPos(-24, 77, 2), BlockPos(-23, 77, -4), BlockPos(-21, 77, -9),
        BlockPos(-17, 77, -14), BlockPos(-12, 77, -19), BlockPos(-6, 77, -22), BlockPos(-1, 77, -23), BlockPos(5, 77, -24),
        BlockPos(10, 77, -24), BlockPos(16, 77, -22), BlockPos(21, 77, -19), BlockPos(27, 77, -15), BlockPos(30, 77, -10),
        BlockPos(32, 77, -5), BlockPos(34, 77, 1), BlockPos(34, 77, 7), BlockPos(33, 77, 12), BlockPos(31, 77, 18),
        BlockPos(28, 77, 23), BlockPos(23, 77, 28), BlockPos(18, 77, 31), BlockPos(12, 77, 33), BlockPos(7, 77, 34)
    )
    private val lastBlockLocation = BlockPos(7, 77, 34)
    private inline val blockLocations: HashSet<BlockPos> get() = if (DungeonUtils.floor?.isMM == true) m4BlockLocations else f4BlockLocations
    private inline val maxKills: Int get() = if (DungeonUtils.floor?.isMM == true) 30 else 25

    private var kills = 0
    private var timer = -1 // state: -1=NotSpawned, 0=Alive, 1+=Spawning

    init {
        onWorldLoad {
            kills = 0
            timer = -1
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.isFloor(4) || !DungeonUtils.inBoss || !blockLocations.contains(event.pos)) return
        when {
            event.updated.block == Blocks.sea_lantern && event.old.block == Blocks.coal_block -> {
                if (kills < maxKills) kills++
                if (event.pos == lastBlockLocation) timer = 68
            }

            event.updated.block == Blocks.coal_block && event.old.block == Blocks.sea_lantern -> {
                if (kills > 0) kills--
                if (event.pos == lastBlockLocation) timer = -1
            }
        }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (timer > 0) timer--
    }
}
