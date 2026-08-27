package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.odtheking.mixin.accessors.BeaconBeamAccessor
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.center
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.feature.TextFeatureRenderer
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.resources.Identifier
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.unaryMinus
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

private val BEAM_TEXTURE = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png")

private fun Int.isFullyOpaque(): Boolean = ((this ushr 24) and 0xFF) == 0xFF

private fun resolveLineRenderType(depth: Boolean, fullyOpaque: Boolean) = when {
    depth && fullyOpaque -> RenderTypes.LINES
    depth -> RenderTypes.LINES_TRANSLUCENT
    fullyOpaque -> CustomRenderType.LINES_ESP
    else -> CustomRenderType.LINES_TRANSLUCENT_ESP
}

private fun resolveQuadRenderType(depth: Boolean, fullyOpaque: Boolean) = when {
    depth && fullyOpaque -> CustomRenderType.QUADS_OPAQUE
    depth -> CustomRenderType.QUADS_TRANSLUCENT
    fullyOpaque -> CustomRenderType.QUADS_ESP
    else -> CustomRenderType.QUADS_TRANSLUCENT_ESP
}

private fun RenderEvent.Extract.cameraRelativePose(offset: Vec3 = Vec3.ZERO): PoseStack {
    val camera = mc.gameRenderer.mainCamera().position()
    val poseStack = context.poseStack()
    poseStack.pushPose()
    poseStack.translate(offset.x - camera.x, offset.y - camera.y, offset.z - camera.z)
    return poseStack
}

fun RenderEvent.Extract.drawTexturedQuad(
    texture: Identifier,
    pos: Vec3,
    width: Float,
    height: Float,
    yaw: Float = 0f,
    color: Color = Color(255, 255, 255)
) {
    val yawRad = Math.toRadians(yaw.toDouble())
    val rx = cos(yawRad).toFloat()
    val rz = sin(yawRad).toFloat()
    val hw = width * 0.5
    val hh = height * 0.5

    val poseStack = cameraRelativePose()
    context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.entityCutout(texture)) { pose, buffer ->
        fun vertex(p: Vec3, u: Float, v: Float) {
            buffer.addVertex(pose, p.x.toFloat(), p.y.toFloat(), p.z.toFloat())
                .setColor(color.rgba)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setUv2(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, -rz, 0f, rx)
        }

        vertex(Vec3(pos.x - rx * hw, pos.y - hh, pos.z - rz * hw), 0f, 1f)
        vertex(Vec3(pos.x - rx * hw, pos.y + hh, pos.z - rz * hw), 0f, 0f)
        vertex(Vec3(pos.x + rx * hw, pos.y + hh, pos.z + rz * hw), 1f, 0f)
        vertex(Vec3(pos.x + rx * hw, pos.y - hh, pos.z + rz * hw), 1f, 1f)
    }
    poseStack.popPose()
}

fun RenderEvent.Extract.drawTracer(to: Vec3, color: Color, depth: Boolean, thickness: Float = 3f) {
    val cam = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState
    drawLine(listOf(cam.pos.add(Vec3.directionFromRotation(cam.xRot, cam.yRot)), to), color, depth, thickness)
}

fun RenderEvent.Extract.drawLine(points: Collection<Vec3>, color: Color, depth: Boolean, thickness: Float = 3f) {
    drawLine(points, color, color, depth, thickness)
}

fun RenderEvent.Extract.drawLine(points: Collection<Vec3>, color1: Color, color2: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return

    val rgba1 = color1.rgba
    val rgba2 = color2.rgba
    val renderType = resolveLineRenderType(depth, rgba1.isFullyOpaque() && rgba2.isFullyOpaque())
    val segments = points.zipWithNext()

    val poseStack = cameraRelativePose()
    context.submitNodeCollector().submitCustomGeometry(poseStack, renderType) { pose, buffer ->
        for ((from, to) in segments) {
            PrimitiveRenderer.renderVector(
                pose, buffer,
                Vector3f(from.x.toFloat(), from.y.toFloat(), from.z.toFloat()),
                to.subtract(from), rgba1, rgba2, thickness
            )
        }
    }
    poseStack.popPose()
}

fun RenderEvent.Extract.drawWireFrameBox(aabb: AABB, color: Color, thickness: Float = 3f, depth: Boolean = false) {
    val renderType = resolveLineRenderType(depth, color.alphaFloat >= 0.999f)

    val poseStack = cameraRelativePose()
    context.submitNodeCollector().submitCustomGeometry(poseStack, renderType) { pose, buffer ->
        PrimitiveRenderer.renderLineBox(pose, buffer, aabb, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat, thickness)
    }
    poseStack.popPose()
}

fun RenderEvent.Extract.drawFilledBox(aabb: AABB, color: Color, depth: Boolean = false) {
    val renderType = resolveQuadRenderType(depth, color.alphaFloat >= 0.999f)

    val poseStack = cameraRelativePose()
    context.submitNodeCollector().submitCustomGeometry(poseStack, renderType) { pose, buffer ->
        PrimitiveRenderer.addChainedFilledBoxVertices(
            pose, buffer,
            aabb.minX.toFloat(), aabb.minY.toFloat(), aabb.minZ.toFloat(),
            aabb.maxX.toFloat(), aabb.maxY.toFloat(), aabb.maxZ.toFloat(),
            color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat
        )
    }
    poseStack.popPose()
}

enum class BoxStyle { FILLED, OUTLINE, FILLED_OUTLINE }

fun RenderEvent.Extract.drawStyledBox(
    aabb: AABB,
    color: Color,
    style: BoxStyle = BoxStyle.FILLED,
    depth: Boolean = true
) {
    when (style) {
        BoxStyle.FILLED -> drawFilledBox(aabb, color, depth = depth)
        BoxStyle.OUTLINE -> drawWireFrameBox(aabb, color, depth = depth)
        BoxStyle.FILLED_OUTLINE -> {
            drawFilledBox(aabb.inflate(0.00005), color.multiplyAlpha(0.5f), depth = depth)
            drawWireFrameBox(aabb, color, depth = depth)
        }
    }
}

fun RenderEvent.Extract.drawBeaconBeam(position: BlockPos, color: Color) {
    val isScoping = mc.player?.isScoping == true
    val gameTime = mc.level?.gameTime ?: 0L
    val camera = mc.gameRenderer.mainCamera().position()

    val centerX = position.x + 0.5
    val centerZ = position.z + 0.5
    val dx = camera.x - centerX
    val dz = camera.z - centerZ
    val length = sqrt(dx * dx + dz * dz).toFloat()
    val scale = if (isScoping) 1.0f else maxOf(1.0f, length * 0.010416667f)

    val poseStack = cameraRelativePose(Vec3(position.x.toDouble(), position.y.toDouble(), position.z.toDouble()))
    BeaconBeamAccessor.invokeRenderBeam(
        poseStack, context.submitNodeCollector(), BEAM_TEXTURE, 1f,
        gameTime.toFloat(), 0, 319, color.rgba, 0.2f * scale, 0.25f * scale
    )
    poseStack.popPose()
}

fun RenderEvent.Extract.drawText(text: String, pos: Vec3, scale: Float, depth: Boolean) {
    val scaleFactor = scale * 0.025f
    val displayMode = if (depth) Font.DisplayMode.POLYGON_OFFSET else Font.DisplayMode.SEE_THROUGH
    val string = Language.getInstance().getVisualOrder(FormattedText.of(text))
    val x = -mc.font.width(text).toFloat() / 2f

    val poseStack = context.poseStack()
    poseStack.pushPose()
    poseStack.last().pose()
        .translate(pos.toVector3f())
        .translate(-mc.gameRenderer.mainCamera().position().toVector3f())
        .rotate(mc.gameRenderer.mainCamera().rotation())
        .scale(scaleFactor, -scaleFactor, scaleFactor)

    if (displayMode == Font.DisplayMode.SEE_THROUGH) {
        context.submitNodeCollector().submitCustom(SubmitRenderPhases.AFTER_TERRAIN,
            TextFeatureRenderer.Submit(
                Matrix4f(poseStack.last().pose()), x, 0f, string,
                true, displayMode, LightCoordsUtil.FULL_BRIGHT, -1, 0, 0
            )
        )
    } else context.submitNodeCollector().submitText(poseStack, x, 0f, string, true, displayMode, LightCoordsUtil.FULL_BRIGHT, -1, 0, 0)
    poseStack.popPose()
}

fun RenderEvent.Extract.drawCustomBeacon(title: String, position: BlockPos, color: Color, increase: Boolean = true, distance: Boolean = true) {
    val dist = mc.player?.blockPosition()?.distManhattan(position) ?: return

    drawWireFrameBox(AABB(position), color, depth = false)
    drawBeaconBeam(position, color)
    drawText(
        (if (distance) ("$title §f(§3${dist}m§f)") else title),
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
    val rgba = color.rgba
    val renderType = resolveLineRenderType(depth, rgba.isFullyOpaque())
    val angleStep = 2.0 * Math.PI / segments

    val poseStack = cameraRelativePose()
    context.submitNodeCollector().submitCustomGeometry(poseStack, renderType) { pose, buffer ->
        fun segment(from: Vec3, to: Vec3) {
            PrimitiveRenderer.renderVector(
                pose, buffer,
                Vector3f(from.x.toFloat(), from.y.toFloat(), from.z.toFloat()),
                to.subtract(from), rgba, rgba, thickness
            )
        }

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

            segment(p1Top, p2Top)
            segment(p1Bottom, p2Bottom)
            segment(p1Bottom, p1Top)
        }
    }
    poseStack.popPose()
}

fun RenderEvent.Extract.drawBoxes(waypoints: Collection<DungeonWaypoints.DungeonWaypoint>, disableDepth: Boolean) {
    if (waypoints.isEmpty()) return

    for ((blockPos, color, filled, depth1, aabb1, _, _, isClicked) in waypoints) {
        if (isClicked || color.isTransparent) continue

        val aabb = aabb1.move(blockPos)
        val depth = depth1 && !disableDepth

        if (filled) drawFilledBox(aabb, color, depth = depth)
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