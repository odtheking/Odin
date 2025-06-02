package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.features.Module
import me.odinmain.utils.addVec
import me.odinmain.utils.equal
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.toVec3
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object TerracottaTimer : Module(
    name = "Terracotta Timer",
    desc = "Displays the time until the terracotta respawns."
) {
    private var terracottaSpawning = CopyOnWriteArrayList<Terracotta>()
    private data class Terracotta(val pos: Vec3, var time: Float)

    init {
        onPacket<S32PacketConfirmTransaction> {
            terracottaSpawning.removeAll {
                it.time -= .05f
                it.time <= 0
            }
        }
    }

    @SubscribeEvent
    fun onBlockPacket(event: BlockChangeEvent) {
        if (DungeonUtils.isFloor(6) && DungeonUtils.inBoss && event.updated.block.isFlowerPot && terracottaSpawning.none { it.pos.equal(event.pos.toVec3().addVec(0.5, 1.5, 0.5)) })
            terracottaSpawning.add(Terracotta(event.pos.toVec3().addVec(0.5, 1.5, 0.5), if (DungeonUtils.floor?.isMM == true) 12f else 15f))
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(6) || terracottaSpawning.isEmpty()) return
        terracottaSpawning.forEach {
            Renderer.drawStringInWorld("${it.time.toFixed()}s", it.pos, getColor(it.time), depth = false, scale = 0.03f)
        }
    }

    private fun getColor(time: Float): Color {
        return when {
            time > 5f -> Colors.MINECRAFT_DARK_GREEN
            time > 2f -> Colors.MINECRAFT_GOLD
            else -> Colors.MINECRAFT_DARK_RED
        }
    }
}