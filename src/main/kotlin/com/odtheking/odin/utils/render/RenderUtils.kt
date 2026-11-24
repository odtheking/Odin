package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.center
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BeaconRenderer
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.resources.Identifier
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.unaryMinus
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private fun Int.isFullyOpaque(): Boolean = (this ushr 24) == 0xFF

private fun lineRenderType(depth: Boolean, fullyOpaque: Boolean) = when {
    depth && fullyOpaque -> RenderTypes.LINES
    depth -> CustomRenderType.LINES_TRANSLUCENT_DEPTH
    fullyOpaque -> CustomRenderType.LINES_ESP
    else -> CustomRenderType.LINES_TRANSLUCENT_ESP
}

private fun quadRenderType(depth: Boolean, fullyOpaque: Boolean) = when {
    depth && fullyOpaque -> CustomRenderType.QUADS_OPAQUE
    depth -> CustomRenderType.QUADS_TRANSLUCENT
    fullyOpaque -> CustomRenderType.QUADS_ESP
    else -> CustomRenderType.QUADS_TRANSLUCENT_ESP
}

private fun cameraPosition(): Vec3 = mc.gameRenderer.mainCamera().position()

private fun RenderExtractEvent.submitGeometry(renderType: RenderType, depth: Boolean, renderer: SubmitNodeCollector.CustomGeometryRenderer) {
    val poseStack = context.poseStack()
    val collector = context.submitNodeCollector()
    if (depth) return collector.submitCustomGeometry(poseStack, renderType, renderer)
    collector.submitCustom(SubmitRenderPhases.SEE_THROUGH_NAME_TAGS, NoDepthSubmit(poseStack.last().copy(), renderType, renderer))
}

private fun RenderExtractEvent.cameraRelativePose(offset: Vec3 = Vec3.ZERO, cameraPos: Vec3): PoseStack {
    val poseStack = context.poseStack()
    poseStack.pushPose()
    poseStack.translate(offset.x - cameraPos.x, offset.y - cameraPos.y, offset.z - cameraPos.z)
    return poseStack
}

fun RenderExtractEvent.drawTexturedQuad(
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

    val poseStack = cameraRelativePose(cameraPos = cameraPosition())
    context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.entityCutoutZOffset(texture)) { pose, buffer ->
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

fun RenderExtractEvent.drawTracer(to: Vec3, color: Color, depth: Boolean, thickness: Float = 3f) {
    val cam = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState
    drawLine(listOf(cam.pos.add(Vec3.directionFromRotation(cam.xRot, cam.yRot)), to), color, depth, thickness)
}

fun RenderExtractEvent.drawLine(points: Collection<Vec3>, color: Color, depth: Boolean, thickness: Float = 3f) {
    drawLine(points, color, color, depth, thickness)
}

fun RenderExtractEvent.drawLine(points: Collection<Vec3>, color1: Color, color2: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return

    val rgba1 = color1.rgba
    val rgba2 = color2.rgba
    val camera = cameraPosition()
    val coords = FloatArray(points.size * 3)
    var i = 0
    for (point in points) {
        coords[i++] = (point.x - camera.x).toFloat()
        coords[i++] = (point.y - camera.y).toFloat()
        coords[i++] = (point.z - camera.z).toFloat()
    }

    submitGeometry(lineRenderType(depth, rgba1.isFullyOpaque() && rgba2.isFullyOpaque()), depth) { pose, buffer ->
        for (o in 0 until coords.size - 3 step 3) {
            line(pose, buffer, coords[o], coords[o + 1], coords[o + 2], coords[o + 3], coords[o + 4], coords[o + 5], rgba1, rgba2, thickness)
        }
    }
}

fun RenderExtractEvent.drawWireFrameBox(aabb: AABB, color: Color, thickness: Float = 3f, depth: Boolean = false) {
    wireBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color.rgba, thickness, depth)
}

fun RenderExtractEvent.drawFilledBox(aabb: AABB, color: Color, depth: Boolean = false) {
    filledBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color.rgba, depth)
}

fun RenderExtractEvent.drawFilledOutlineBox(aabb: AABB, color: Color, thickness: Float = 3f, depth: Boolean = true) {
    val rgba = color.rgba
    val fillRgba = (rgba and 0xFFFFFF) or ((rgba ushr 25) shl 24)
    val camera = cameraPosition()
    val x0 = (aabb.minX - camera.x).toFloat()
    val y0 = (aabb.minY - camera.y).toFloat()
    val z0 = (aabb.minZ - camera.z).toFloat()
    val x1 = (aabb.maxX - camera.x).toFloat()
    val y1 = (aabb.maxY - camera.y).toFloat()
    val z1 = (aabb.maxZ - camera.z).toFloat()
    val e = FILL_INFLATE

    submitGeometry(quadRenderType(depth, fillRgba.isFullyOpaque()), depth) { pose, buffer ->
        quadBox(pose, buffer, x0 - e, y0 - e, z0 - e, x1 + e, y1 + e, z1 + e, fillRgba)
    }
    submitGeometry(lineRenderType(depth, rgba.isFullyOpaque()), depth) { pose, buffer ->
        lineBox(pose, buffer, x0, y0, z0, x1, y1, z1, rgba, thickness)
    }
}

private const val FILL_INFLATE = 0.00005f

private fun RenderExtractEvent.wireBox(
    minX: Double, minY: Double, minZ: Double,
    maxX: Double, maxY: Double, maxZ: Double,
    rgba: Int, thickness: Float, depth: Boolean
) {
    val camera = cameraPosition()
    val x0 = (minX - camera.x).toFloat()
    val y0 = (minY - camera.y).toFloat()
    val z0 = (minZ - camera.z).toFloat()
    val x1 = (maxX - camera.x).toFloat()
    val y1 = (maxY - camera.y).toFloat()
    val z1 = (maxZ - camera.z).toFloat()

    submitGeometry(lineRenderType(depth, rgba.isFullyOpaque()), depth) { pose, buffer ->
        lineBox(pose, buffer, x0, y0, z0, x1, y1, z1, rgba, thickness)
    }
}

private fun RenderExtractEvent.filledBox(
    minX: Double, minY: Double, minZ: Double,
    maxX: Double, maxY: Double, maxZ: Double,
    rgba: Int, depth: Boolean
) {
    val camera = cameraPosition()
    val x0 = (minX - camera.x).toFloat()
    val y0 = (minY - camera.y).toFloat()
    val z0 = (minZ - camera.z).toFloat()
    val x1 = (maxX - camera.x).toFloat()
    val y1 = (maxY - camera.y).toFloat()
    val z1 = (maxZ - camera.z).toFloat()

    submitGeometry(quadRenderType(depth, rgba.isFullyOpaque()), depth) { pose, buffer ->
        quadBox(pose, buffer, x0, y0, z0, x1, y1, z1, rgba)
    }
}

enum class BoxStyle { FILLED, OUTLINE, FILLED_OUTLINE }

fun RenderExtractEvent.drawStyledBox(
    aabb: AABB,
    color: Color,
    style: BoxStyle = BoxStyle.FILLED,
    depth: Boolean = true
) {
    when (style) {
        BoxStyle.FILLED -> drawFilledBox(aabb, color, depth = depth)
        BoxStyle.OUTLINE -> drawWireFrameBox(aabb, color, depth = depth)
        BoxStyle.FILLED_OUTLINE -> drawFilledOutlineBox(aabb, color, depth = depth)
    }
}

fun RenderExtractEvent.drawBeaconBeam(position: BlockPos, color: Color) {
    val camera = mc.gameRenderer.mainCamera().position()
    val animationTime = (mc.level?.gameTime ?: 0L).mod(40L) +  mc.deltaTracker.getGameTimeDeltaPartialTick(false)
    val scale = if (mc.player?.isScoping == true) 1f else max(1f, camera.subtract(Vec3.atCenterOf(position)).horizontalDistance().toFloat() / 96f)

    val poseStack = cameraRelativePose(Vec3.atLowerCornerOf(position), camera)
    BeaconRenderer.submitBeaconBeam(
        poseStack, context.submitNodeCollector(), BeaconRenderer.BEAM_LOCATION, 1f,
        animationTime, 0, BeaconRenderer.MAX_RENDER_Y, color.rgba,
        BeaconRenderer.SOLID_BEAM_RADIUS * scale, BeaconRenderer.BEAM_GLOW_RADIUS * scale
    )
    poseStack.popPose()
}

fun RenderExtractEvent.drawText(text: String, pos: Vec3, scale: Float, depth: Boolean) {
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

    context.submitNodeCollector().submitText(poseStack, x, 0f, string, true, displayMode, LightCoordsUtil.FULL_BRIGHT, -1, 0, 0)
    poseStack.popPose()
}

fun RenderExtractEvent.drawCustomBeacon(title: String, position: BlockPos, color: Color, increase: Boolean = true, distance: Boolean = true) {
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

fun RenderExtractEvent.drawCylinder(
    center: Vec3,
    radius: Float,
    height: Float,
    color: Color,
    segments: Int = 32,
    thickness: Float = 5f,
    depth: Boolean = false
) {
    val rgba = color.rgba
    val camera = cameraPosition()
    val cx = (center.x - camera.x).toFloat()
    val cz = (center.z - camera.z).toFloat()
    val bottom = (center.y - camera.y).toFloat()
    val top = bottom + height
    val angleStep = 2.0 * Math.PI / segments

    submitGeometry(lineRenderType(depth, rgba.isFullyOpaque()), depth) { pose, buffer ->
        var x1 = cx + radius
        var z1 = cz
        for (i in 1..segments) {
            val angle = i * angleStep
            val x2 = cx + (radius * cos(angle)).toFloat()
            val z2 = cz + (radius * sin(angle)).toFloat()

            line(pose, buffer, x1, top, z1, x2, top, z2, rgba, rgba, thickness)
            line(pose, buffer, x1, bottom, z1, x2, bottom, z2, rgba, rgba, thickness)
            line(pose, buffer, x1, bottom, z1, x1, top, z1, rgba, rgba, thickness)

            x1 = x2
            z1 = z2
        }
    }
}

fun RenderExtractEvent.drawBoxes(waypoints: Collection<DungeonWaypoints.DungeonWaypoint>, disableDepth: Boolean) {
    if (waypoints.isEmpty()) return

    for ((blockPos, color, filled, depth1, aabb, _, _, isClicked) in waypoints) {
        if (isClicked || color.isTransparent) continue

        val depth = depth1 && !disableDepth
        val x = blockPos.x.toDouble()
        val y = blockPos.y.toDouble()
        val z = blockPos.z.toDouble()

        if (filled) filledBox(aabb.minX + x, aabb.minY + y, aabb.minZ + z, aabb.maxX + x, aabb.maxY + y, aabb.maxZ + z, color.rgba, depth)
        else wireBox(aabb.minX + x, aabb.minY + y, aabb.minZ + z, aabb.maxX + x, aabb.maxY + y, aabb.maxZ + z, color.rgba, 3f, depth)
    }
}

private fun lineBox(
    pose: PoseStack.Pose, buffer: VertexConsumer,
    x0: Float, y0: Float, z0: Float,
    x1: Float, y1: Float, z1: Float,
    rgba: Int, width: Float
) {
    // bottom
    line(pose, buffer, x0, y0, z0, x1, y0, z0, rgba, rgba, width)
    line(pose, buffer, x1, y0, z0, x1, y0, z1, rgba, rgba, width)
    line(pose, buffer, x1, y0, z1, x0, y0, z1, rgba, rgba, width)
    line(pose, buffer, x0, y0, z1, x0, y0, z0, rgba, rgba, width)
    // top
    line(pose, buffer, x0, y1, z0, x1, y1, z0, rgba, rgba, width)
    line(pose, buffer, x1, y1, z0, x1, y1, z1, rgba, rgba, width)
    line(pose, buffer, x1, y1, z1, x0, y1, z1, rgba, rgba, width)
    line(pose, buffer, x0, y1, z1, x0, y1, z0, rgba, rgba, width)
    // verticals
    line(pose, buffer, x0, y0, z0, x0, y1, z0, rgba, rgba, width)
    line(pose, buffer, x1, y0, z0, x1, y1, z0, rgba, rgba, width)
    line(pose, buffer, x1, y0, z1, x1, y1, z1, rgba, rgba, width)
    line(pose, buffer, x0, y0, z1, x0, y1, z1, rgba, rgba, width)
}

private fun quadBox(
    pose: PoseStack.Pose, buffer: VertexConsumer,
    x0: Float, y0: Float, z0: Float,
    x1: Float, y1: Float, z1: Float,
    rgba: Int
) {
    val m = pose.pose()

    buffer.addVertex(m, x0, y0, z0).setColor(rgba)
    buffer.addVertex(m, x0, y0, z1).setColor(rgba)
    buffer.addVertex(m, x0, y1, z1).setColor(rgba)
    buffer.addVertex(m, x0, y1, z0).setColor(rgba)

    buffer.addVertex(m, x1, y0, z1).setColor(rgba)
    buffer.addVertex(m, x1, y0, z0).setColor(rgba)
    buffer.addVertex(m, x1, y1, z0).setColor(rgba)
    buffer.addVertex(m, x1, y1, z1).setColor(rgba)

    buffer.addVertex(m, x0, y0, z0).setColor(rgba)
    buffer.addVertex(m, x0, y1, z0).setColor(rgba)
    buffer.addVertex(m, x1, y1, z0).setColor(rgba)
    buffer.addVertex(m, x1, y0, z0).setColor(rgba)

    buffer.addVertex(m, x1, y0, z1).setColor(rgba)
    buffer.addVertex(m, x1, y1, z1).setColor(rgba)
    buffer.addVertex(m, x0, y1, z1).setColor(rgba)
    buffer.addVertex(m, x0, y0, z1).setColor(rgba)

    buffer.addVertex(m, x0, y0, z0).setColor(rgba)
    buffer.addVertex(m, x1, y0, z0).setColor(rgba)
    buffer.addVertex(m, x1, y0, z1).setColor(rgba)
    buffer.addVertex(m, x0, y0, z1).setColor(rgba)

    buffer.addVertex(m, x0, y1, z1).setColor(rgba)
    buffer.addVertex(m, x1, y1, z1).setColor(rgba)
    buffer.addVertex(m, x1, y1, z0).setColor(rgba)
    buffer.addVertex(m, x0, y1, z0).setColor(rgba)
}

private fun line(
    pose: PoseStack.Pose, buffer: VertexConsumer,
    x0: Float, y0: Float, z0: Float,
    x1: Float, y1: Float, z1: Float,
    rgba0: Int, rgba1: Int, width: Float
) {
    val dx = x1 - x0
    val dy = y1 - y0
    val dz = z1 - z0
    buffer.addVertex(pose, x0, y0, z0).setColor(rgba0).setNormal(pose, dx, dy, dz).setLineWidth(width)
    buffer.addVertex(pose, x1, y1, z1).setColor(rgba1).setNormal(pose, dx, dy, dz).setLineWidth(width)
}