package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.Supply
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.*

object PearlWaypoints : Module(
    name = "Pearl Waypoints",
    description = "Renders waypoints for pearls in Kuudra."
) {
    private val dynamicWaypoints by BooleanSetting("Dynamic Waypoints", false, desc = "Renders waypoints dynamically based on your position.")
    private val dynamicWaypointsColor by ColorSetting("Dynamic Color", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the dynamic waypoints.").withDependency { dynamicWaypoints }
    private val presetWaypoints by BooleanSetting("Preset Waypoints", true, desc = "Renders preset waypoints for pearls.")
    private val hideFarWaypoints by BooleanSetting("Hide Far Waypoints", true, desc = "Hides the waypoints that are not the closest to you.").withDependency { presetWaypoints }

    init {
        on<RenderExtractEvent> {
            if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return@on

            var closest = true
            getOrderedLineups(mc.player?.blockPosition() ?: return@on).forEach { (lineup, color) ->
                lineup.startPos.forEach {
                    if (presetWaypoints) drawWireFrameBox(AABB(it), color)
                }

                lineup.lineups.forEach lineupLoop@{ blockPos ->
                    if ((NoPre.missing.equalsOneOf(Supply.None, Supply.Square) ||
                                (lineup.supply != Supply.Square || enumToLineup[NoPre.missing] == blockPos)) && (!hideFarWaypoints || closest)) {
                        if (presetWaypoints) drawFilledBox(AABB(blockPos), color)
                        if (dynamicWaypoints) {
                            val destinationSupply = if (lineup.supply == Supply.Square) NoPre.missing else lineup.supply
                            calculatePearl(destinationSupply.dropOffSpot)?.let { result ->
                                drawFilledBox(AABB.ofSize(result.upAngle, 0.12, 0.12, 0.12), dynamicWaypointsColor)
                                drawFilledBox(AABB.ofSize(result.flatAngle, 0.12, 0.12, 0.12), dynamicWaypointsColor)
                            }
                            drawWireFrameBox(AABB(lineup.supply.dropOffSpot.above()), dynamicWaypointsColor)
                        }
                    }
                }
                closest = false
            }
        }
    }

    private val enumToLineup = hashMapOf(
        Supply.xCannon to BlockPos(-59, 106, -59),
        Supply.X to BlockPos(-58, 127, -148),
        Supply.Shop to BlockPos(-146, 107, -60),
        Supply.Triangle to BlockPos(-149, 104, -70),
        Supply.Equals to BlockPos(-168, 124, -118),
        Supply.Slash to BlockPos(-65, 109, -162)
    )

    private fun getOrderedLineups(pos: BlockPos): SortedMap<Lineup, Color> {
        return pearlLineups.toSortedMap(
            compareBy { key ->
                key.startPos.minOfOrNull { it.distSqr(pos) } ?: Double.MAX_VALUE
            }
        )
    }

    private const val GRAV = 0.05
    private const val E_VEL = 1.67

    private data class PearlResult(
        val upAngle: Vec3,
        val flatAngle: Vec3,
        val upTiming: Int,
        val flatTiming: Int
    )

    // Made by Aidanmao
    private fun calculatePearl(targetPos: BlockPos): PearlResult? {
        val (posX, posY, posZ) = mc.player?.renderPos ?: return null

        val offX = targetPos.x - posX
        val offY = targetPos.y - (posY + 1.62)
        val offZ = targetPos.z - posZ
        val offHor = hypot(offX, offZ)

        val v2 = E_VEL * E_VEL
        val v4 = v2 * v2
        val g = GRAV * (1.0 + (offHor * 0.0012)) // drag

        val discriminant = v4 - g * (g * (offHor * offHor) + 2 * offY * v2)

        if (discriminant < 0) return PearlResult(Vec3(0.0, 9.0, 0.0), Vec3(0.0, 9.0, 0.0), 0, 0)

        val root = sqrt(discriminant)

        val angle1 = Math.toDegrees(atan((v2 + root) / (g * offHor)))
        val angle2 = Math.toDegrees(atan((v2 - root) / (g * offHor)))

        val uAngle = max(angle1, angle2)
        val fAngle = min(angle1, angle2)

        var uRes = Vec3(0.0, 10.0, 0.0)
        var fRes = Vec3(0.0, 10.0, 0.0)

        var uTiming = 0
        var fTiming = 0

        if (uAngle > 0.0) {
            val pitch = -uAngle
            val radP = Math.toRadians(pitch)
            val radY = -atan2(offX, offZ)

            val vY = E_VEL * sin(Math.toRadians(uAngle))
            val flightTimeFactor = (1.0012).pow(max(offHor / 15, 1.0)) * 0.8
            val fT = (vY + sqrt(vY * vY + 2 * GRAV * (posY + 1.62 - targetPos.y))) / GRAV
            uTiming = floor((fT / (0.992).pow(fT)) * flightTimeFactor).toInt() - 2

            val cosRadP = cos(radP)
            val fX = cosRadP * sin(radY)
            val fY = -sin(radP)
            val fZ = cosRadP * cos(radY)

            val targetX = posX - fX * 10
            val targetY = posY + fY * 10
            val targetZ = posZ + fZ * 10

            uRes = Vec3(targetX, targetY, targetZ)
        }

        if (fAngle > 0.0) {
            val pitch = -fAngle
            val radP = Math.toRadians(pitch)
            val radY = -atan2(offX, offZ)

            val vX = E_VEL * cos(Math.toRadians(fAngle))

            val drag = 0.978
            val ticks = ln(1 - (offHor * (1 - drag) / vX)) / ln(drag)

            fTiming = if (ticks.isNaN()) (offHor / vX).toInt() else ceil(ticks).toInt()

            val cosRadP = cos(radP)
            val fX = cosRadP * sin(radY)
            val fY = -sin(radP)
            val fZ = cosRadP * cos(radY)

            val targetX = posX - fX * 10
            val targetY = posY + 1.2 + fY * 10
            val targetZ = posZ + fZ * 10

            fRes = Vec3(targetX, targetY, targetZ)
        }

        return PearlResult(uRes, fRes, uTiming, fTiming)
    }

    private val pearlLineups: Map<Lineup, Color> = mapOf(
        // Shop
        Lineup(
            supply = Supply.Shop,
            startPos = setOf(BlockPos(-71, 79, -135), BlockPos(-86, 78, -129)),
            lineups = setOf(BlockPos(-146, 107, -60), BlockPos(-147, 111, -69))
        ) to Colors.MINECRAFT_RED,
        // Triangle
        Lineup(
            supply = Supply.Triangle,
            startPos = setOf(BlockPos(-68, 77, -123)),
            lineups = setOf(BlockPos(-149, 104, -70))
        ) to Colors.MINECRAFT_LIGHT_PURPLE,
        // X
        Lineup(
            supply = Supply.X,
            startPos = setOf(BlockPos(-135, 77, -139)),
            lineups = setOf(BlockPos(-59, 115, -71))
        ) to Colors.MINECRAFT_YELLOW,
        Lineup(
            supply = Supply.xCannon,
            startPos = setOf(BlockPos(-131, 79, -114)),
            lineups = setOf(BlockPos(-59, 106, -59), BlockPos(-51, 108, -67), BlockPos(-39, 93, -76))
        ) to Colors.WHITE,
        // Square
        Lineup(
            supply = Supply.Square,
            startPos = setOf(BlockPos(-141, 78, -91)),
            lineups = setOf(
                BlockPos(-59, 106, -59), // cannon
                BlockPos(-58, 127, -148), // X
                BlockPos(-146, 107, -60), // shop
                BlockPos(-149, 104, -70), // triangle
                BlockPos(-168, 124, -118), // equals
                BlockPos(-65, 109, -162) // slash
            )
        ) to Colors.MINECRAFT_AQUA,
        // equals
        Lineup(
            supply = Supply.Equals,
            startPos = setOf(BlockPos(-66, 76, -88)),
            lineups = setOf(BlockPos(-168, 124, -118))
        ) to Colors.MINECRAFT_GREEN,
        // slash
        Lineup(
            supply = Supply.Slash,
            startPos = setOf(BlockPos(-115, 77, -69)),
            lineups = setOf(BlockPos(-65, 109, -162))
        ) to Colors.MINECRAFT_BLUE
    )

    private data class Lineup(val supply: Supply, val startPos: Set<BlockPos>, val lineups: Set<BlockPos>)
}