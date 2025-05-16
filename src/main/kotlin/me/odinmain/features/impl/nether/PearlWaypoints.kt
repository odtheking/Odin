package me.odinmain.features.impl.nether

import jdk.nashorn.internal.ir.Block
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
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
    private val showPriorityWaypoints by BooleanSetting("Show Priority Waypoints", false, "Shows waypoints to land at your priority crate.").withDependency { hideFarWaypoints }

    private val pearlLineups: Map<Lineup, Color> = mapOf(
        Lineup(
            supply = SupplyPickUpSpot.ShopCorner,
            startPos = setOf(BlockPos(-71, 79, -135)),
            lineups = setOf(BlockPos(0, 0, 0))
        ) to Colors.MINECRAFT_DARK_RED,
        Lineup(
            supply = SupplyPickUpSpot.Shop,
            startPos = setOf(BlockPos(-86, 78, -129)),
            lineups = setOf(BlockPos(-97, 159, -112))
        ) to Colors.MINECRAFT_RED,
        Lineup(
            supply = SupplyPickUpSpot.Triangle,
            startPos = setOf(BlockPos(-68, 77, -123)),
            lineups = setOf(BlockPos(-93, 155, -105))
        ) to Colors.MINECRAFT_LIGHT_PURPLE,
        Lineup(
            supply = SupplyPickUpSpot.X,
            startPos = setOf(BlockPos(-135, 77, -139)),
            lineups = setOf(BlockPos(-105, 149, -112))
        ) to Colors.MINECRAFT_YELLOW,
        Lineup(
            supply = SupplyPickUpSpot.XSafe,
            startPos = setOf(BlockPos(-135, 78, -129)),
            lineups = setOf(BlockPos(-109, 157, -105))
        ) to Colors.MINECRAFT_GOLD,
        Lineup(
            supply = SupplyPickUpSpot.xCannon,
            startPos = setOf(BlockPos(-131, 78, -115)),
            lineups = setOf(BlockPos(-109, 163, -105))
        ) to Colors.WHITE,
        Lineup(
            supply = SupplyPickUpSpot.xCannonStair,
            startPos = setOf(BlockPos(-135, 76, -124)),
            lineups = setOf(BlockPos(-109, 155, -105))
        ) to Colors.MINECRAFT_GRAY,
        Lineup(
            supply = SupplyPickUpSpot.Square,
            startPos = setOf(BlockPos(-141, 78, -91)),
            lineups = setOf(
                BlockPos(-109, 155, -105), // XCannon
                BlockPos(-105, 150, -112), // X
                BlockPos(-97, 105, -111), // shop
                BlockPos(-93, 107, -105), // triangle
                BlockPos(-97, 97, -98), // equals
                BlockPos(-105, 155, -98) // slash
            )
        ) to Colors.MINECRAFT_AQUA,
        Lineup(
            supply = SupplyPickUpSpot.SquareLow,
            startPos = setOf(BlockPos(-142, 77, -87)),
            lineups = setOf(
                BlockPos(-109, 151, -105), // XCannon
                BlockPos(-105, 99, -112), // X
                BlockPos(-97, 126, -112), // Shop
                BlockPos(-93, 127, -105), // Triangle
                BlockPos(-97, 102, -98), // Equals
                BlockPos(-105, 149, -98) // Slash
            )
        ) to Colors.MINECRAFT_DARK_AQUA,
        Lineup(
            supply = SupplyPickUpSpot.Equals,
            startPos = setOf(BlockPos(-66, 76, -87)),
            lineups = setOf(BlockPos(-97, 153, -98))
        ) to Colors.MINECRAFT_GREEN,
        Lineup(
            supply = SupplyPickUpSpot.Slash,
            startPos = setOf(BlockPos(-113, 77, -69)),
            lineups = setOf(BlockPos(-105, 157, -99))
        ) to Colors.MINECRAFT_BLUE
    )

    private val blockNameMap = hashMapOf(
        SupplyPickUpSpot.xCannon to BlockPos(-109, 155, -105),
        SupplyPickUpSpot.X to BlockPos(-105, 150, -112),
        SupplyPickUpSpot.Shop to BlockPos(-97, 105, -111),
        SupplyPickUpSpot.Triangle to BlockPos(-93, 107, -105),
        SupplyPickUpSpot.Equals to BlockPos(-97, 97, -98),
        SupplyPickUpSpot.Slash to BlockPos(-105, 155, -98)
    )

    private var prioLineup = emptyList<BlockPos>()
    private var prioColor = Colors.TRANSPARENT
    private fun cratePriorityPearl(closestSupplyPickUpSpot: SupplyPickUpSpot) {
        when (closestSupplyPickUpSpot) {
            SupplyPickUpSpot.X -> when (NoPre.prio) {
                SupplyPickUpSpot.xCannon -> {
                    prioLineup = listOf(BlockPos(-130, 160, -114), BlockPos(-109, 155, -105))
                    prioColor = Colors.WHITE
                }
                SupplyPickUpSpot.Square -> {
                    prioLineup = listOf(BlockPos(-142, 152, -179))
                    prioColor = Colors.MINECRAFT_AQUA
                }
                else -> {
                    prioLineup = emptyList<BlockPos>()
                    prioColor = Colors.TRANSPARENT
                }
            }

            SupplyPickUpSpot.Triangle -> when (NoPre.prio) {
                SupplyPickUpSpot.Shop -> {
                    prioLineup = listOf(BlockPos(-74, 152, -134))
                    prioColor = Colors.MINECRAFT_RED
                }
                SupplyPickUpSpot.xCannon -> {
                    prioLineup = listOf(BlockPos(-121, 121, -120))
                    prioColor = Colors.WHITE
                }
                else -> {
                    prioLineup = emptyList<BlockPos>()
                    prioColor = Colors.TRANSPARENT
                }
            }

            SupplyPickUpSpot.Equals -> when (NoPre.prio) {
                SupplyPickUpSpot.Shop -> {
                    prioLineup = listOf(BlockPos(-76, 126, -134))
                    prioColor = Colors.MINECRAFT_RED
                }
                // TODO: Add pearl waypoint for Equals to Square
                else -> {
                    prioLineup = emptyList<BlockPos>()
                    prioColor = Colors.TRANSPARENT
                }
            }

            SupplyPickUpSpot.Slash -> when (NoPre.prio) {
                SupplyPickUpSpot.Square -> {
                    prioLineup = listOf(BlockPos(-140, 155, -87))
                    prioColor = Colors.MINECRAFT_AQUA
                }
                SupplyPickUpSpot.xCannon -> {
                    prioLineup = listOf(BlockPos(-133, 157, -131))
                    prioColor = Colors.WHITE
                }
                else -> {
                    prioLineup = emptyList<BlockPos>()
                    prioColor = Colors.TRANSPARENT
                }
            }

            else -> {
                prioLineup = emptyList<BlockPos>()
                prioColor = Colors.TRANSPARENT
            }

        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return

        var closestSupplyPickUpSpot = SupplyPickUpSpot.None
        val closest = true
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
            if (closest) closestSupplyPickUpSpot = lineup.supply
        }
        if (!showPriorityWaypoints) return
        prioLineup = emptyList<BlockPos>()
        prioColor = Colors.TRANSPARENT
        cratePriorityPearl(closestSupplyPickUpSpot)
        prioLineup.forEach {
            Renderer.drawBox(it.toAABB(), prioColor, outlineAlpha = 0f, fillAlpha = 3f, depth = false)
        }
    }

    private fun getOrderedLineups(pos: Vec3i): SortedMap<Lineup, Color> {
        return pearlLineups.toSortedMap(
            compareBy { key ->
                key.startPos.minOfOrNull { it.distanceSq(pos) } ?: Double.MAX_VALUE
            }
        )
    }

    private data class Lineup(val supply: SupplyPickUpSpot, val startPos: Set<BlockPos>, val lineups: Set<BlockPos>)
}