package me.odinmain.features.impl.dungeon

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SpiritBear : Module(
    name = "Spirit Bear",
    description = "Displays the current state of Spirit Bear."
) {
    private val hud by HUD("Hud", "Display the Spirit Bear hud.") { example ->
        when {
            example -> "§61.45s"
            !DungeonUtils.isFloor(4) || !DungeonUtils.inBoss -> null
            timer > 0 -> "§6${(timer / 20f).toFixed()}s"
            timer == 0 -> "§aAlive!"
            showNotSpawned -> "§cNot Spawned"
            else -> null
        }?.let { text ->
            drawStringWidth("§eSpirit Bear: $text", 1f, 1f, Colors.WHITE) + 2f to 10f
        } ?: (0f to 0f)
    }
    private val showNotSpawned by BooleanSetting("Show Not Spawned", false, desc = "Show the Spirit Bear hud even when it's not spawned.")

    private val lastBlockLocation = BlockPos(7, 77, 34)
    private var timer = -1 // state: -1=NotSpawned, 0=Alive, 1+=Spawning

    init {
        onPacket<S32PacketConfirmTransaction> { if (timer > 0) timer -- }
        onWorldLoad { timer = -1 }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.isFloor(4) || !DungeonUtils.inBoss || event.pos != lastBlockLocation) return
        when {
            event.updated.block == Blocks.sea_lantern && event.old.block == Blocks.coal_block -> timer = 68 // bear starts to spawn
            event.updated.block == Blocks.coal_block && event.old.block == Blocks.sea_lantern -> timer = -1 // bear dead
        }
    }
}
