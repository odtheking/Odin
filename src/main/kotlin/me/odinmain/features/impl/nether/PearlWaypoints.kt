package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.SupplyPickUpSpot
import me.odinmain.utils.toAABB
import me.odinmain.utils.ui.Colors
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object PearlWaypoints : Module(
    name = "Pearl Waypoints",
    desc = "Renders waypoints for pearls in Kuudra."
) {
    private val hideFarWaypoints by BooleanSetting("Hide Far Waypoints", true, desc = "Hides the waypoints that are not the closest to you.")

    private val pearlLineups: Map<Lineup, Color> = mapOf(
        // Triangle
        Lineup(
            startPos = setOf(BlockPos(-71, 79, -135), BlockPos(-86, 78, -129)),
            lineups = setOf(BlockPos(-97, 157, -114))
        ) to Colors.MINECRAFT_RED,
        // Triangle 2
        Lineup(
            startPos = setOf(BlockPos(-68, 77, -123)),
            lineups = setOf(BlockPos(-96, 161, -105))
        ) to Colors.MINECRAFT_LIGHT_PURPLE,
        // X
        Lineup(
            startPos = setOf(BlockPos(-135, 77, -139)),
            lineups = setOf(BlockPos(-102, 160, -110))
        ) to Colors.MINECRAFT_YELLOW,
        Lineup(
            startPos = setOf(BlockPos(-131, 79, -114)),
            lineups = setOf(BlockPos(-112, 155, -107))
        ) to Colors.WHITE,
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
        ) to Colors.MINECRAFT_AQUA,
        // equals
        Lineup(
            startPos = setOf(BlockPos(-66, 76, -88)),
            lineups = setOf(BlockPos(-101, 160, -100))
        ) to Colors.MINECRAFT_GREEN,
        // slash
        Lineup(
            startPos = setOf(BlockPos(-114, 77, -69)),
            lineups = setOf(BlockPos(-106, 157, -99), BlockPos(-138, 145, -88))
        ) to Colors.MINECRAFT_BLUE
    )

    private val blockNameMap = hashMapOf(
        SupplyPickUpSpot.xCannon to BlockPos(-110, 155, -106),
        SupplyPickUpSpot.X to BlockPos(-46, 120, -150),
        SupplyPickUpSpot.Shop to BlockPos(-46, 135, -139),
        SupplyPickUpSpot.Triangle to BlockPos(-37, 139, -125),
        SupplyPickUpSpot.Equals to BlockPos(-28, 128, -112),
        SupplyPickUpSpot.Slash to BlockPos(-106, 157, -99)
    )

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return

        var closest = true
        getOrderedLineups(mc.thePlayer.position).forEach { (lineup, color) ->
            lineup.startPos.forEach {
                Renderer.drawBox(aabb = it.toAABB(), color = color, outlineWidth = if (!closest && hideFarWaypoints) 1f else 3f,
                    outlineAlpha = if (!closest && hideFarWaypoints) 0.25f else 1f, fillAlpha = 0f, depth = false)
            }
            lineup.lineups.forEach lineupLoop@{
                if (NoPre.missing == SupplyPickUpSpot.None || NoPre.missing == SupplyPickUpSpot.Square)
                    return@lineupLoop Renderer.drawBox(aabb = it.toAABB(), color = color, outlineAlpha = 0f, fillAlpha = if (!closest && hideFarWaypoints) 0f else 3f, depth = false)
                if (lineup.startPos != setOf(BlockPos(-141, 78, -91)) || blockNameMap[NoPre.missing] == it)
                    Renderer.drawBox(aabb = it.toAABB(), color = color, outlineAlpha = 0f, fillAlpha = if (!closest && hideFarWaypoints) 0f else 3f, depth = false)
            }
            closest = false
        }
    }

    private fun getOrderedLineups(pos: Vec3i): SortedMap<Lineup, Color> {
        return pearlLineups.toSortedMap(
            compareBy { key ->
                key.startPos.minOfOrNull { it.distanceSq(pos) } ?: Double.MAX_VALUE
            }
        )
    }

    private data class Lineup(val startPos: Set<BlockPos>, val lineups: Set<BlockPos>)
}