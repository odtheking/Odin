package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.odtheking.mixin.accessors.BeaconBeamAccessor
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.renderPos
import com.odtheking.odin.utils.unaryMinus
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

private const val DEPTH = 0
private const val NO_DEPTH = 1

private val BEAM_TEXTURE = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png")

internal data class LineData(val from: Vec3, val to: Vec3, val color1: Int, val color2: Int, val thickness: Float)
internal data class BoxData(val aabb: AABB, val r: Float, val g: Float, val b: Float, val a: Float, val thickness: Float)
internal data class BeaconData(val pos: BlockPos, val color: Color, val isScoping: Boolean, val gameTime: Long)
internal data class TextData(val text: String, val pos: Vec3, val scale: Float, val depth: Boolean, val cameraRotation: org.joml.Quaternionf, val font: Font, val textWidth: Float)

class RenderConsumer {
    internal val lines = listOf(ObjectArrayList<LineData>(), ObjectArrayList())
    internal val filledBoxes = listOf(ObjectArrayList<BoxData>(), ObjectArrayList())
    internal val wireBoxes = listOf(ObjectArrayList<BoxData>(), ObjectArrayList())

    internal val beaconBeams = ObjectArrayList<BeaconData>()
    internal val texts = ObjectArrayList<TextData>()

    fun clear() {
        lines.forEach { it.clear() }
        filledBoxes.forEach { it.clear() }
        wireBoxes.forEach { it.clear() }
        beaconBeams.clear()
        texts.clear()
    }
}

object RenderBatchManager {
    val renderConsumer = RenderConsumer()

    init {
        on<RenderEvent.Last> {
            val matrix = context.matrices() ?: return@on
            val bufferSource = context.consumers() as? MultiBufferSource.BufferSource ?: return@on
            val camera = mc.gameRenderer.mainCamera.position()

            matrix.pushPose()
            matrix.translate(-camera.x, -camera.y, -camera.z)

            matrix.renderBatchedLinesAndWireBoxes(renderConsumer.lines, renderConsumer.wireBoxes, bufferSource)
            matrix.renderBatchedFilledBoxes(renderConsumer.filledBoxes, bufferSource)
            matrix.popPose()

            matrix.renderBatchedBeaconBeams(renderConsumer.beaconBeams, camera)
            matrix.renderBatchedTexts(renderConsumer.texts, bufferSource, camera)
            renderConsumer.clear()
        }
    }
}

private fun PoseStack.renderBatchedLinesAndWireBoxes(
    lines: List<List<LineData>>,
    wireBoxes: List<List<BoxData>>,
    bufferSource: MultiBufferSource.BufferSource
) {
    val lineRenderLayers = listOf(CustomRenderLayer.LINE_LIST, CustomRenderLayer.LINE_LIST_ESP)
    val last = this.last()
    for (depthState in 0..1) {
        if (lines[depthState].isEmpty() && wireBoxes[depthState].isEmpty()) continue
        val buffer = bufferSource.getBuffer(lineRenderLayers[depthState])

        for (line in lines[depthState]) {
            val dirX = line.to.x - line.from.x
            val dirY = line.to.y - line.from.y
            val dirZ = line.to.z - line.from.z

            PrimitiveRenderer.renderVector(
                last, buffer,
                Vector3f(line.from.x.toFloat(), line.from.y.toFloat(), line.from.z.toFloat()),
                Vec3(dirX, dirY, dirZ),
                line.color1, line.color2, line.thickness
            )
        }

        for (box in wireBoxes[depthState]) {
            PrimitiveRenderer.renderLineBox(
                last, buffer, box.aabb,
                box.r, box.g, box.b, box.a, box.thickness
            )
        }
    }
}

private fun PoseStack.renderBatchedFilledBoxes(consumer: List<List<BoxData>>, bufferSource: MultiBufferSource.BufferSource) {
    val filledBoxRenderLayers = listOf(CustomRenderLayer.TRIANGLE_STRIP, CustomRenderLayer.TRIANGLE_STRIP_ESP)
    val last = this.last()
    for ((depthState, boxes) in consumer.withIndex()) {
        if (boxes.isEmpty()) continue
        val buffer = bufferSource.getBuffer(filledBoxRenderLayers[depthState])

        for (box in boxes) {
            PrimitiveRenderer.addChainedFilledBoxVertices(
                last, buffer,
                box.aabb.minX.toFloat(), box.aabb.minY.toFloat(), box.aabb.minZ.toFloat(),
                box.aabb.maxX.toFloat(), box.aabb.maxY.toFloat(), box.aabb.maxZ.toFloat(),
                box.r, box.g, box.b, box.a
            )
        }
    }
}

private fun PoseStack.renderBatchedBeaconBeams(consumer: List<BeaconData>, camera: Vec3) {
    for (beacon in consumer) {
        pushPose()
        translate(beacon.pos.x - camera.x, beacon.pos.y - camera.y, beacon.pos.z - camera.z)

        val centerX = beacon.pos.x + 0.5
        val centerZ = beacon.pos.z + 0.5
        val dx = camera.x - centerX
        val dz = camera.z - centerZ
        val length = sqrt(dx * dx + dz * dz).toFloat()

        val scale = if (beacon.isScoping) 1.0f else maxOf(1.0f, length * 0.010416667f)

        BeaconBeamAccessor.invokeRenderBeam(
            this,
            mc.gameRenderer.featureRenderDispatcher.submitNodeStorage,
            BEAM_TEXTURE,
            1f,
            beacon.gameTime.toFloat(),
            0,
            319,
            beacon.color.rgba,
            0.2f * scale,
            0.25f * scale
        )
        popPose()
    }
}

private fun PoseStack.renderBatchedTexts(consumer: List<TextData>, bufferSource: MultiBufferSource.BufferSource, camera: Vec3) {
    val cameraPos = -camera

    for (textData in consumer) {
        pushPose()
        val pose = last().pose()
        val scaleFactor = textData.scale * 0.025f

        pose.translate(textData.pos.toVector3f())
            .translate(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
            .rotate(textData.cameraRotation)
            .scale(scaleFactor, -scaleFactor, scaleFactor)

        textData.font.drawInBatch(
            textData.text, -textData.textWidth / 2f, 0f, -1, true, pose, bufferSource,
            if (textData.depth) Font.DisplayMode.NORMAL else Font.DisplayMode.SEE_THROUGH,
            0, LightTexture.FULL_BRIGHT
        )

        popPose()
    }
}

fun RenderEvent.Extract.drawTracer(to: Vec3, color: Color, depth: Boolean, thickness: Float = 3f) {
    val from = mc.player?.let { player ->
        player.renderPos.add(player.forward.add(0.0, player.eyeHeight.toDouble(), 0.0))
    } ?: return
    drawLine(listOf(from, to), color, depth, thickness)
}

fun RenderEvent.Extract.drawLine(points: Collection<Vec3>, color: Color, depth: Boolean, thickness: Float = 3f) {
    drawLine(points, color, color, depth, thickness)
}

fun RenderEvent.Extract.drawLine(points: Collection<Vec3>, color1: Color, color2: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return

    val rgba1 = color1.rgba
    val rgba2 = color2.rgba
    val batch = consumer.lines[if (depth) DEPTH else NO_DEPTH]

    val iterator = points.iterator()
    var current = iterator.next()

    while (iterator.hasNext()) {
        val next = iterator.next()
        batch.add(LineData(current, next, rgba1, rgba2, thickness))
        current = next
    }
}

fun RenderEvent.Extract.drawWireFrameBox(aabb: AABB, color: Color, thickness: Float = 3f, depth: Boolean = false) {
    consumer.wireBoxes[if (depth) DEPTH else NO_DEPTH].add(
        BoxData(aabb, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat, thickness)
    )
}

fun RenderEvent.Extract.drawFilledBox(aabb: AABB, color: Color, depth: Boolean = false) {
    consumer.filledBoxes[if (depth) DEPTH else NO_DEPTH].add(
        BoxData(aabb, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat, 3f)
    )
}

fun RenderEvent.Extract.drawStyledBox(
    aabb: AABB,
    color: Color,
    style: Int = 0,
    depth: Boolean = true
) {
    when (style) {
        0 -> drawFilledBox(aabb, color, depth = depth)
        1 -> drawWireFrameBox(aabb, color, depth = depth)
        2 -> {
            drawFilledBox(aabb, color.multiplyAlpha(0.5f), depth = depth)
            drawWireFrameBox(aabb, color, depth = depth)
        }
    }
}

fun RenderEvent.Extract.drawBeaconBeam(position: BlockPos, color: Color) {
    val isScoping = mc.player?.isScoping == true
    val gameTime = mc.level?.gameTime ?: 0L

    consumer.beaconBeams.add(BeaconData(position, color, isScoping, gameTime))
}

fun RenderEvent.Extract.drawText(text: String, pos: Vec3, scale: Float, depth: Boolean) {
    val cameraRotation = mc.gameRenderer.mainCamera.rotation()
    val font = mc.font ?: return
    val textWidth = font.width(text).toFloat()

    consumer.texts.add(TextData(text, pos, scale, depth, cameraRotation, font, textWidth))
}

fun RenderEvent.Extract.drawCustomBeacon(
    title: String,
    position: BlockPos,
    color: Color,
    increase: Boolean = true,
    distance: Boolean = true
) {
    val dist = mc.player?.blockPosition()?.distManhattan(position) ?: return

    drawWireFrameBox(AABB(position), color, depth = false)
    drawBeaconBeam(position, color)
    drawText(
        (if (distance) ("$title §r§f(§3${dist}m§f)") else title),
        position.center.addVec(y = 1.7),
        if (increase) max(1f, dist * 0.05f) else 2f,
        false
    )
}

fun RenderEvent.Extract.drawCylinder(
    center: Vec3,
    radius: Float,
    height: Float,
    color: Color,
    segments: Int = 32,
    thickness: Float = 5f,
    depth: Boolean = false
) {
    val batch = consumer.lines[if (depth) DEPTH else NO_DEPTH]
    val angleStep = 2.0 * Math.PI / segments
    val rgba = color.rgba

    for (i in 0 until segments) {
        val angle1 = i * angleStep
        val angle2 = (i + 1) * angleStep

        val x1 = (radius * cos(angle1)).toFloat()
        val z1 = (radius * sin(angle1)).toFloat()
        val x2 = (radius * cos(angle2)).toFloat()
        val z2 = (radius * sin(angle2)).toFloat()

        val p1Top = center.add(x1.toDouble(), height.toDouble(), z1.toDouble())
        val p2Top = center.add(x2.toDouble(), height.toDouble(), z2.toDouble())
        val p1Bottom = center.add(x1.toDouble(), 0.0, z1.toDouble())
        val p2Bottom = center.add(x2.toDouble(), 0.0, z2.toDouble())

        batch.add(LineData(p1Top, p2Top, rgba, rgba, thickness))
        batch.add(LineData(p1Bottom, p2Bottom, rgba, rgba, thickness))
        batch.add(LineData(p1Bottom, p1Top, rgba, rgba, thickness))
    }
}

fun RenderEvent.Extract.drawBoxes(waypoints: Collection<DungeonWaypoints.DungeonWaypoint>, disableDepth: Boolean) {
    if (waypoints.isEmpty()) return

    for (waypoint in waypoints) {
        val color = waypoint.color
        if (waypoint.isClicked || color.isTransparent) continue

        val aabb = waypoint.aabb.move(waypoint.blockPos)
        val depth = waypoint.depth && !disableDepth

        if (waypoint.filled) drawFilledBox(aabb, color, depth = depth)
        else drawWireFrameBox(aabb, color, depth = depth)
    }
}

object PrimitiveRenderer {

    private val edges = intArrayOf(
        0, 1,  1, 5,  5, 4,  4, 0,
        3, 2,  2, 6,  6, 7,  7, 3,
        0, 3,  1, 2,  5, 6,  4, 7
    )

    fun renderLineBox(
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
        aabb: AABB,
        r: Float, g: Float, b: Float, a: Float,
        thickness: Float
    ) {
        val x0 = aabb.minX.toFloat()
        val y0 = aabb.minY.toFloat()
        val z0 = aabb.minZ.toFloat()
        val x1 = aabb.maxX.toFloat()
        val y1 = aabb.maxY.toFloat()
        val z1 = aabb.maxZ.toFloat()

        val corners = floatArrayOf(
            x0, y0, z0,
            x1, y0, z0,
            x1, y1, z0,
            x0, y1, z0,
            x0, y0, z1,
            x1, y0, z1,
            x1, y1, z1,
            x0, y1, z1
        )

        for (i in edges.indices step 2) {
            val i0 = edges[i] * 3
            val i1 = edges[i + 1] * 3

            val x0 = corners[i0]
            val y0 = corners[i0 + 1]
            val z0 = corners[i0 + 2]
            val x1 = corners[i1]
            val y1 = corners[i1 + 1]
            val z1 = corners[i1 + 2]

            val dx = x1 - x0
            val dy = y1 - y0
            val dz = z1 - z0

            buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, dx, dy, dz).setLineWidth(thickness)
            buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, dx, dy, dz).setLineWidth(thickness)
        }
    }

    fun addChainedFilledBoxVertices(
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
        minX: Float, minY: Float, minZ: Float,
        maxX: Float, maxY: Float, maxZ: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        val matrix = pose.pose()

        fun vertex(x: Float, y: Float, z: Float) {
            buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a)
        }

        vertex(minX, minY, minZ)
        vertex(minX, minY, maxZ)
        vertex(minX, maxY, maxZ)
        vertex(minX, maxY, minZ)

        vertex(maxX, minY, maxZ)
        vertex(maxX, minY, minZ)
        vertex(maxX, maxY, minZ)
        vertex(maxX, maxY, maxZ)

        vertex(minX, minY, minZ)
        vertex(minX, maxY, minZ)
        vertex(maxX, maxY, minZ)
        vertex(maxX, minY, minZ)

        vertex(maxX, minY, maxZ)
        vertex(maxX, maxY, maxZ)
        vertex(minX, maxY, maxZ)
        vertex(minX, minY, maxZ)

        vertex(minX, minY, minZ)
        vertex(maxX, minY, minZ)
        vertex(maxX, minY, maxZ)
        vertex(minX, minY, maxZ)

        vertex(minX, maxY, maxZ)
        vertex(maxX, maxY, maxZ)
        vertex(maxX, maxY, minZ)
        vertex(minX, maxY, minZ)
    }

    fun renderVector(
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
        start: Vector3f,
        direction: Vec3,
        startColor: Int,
        endColor: Int,
        thickness: Float
    ) {
        val endX = start.x() + direction.x.toFloat()
        val endY = start.y() + direction.y.toFloat()
        val endZ = start.z() + direction.z.toFloat()

        val nx = direction.x.toFloat()
        val ny = direction.y.toFloat()
        val nz = direction.z.toFloat()

        buffer.addVertex(pose, start.x(), start.y(), start.z())
            .setColor(startColor)
            .setNormal(pose, nx, ny, nz)
            .setLineWidth(thickness)

        buffer.addVertex(pose, endX, endY, endZ)
            .setColor(endColor)
            .setNormal(pose, nx, ny, nz)
            .setLineWidth(thickness)
    }
}