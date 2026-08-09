package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.DevModule
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.drawCylinder
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.render.drawWireFrameBox
import net.minecraft.world.phys.AABB
import kotlin.math.ceil
import kotlin.math.sqrt

@DevModule
object RenderTest : Module(
    name = "Render Test",
    description = "Test rendering stuff",
    category = Category.RENDER,
) {

    val boxStyle by SelectorSetting(
        name = "Styled Box Style",
        default = "FilledBox",
        options = listOf("FilledBox", "WireFrame", "Both"),
        desc = "Style of the styled box"
    )

    val boxCount by NumberSetting("Box Count", 0, 0, 200000, 1, desc = "Approx number of boxes to render")
    val boxLevels by NumberSetting("Box Levels", 4, 1, 12, 1, desc = "Vertical layers for boxes")

    init {
        on<RenderEvent.Extract> {
            val player = mc.player ?: return@on

            val frameDelta = mc.deltaTracker.realtimeDeltaTicks
            val playerPos = player.getPosition(frameDelta)
            val playerBB = player.type.dimensions.makeBoundingBox(playerPos)

            drawWireFrameBox(
                aabb = playerBB.inflate(2.0, 2.0, 2.0),
                color = Color(0x7eb4c7ff),
            )

            drawLine(
                points = listOf(
                    player.getEyePosition(frameDelta),
                    player.getEyePosition(frameDelta).add(0.0, 5.0, 0.0)
                ),
                color = Color(0x7eb4c7ff),
                depth = false,
            )

            drawCylinder(
                center = playerPos,
                radius = 1f,
                height = 1f,
                color = Color(0x7eb4c7ff),
            )

            drawStyledBox(
                aabb = playerBB.inflate(1.0, 1.0, 1.0),
                color = Color(0x7eb4c7ff),
                style = boxStyle,
            )

            drawFilledBox(
                aabb = playerBB.inflate(0.5, 0.5, 0.5),
                color = Color(0x7eb4c7ff),
            )

            try {
                run {
                    val desiredCount = boxCount.coerceAtLeast(0)
                    val layers = boxLevels.coerceAtLeast(1)

                    if (desiredCount > 0) {
                        val perLayer = ceil(desiredCount.toDouble() / layers.toDouble()).toInt()
                        val side = ceil(sqrt(perLayer.toDouble())).toInt()
                        val range = side / 2

                        var drawn = 0
                        outer@ for (by in 0 until layers) {
                            val byOff = (by - (layers / 2)).toDouble()
                            for (bx in -range..range) {
                                for (bz in -range..range) {
                                    if (drawn >= desiredCount) break@outer

                                    val bxOff = bx.toDouble()
                                    val bzOff = bz.toDouble()

                                    val minX = playerPos.x + bxOff - 0.45
                                    val minY = playerPos.y + byOff - 0.45
                                    val minZ = playerPos.z + bzOff - 0.45
                                    val aabb = AABB(minX, minY, minZ, minX + 0.9, minY + 0.9, minZ + 0.9)

                                    val cInt = (0xff000000.toInt() or (((bx + range) and 0xff) shl 16) or (((by + 32) and 0xff) shl 8) or ((bz + range) and 0xff))
                                    val c = Color(cInt)

                                    drawStyledBox(aabb = aabb, color = c, style = 2)

                                    drawn++
                                }
                            }
                        }
                    }
                }

            } catch (_: Exception) { }
        }
    }

}