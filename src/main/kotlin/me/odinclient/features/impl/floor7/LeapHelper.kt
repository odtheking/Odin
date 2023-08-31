package me.odinclient.features.impl.floor7

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.events.impl.DrawSlotEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.gui.Gui
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object LeapHelper : Module(
    name = "Leap Helper",
    description = "Shows which player is the most practical to leap to in the M7 boss-fight.",
    category = Category.FLOOR7
) {
    private val NONE = Vec3(0.0, 0.0, 0.0)
    private val messageMap = mapOf(
        "" to NONE,
        "[BOSS] Maxor: I’M TOO YOUNG TO DIE AGAIN!" to Vec3(65.0, 175.0, 53.0),
        "[BOSS] Storm: I should have known that I stood no chance." to Vec3(107.0, 119.0, 93.0),
        "[BOSS] Goldor: You have done it, you destroyed the factory…" to Vec3(54.0, 115.0, 70.0),
        "[BOSS] Necron: You went further than any human before, congratulations." to Vec3(41.0, 64.0, 102.0),
        "[BOSS] Necron: That's a very impressive trick. I guess I'll have to handle this myself." to Vec3(54.0, 65.0, 82.0),
        "[BOSS] Necron: Let's make some space!" to Vec3(54.0, 4.0, 95.0)
    )
    private var currentPos = NONE
    private var closestPlayer = ""

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || DungeonUtils.teammates.isEmpty()) return
        if (DungeonUtils.getPhase() == 3) scanGates()
        if (currentPos == NONE) return
        closestPlayer = DungeonUtils.teammates
            .filter {
                it != mc.thePlayer &&
                if (currentPos.equal(Vec3(54.0, 4.0, 95.0))) it.first.positionVector.yCoord < 54.0 else true // To make sure the player is underneath necron's platform
            }
            .minByOrNull { it.first.positionVector.distanceTo(currentPos) }
            ?.first
            ?.displayNameString
            .noControlCodes
    }

    private val gateBlocks = mapOf(
        BlockPos(8, 118, 50) to Vec3(8.0, 113.0, 51.0),
        BlockPos(18, 118, 132) to Vec3(19.0, 114.0, 132.0),
        BlockPos(100, 118, 122) to Vec3(100.0, 115.0, 121.0)
    )

    private fun scanGates() {
        gateBlocks.entries.forEach { (pos, vec) ->
            val block = mc.theWorld.getBlockState(pos)
            if (block == Blocks.air.defaultState) // Is barrier if gate is closed
                currentPos = vec
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        if (event.message !in messageMap) return
        currentPos = messageMap[event.message] ?: NONE
        closestPlayer = ""
    }

    @SubscribeEvent
    fun onRenderSlot(event: DrawSlotEvent) {
        if (!DungeonUtils.inDungeons || event.slot.inventory?.name != "Spirit Leap") return
        if (event.slot.stack?.displayName.noControlCodes != closestPlayer) return
        Gui.drawRect(event.slot.xDisplayPosition, event.slot.yDisplayPosition, event.slot.xDisplayPosition + 16, event.slot.yDisplayPosition + 16, Color.RED.rgb)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentPos = NONE
        closestPlayer = ""
    }

    private fun Vec3.equal(other: Vec3): Boolean {
        return this.xCoord == other.xCoord && this.yCoord == other.yCoord && this.zCoord == other.zCoord
    }
}