package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import me.odinclient.utils.skyblock.ScoreboardUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

object WormFishingLavaESP : Module(
    "Worm fishing ESP",
    description = "Detects lava in precursor remnants above Y=64.",
    category = Category.RENDER
) {

    private val wormFishingLavaESPRadius: Int by NumberSetting("ESP Radius", 50, 50, 150, 5)
    private val wormFishingLavaESPTime: Int by NumberSetting("ESP Time", 750, 1000, 5000, 10, description = "Time in ms between scan cycles.")
    private val wormFishingLavaHideNear: Boolean by BooleanSetting("Hide ESP near lava", description = "Hide the ESP near the lava.")
    private val wormFishingHideFished: Boolean by BooleanSetting("Turn off after a worm", description = "Disables it only for the current session.")

    private val lavaBlocksList: MutableList<BlockPos> = mutableListOf()
    private var lastUpdate: Long = 0
    private val locations = listOf(
        "Precursor Remnants",
        "Lost Precursor City",
        "Magma Fields",
        "Khazad-dûm",
    )
    private var thread: Thread? = null
    private var disabledInSession: Boolean = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !isCrystalHollow()) return
        if (thread?.isAlive == true || lastUpdate + wormFishingLavaESPTime > System.currentTimeMillis()) return
        thread = Thread({
            val blockList: MutableList<BlockPos> = mutableListOf()
            val player = mc.thePlayer.position
            val radius = wormFishingLavaESPRadius
            val vec3i = Vec3i(radius, radius, radius)

            BlockPos.getAllInBox(player.add(vec3i), player.subtract(vec3i)).forEach {
                val blockState = mc.theWorld.getBlockState(it)

                if ((blockState.block == Blocks.lava || blockState.block == Blocks.flowing_lava) && it.y > 64) {
                    blockList.add(it)
                }
            }
            synchronized(lavaBlocksList) {
                lavaBlocksList.clear()
                lavaBlocksList.addAll(blockList)
            }
            lastUpdate = System.currentTimeMillis()
        }, "Worm fishing ESP")
        thread!!.start()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!isCrystalHollow() || disabledInSession) return
        val player = mc.thePlayer.position
        synchronized(lavaBlocksList) {
            lavaBlocksList.forEach { blockPos ->
                if (!wormFishingLavaHideNear || player.distanceSq(blockPos) > 40 && wormFishingLavaHideNear) {
                    RenderUtils.drawBlockBox(blockPos, Color(255, 170, 0), outline = true, fill = true, event.partialTicks)
                }
            }
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (!wormFishingHideFished || event.type.toInt() == 2) return
        if (event.message.unformattedText == "A flaming worm surfaces from the depths!") {
            disabledInSession = true
        }
    }

    @SubscribeEvent
    fun onChangeWorld(event: WorldEvent.Load) {
        if (!wormFishingHideFished || !disabledInSession) return
        disabledInSession = false
    }

    private fun isCrystalHollow(): Boolean {
        return inSkyblock && ScoreboardUtils.sidebarLines.any { s ->
            locations.any { ScoreboardUtils.cleanSB(s).contains(it) }
        }
    }
}
