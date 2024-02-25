package me.odinmain.features.impl.kuudra

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils.drawBoxOutline
import me.odinmain.utils.render.world.RenderUtils.drawFilledBox
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.toAABB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object PearlWaypoints : Module(
    name = "Pearl Waypoints",
    description = "Renders waypoints for pearls in Kuudra.",
    category = Category.KUUDRA
) {
    private val hideFarWaypoints: Boolean by BooleanSetting("Hide Far Waypoints", false, description = "Hides the waypoints that are far away")

    private val pearlLineups: Map<Lineup, Color> = mapOf(
        // Triangle
        Lineup(
            startPos = setOf(BlockPos(-71, 79, -135), BlockPos(-86, 78, -129)),
            lineups = setOf(BlockPos(-97, 157, -114))
        ) to Color(255, 0, 0),
        // Triangle 2
        Lineup(
            startPos = setOf(BlockPos(-68, 77, -123)),
            lineups = setOf(BlockPos(-96, 161, -105))
        ) to Color(255, 0, 255),
        // X
        Lineup(
            startPos = setOf(BlockPos(-135, 77, -139)),
            lineups = setOf(BlockPos(-102, 160, -110))
        ) to Color(255, 255, 0),
        Lineup(
            startPos = setOf(BlockPos(-131, 79, -114)),
            lineups = setOf(BlockPos(-112, 155, -107))
        ) to Color(255, 255, 255),
        // Square
        Lineup(
            startPos = setOf(BlockPos(-141, 78, -91)),
            lineups = setOf(
                BlockPos(-110, 155, -106), // cannon
                BlockPos(-46, 120, -150), // X
                BlockPos(-46, 135, -139), // shop
                BlockPos(-37, 139, -125), // triangle
                BlockPos(-28, 128, -112), // equals
                BlockPos(-106, 157, -99) // slash
            )
        ) to Color(0, 255, 255),
        // equals
        Lineup(
            startPos = setOf(BlockPos(-66, 76, -88)),
            lineups = setOf(BlockPos(-101, 160, -100))
        ) to Color(0, 255, 0),
        // slash
        Lineup(
            startPos = setOf(BlockPos(-114, 77, -69)),
            lineups = setOf(BlockPos(-106, 157, -99))
        ) to Color(0, 0, 255)
    )

    private val blockNameMap = hashMapOf(
        "Cannon" to BlockPos(-110, 155, -106),
        "X" to BlockPos(-46, 120, -150),
        "Shop" to BlockPos(-46, 135, -139),
        "Triangle" to BlockPos(-37, 139, -125),
        "Equals" to BlockPos(-28, 128, -112),
        "Slash" to BlockPos(-106, 157, -99)
    )


    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (KuudraUtils.phase != 1) return

        var closest = true
        getOrderedLineups(mc.thePlayer.position).forEach { (lineup, color) ->
            lineup.startPos.forEach {
                drawBoxOutline(it.toAABB(), color, if (!closest && hideFarWaypoints) 1f else 3f, phase = true)
            }

            lineup.lineups.forEach {
                if (!closest && hideFarWaypoints) return@forEach
                if (NoPre.missing == "") return@forEach drawFilledBox(it.toAABB(), color, true)
                if (lineup.startPos == setOf(BlockPos(-141, 78, -91)) && blockNameMap[NoPre.missing] != it) return@forEach

                drawFilledBox(it.toAABB(), color, true)
            }
            closest = false
        }
    }

    private fun getOrderedLineups(pos: Vec3i): SortedMap<Lineup, Color> {
        return pearlLineups.toSortedMap(
            compareBy { key ->
                key.startPos.minOfOrNull { startPos ->
                    startPos.distanceSq(pos)
                } ?: Double.MAX_VALUE
            }
        )
    }

    private data class Lineup(val startPos: Set<BlockPos>, val lineups: Set<BlockPos>)
}