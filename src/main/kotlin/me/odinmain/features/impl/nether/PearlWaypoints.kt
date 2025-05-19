package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.SupplyPickUpSpot
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.toAABB
import me.odinmain.utils.ui.Colors
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.math.*

object PearlWaypoints : Module(
    name = "Pearl Waypoints",
    desc = "Renders waypoints for pearls in Kuudra."
) {
    private val hideFarWaypoints by BooleanSetting("Hide Far Waypoints", true, desc = "Hides the waypoints that are not the closest to you.")
    private val dynamicPearlWaypoints by BooleanSetting("Dynamic Peal Waypoints", true, "Changes pearl waypoints to change based on where you are stood.")
    private val showPriorityWaypoints by BooleanSetting("Show Priority Waypoints", false, "Shows waypoints to land at your priority crate.").withDependency { hideFarWaypoints }

    private val supplyNameMap = mapOf(
        SupplyPickUpSpot.xCannon to BlockPos(-110.0, 78.0, -106.0),
        SupplyPickUpSpot.Triangle to BlockPos(-94.0, 78.0, -106.0),
        SupplyPickUpSpot.Equals to BlockPos(-98.0, 78.0, -99.0),
        SupplyPickUpSpot.Slash to BlockPos(-106.0, 78.0, -99.0),
        SupplyPickUpSpot.Shop to BlockPos(-98.0, 78.0, -112.0),
        SupplyPickUpSpot.X to BlockPos(-106.0, 78.0, -112.0),
        SupplyPickUpSpot.None to BlockPos(0, 0, 0)
    )

    private val pearlLineups: Map<Lineup, Color> = mapOf(
        Lineup(
            supply = SupplyPickUpSpot.ShopCorner,
            startPos = setOf(BlockPos(-71, 79, -135)),
            lineups = setOf(BlockPos(0, 0, 0)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.Shop] as BlockPos)
        ) to Colors.MINECRAFT_DARK_RED,
        Lineup(
            supply = SupplyPickUpSpot.Shop,
            startPos = setOf(BlockPos(-86, 78, -129)),
            lineups = setOf(BlockPos(-97, 159, -112)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.Shop] as BlockPos)
        ) to Colors.MINECRAFT_RED,
        Lineup(
            supply = SupplyPickUpSpot.Triangle,
            startPos = setOf(BlockPos(-68, 77, -123)),
            lineups = setOf(BlockPos(-93, 155, -105)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.Triangle] as BlockPos)
        ) to Colors.MINECRAFT_LIGHT_PURPLE,
        Lineup(
            supply = SupplyPickUpSpot.X,
            startPos = setOf(BlockPos(-135, 77, -139)),
            lineups = setOf(BlockPos(-105, 149, -112)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.X] as BlockPos)
        ) to Colors.MINECRAFT_YELLOW,
        Lineup(
            supply = SupplyPickUpSpot.XSafe,
            startPos = setOf(BlockPos(-135, 78, -129)),
            lineups = setOf(BlockPos(-109, 157, -105)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.X] as BlockPos)
        ) to Colors.MINECRAFT_GOLD,
        Lineup(
            supply = SupplyPickUpSpot.xCannon,
            startPos = setOf(BlockPos(-131, 78, -115)),
            lineups = setOf(BlockPos(-109, 163, -105)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.xCannon] as BlockPos)
        ) to Colors.WHITE,
        Lineup(
            supply = SupplyPickUpSpot.xCannonStair,
            startPos = setOf(BlockPos(-135, 76, -124)),
            lineups = setOf(BlockPos(-109, 155, -105)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.xCannon] as BlockPos)
        ) to Colors.MINECRAFT_GRAY,
        Lineup(
            supply = SupplyPickUpSpot.Square,
            startPos = setOf(BlockPos(-141, 78, -91)),
            lineups = setOf(
                BlockPos(-109, 155, -105), // XCannon
                BlockPos(-105, 150, -112), // X
                BlockPos(-97, 105, -111), // Shop
                BlockPos(-93, 107, -105), // Triangle
                BlockPos(-97, 97, -98), // Equals
                BlockPos(-105, 155, -98) // Slash
            ),
            endPos = setOf(supplyNameMap[NoPre.missing] as BlockPos)
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
            ),
            endPos = setOf(supplyNameMap[NoPre.missing] as BlockPos)
        ) to Colors.MINECRAFT_DARK_AQUA,
        Lineup(
            supply = SupplyPickUpSpot.Equals,
            startPos = setOf(BlockPos(-66, 76, -87)),
            lineups = setOf(BlockPos(-97, 153, -98)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.Equals] as BlockPos)
        ) to Colors.MINECRAFT_GREEN,
        Lineup(
            supply = SupplyPickUpSpot.Slash,
            startPos = setOf(BlockPos(-113, 77, -69)),
            lineups = setOf(BlockPos(-105, 157, -99)),
            endPos = setOf(supplyNameMap[SupplyPickUpSpot.Slash] as BlockPos)
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

    private fun cratePriorityPearl(closestSupplyPickUpSpot: SupplyPickUpSpot): Pair<List<BlockPos>, Color> {
        return when (closestSupplyPickUpSpot) {
            SupplyPickUpSpot.X -> when (NoPre.prio) {
                SupplyPickUpSpot.xCannon -> listOf(BlockPos(-130, 160, -114), BlockPos(-109, 155, -105)) to Colors.WHITE
                SupplyPickUpSpot.Square -> listOf(BlockPos(-142, 152, -179)) to Colors.MINECRAFT_AQUA
                else -> emptyList<BlockPos>() to Colors.TRANSPARENT
            }

            SupplyPickUpSpot.Triangle -> when (NoPre.prio) {
                SupplyPickUpSpot.Shop -> listOf(BlockPos(-74, 152, -134)) to Colors.MINECRAFT_RED
                SupplyPickUpSpot.xCannon -> listOf(BlockPos(-121, 121, -120)) to Colors.WHITE
                else -> emptyList<BlockPos>() to Colors.TRANSPARENT
            }

            SupplyPickUpSpot.Equals -> when (NoPre.prio) {
                SupplyPickUpSpot.Shop -> listOf(BlockPos(-76, 126, -134)) to Colors.MINECRAFT_RED
                // TODO: Add pearl waypoint for Equals to Square
                else -> emptyList<BlockPos>() to Colors.TRANSPARENT
            }

            SupplyPickUpSpot.Slash -> when (NoPre.prio) {
                SupplyPickUpSpot.Square -> listOf(BlockPos(-140, 155, -87)) to Colors.MINECRAFT_AQUA
                SupplyPickUpSpot.xCannon -> listOf(BlockPos(-133, 157, -131)) to Colors.WHITE
                else -> emptyList<BlockPos>() to Colors.TRANSPARENT
            }

            else -> emptyList<BlockPos>() to Colors.TRANSPARENT
        }
    }

    private const val RAD_TO_DEG = 180.0 / PI
    private const val DEG_TO_RAD = PI / 180.0
    private const val EVEL = 1.67
    private const val GRAV = 0.08

    // Made by Aidanmao
    fun calculatePearl(landingX: Double, landingY: Double, landingZ: Double): Vec3? {
        val playerX = posX
        val playerY = posY
        val playerZ = posZ

        val offX = landingX - posX
        val offZ = landingZ - posZ
        val offXSq = offX * offX
        val offZSq = offZ * offZ
        val offHorSq = offXSq + offZSq

        if (offHorSq > 10000) return null

        val offY = landingY - (playerY + 1.62)
        val offHor = sqrt(offHorSq)

        val vSq = EVEL * EVEL
        val gravHorSq = GRAV * offHorSq
        val term1 = gravHorSq / (2 * vSq)
        val discrim = vSq - GRAV * (term1 - offY)

        if (discrim < 0) return null

        val sqrtDiscrim = sqrt(discrim)
        val atanFactor = GRAV * offHor

        val vPlusSqrt = (vSq + sqrtDiscrim) / atanFactor
        val angle1 = atan(vPlusSqrt) * RAD_TO_DEG

        val angle = if (angle1 >= 45.0) angle1
        else {
            val angle2 = atan((vSq - sqrtDiscrim) / atanFactor) * RAD_TO_DEG
            if (angle2 >= 45.0) angle2 else return null
        }

        val dragAng = when {
            offHor < 10 -> 1.0
            offHor < 28 -> 1.026 - (offHor - 10) * 0.000944
            offHor < 36 -> 1.033 - (offHor - 28) * 0.00275
            offHor < 45 -> 0.982
            else -> 1.0 - (offHor - 40) * 0.008
        }

        val radP = -(angle * dragAng) * DEG_TO_RAD
        val radY = -atan2(offX, offZ)
        val cosRadP = cos(radP)

        return Vec3(
            playerX - cosRadP * sin(radY) * 10,
            playerY - sin(radP) * 10,
            playerZ + cosRadP * cos(radY) * 10
        )
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return

        var closestSupplyPickUpSpot = SupplyPickUpSpot.None
        var closest = true
        getOrderedLineups(mc.thePlayer.position).forEach { (lineup, color) ->
            lineup.startPos.forEach {
                Renderer.drawBox(aabb = it.toAABB(), color = color, outlineWidth = if (!closest && hideFarWaypoints) 1f else 3f,
                    outlineAlpha = if (!closest && hideFarWaypoints) 0.25f else 1f, fillAlpha = 0f, depth = false)
            }
            if (!dynamicPearlWaypoints) {
                lineup.lineups.forEach lineupLoop@{
                    if (NoPre.missing == SupplyPickUpSpot.None || NoPre.missing == SupplyPickUpSpot.Square)
                        return@lineupLoop Renderer.drawBox(
                            aabb = it.toAABB(),
                            color = color,
                            outlineAlpha = 0f,
                            fillAlpha = if (!closest && hideFarWaypoints) 0f else 3f,
                            depth = false
                        )
                    if (lineup.startPos != setOf(BlockPos(-141, 78, -91)) || blockNameMap[NoPre.missing] == it)
                        Renderer.drawBox(
                            aabb = it.toAABB(),
                            color = color,
                            outlineAlpha = 0f,
                            fillAlpha = if (!closest && hideFarWaypoints) 0f else 3f,
                            depth = false
                        )
                }
            } else {
                lineup.endPos.forEach lineupLoop@{
                    if (NoPre.missing == SupplyPickUpSpot.None || NoPre.missing == SupplyPickUpSpot.Square)
                        return@lineupLoop Renderer.drawBox(
                            aabb = calculatePearl(it.x.toDouble(), it.y.toDouble(), it.z.toDouble())?.toAABB() ?: return@lineupLoop,
                            color = color,
                            outlineAlpha = 0f,
                            fillAlpha = if (!closest && hideFarWaypoints) 0f else 3f,
                            depth = false
                        )
                    if (lineup.startPos != setOf(BlockPos(-141, 78, -91)) || blockNameMap[NoPre.missing] == it)
                        Renderer.drawBox(
                            aabb = calculatePearl(it.x.toDouble(), it.y.toDouble(), it.z.toDouble())?.toAABB() ?: return@lineupLoop,
                            color = color,
                            outlineAlpha = 0f,
                            fillAlpha = if (!closest && hideFarWaypoints) 0f else 3f,
                            depth = false
                        )
                }
            }
            if (closest) closestSupplyPickUpSpot = lineup.supply
            closest = false
        }
        if (!showPriorityWaypoints) return
        val (prioLineup, prioColor) = cratePriorityPearl(closestSupplyPickUpSpot)
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

    private data class Lineup(val supply: SupplyPickUpSpot, val startPos: Set<BlockPos>, val endPos: Set<BlockPos>, val lineups: Set<BlockPos>)
}