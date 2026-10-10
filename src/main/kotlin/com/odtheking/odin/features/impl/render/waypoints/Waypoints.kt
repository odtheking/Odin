package com.odtheking.odin.features.impl.render.waypoints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ListSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.BlockClickEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawCylinder
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.setClipboardContent
import net.minecraft.util.Mth
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.round
import kotlin.math.roundToInt

enum class Trigger { VISUAL, PROXIMITY, CLICK }

fun Double.prettyCoord(): String {
    val rounded = (this * 100).roundToInt() / 100.0
    return if (rounded == round(rounded)) rounded.toLong().toString() else rounded.toString()
}

class Waypoint(
    val area: String = "",
    val blockPos: Vec3 = Vec3.ZERO,
    val endPos: Vec3 = blockPos,
    val label: String = "",
    val color: Color = Colors.minecraftColors.random().copy(),
    val trigger: Trigger = Trigger.VISUAL,
    val command: String? = null,
    val radius: Float? = null,
    var enabled: Boolean = true,
) {
    @delegate:Transient
    val id by lazy {
        val min = Vec3(minOf(blockPos.x, endPos.x), minOf(blockPos.y, endPos.y), minOf(blockPos.z, endPos.z))
        val max = Vec3(maxOf(blockPos.x, endPos.x), maxOf(blockPos.y, endPos.y), maxOf(blockPos.z, endPos.z))
        "$area:${trigger.ordinal}:${min.x.prettyCoord()},${min.y.prettyCoord()},${min.z.prettyCoord()}-${max.x.prettyCoord()},${max.y.prettyCoord()},${max.z.prettyCoord()}"
    }

    @delegate:Transient
    val box by lazy {
        val pad = if (radius != null) 1.0 else 0.0
        val base = AABB(
            minOf(blockPos.x, endPos.x), minOf(blockPos.y, endPos.y), minOf(blockPos.z, endPos.z),
            maxOf(blockPos.x, endPos.x) + pad, maxOf(blockPos.y, endPos.y) + pad, maxOf(blockPos.z, endPos.z) + pad,
        )
        radius?.let { base.inflate(it.toDouble(), 0.0, it.toDouble()) } ?: base
    }

    @delegate:Transient val center by lazy { box.center }
    @delegate:Transient val radiusF by lazy { radius ?: 0f }
    @delegate:Transient private val radiusSq by lazy { Mth.square((radius ?: 0f).toDouble()) }
    @delegate:Transient val cylinderBase by lazy { Vec3(center.x, box.minY, center.z) }
    @delegate:Transient val labelPos by lazy { Vec3(center.x, box.maxY + 0.5, center.z) }

    fun intersects(hitbox: AABB): Boolean {
        if (radius == null) return box.intersects(hitbox)
        if (hitbox.minY < box.minY || hitbox.minY >= box.maxY) return false
        val dx = Mth.clamp(center.x, hitbox.minX, hitbox.maxX) - center.x
        val dz = Mth.clamp(center.z, hitbox.minZ, hitbox.maxZ) - center.z
        return Mth.lengthSquared(dx, dz) <= radiusSq
    }

    fun fire() {
        command?.let { sendCommand(it) }
    }

    @Transient
    var inside = false
}

object Waypoints : Module(
    name = "Waypoints",
    description = "Add waypoints that only show in a specific dungeon phase, Kuudra tier or island and can run commands with various trigger types."
) {
    private val textScale by NumberSetting("Text Scale", 1f, 0.1..4.0, increment = 0.1f, desc = "The scale of the labels of waypoints.")
    private val showThroughWalls by BooleanSetting("Show Through Walls", false, desc = "Disables depth testing so all waypoints are visible through walls.")

    private val openManager by ActionSetting("Open Waypoint Manager", desc = "Opens the waypoint manager.") {
        mc.setScreenAndShow(WaypointScreen(mc.gui.screen()))
    }

    val waypoints: MutableList<Waypoint> by ListSetting("Waypoints", mutableListOf())

    var revision by mutableIntStateOf(0)
        private set

    private var active: List<Waypoint> = emptyList()
    private var proximity: List<Waypoint> = emptyList()
    private var clickable: List<Waypoint> = emptyList()

    private var activeKey: String? = null
    private var dirty = true

    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        on<TickEvent.End> {
            if (waypoints.isEmpty() && active.isEmpty()) return@on
            val key = WaypointAreas.current()?.key
            if (key != activeKey) {
                activeKey = key
                proximity = emptyList()
                dirty = true
            }
            if (dirty) rebuild()
            if (proximity.isEmpty()) return@on

            val hitbox = mc.player?.boundingBox?: return@on
            for (wp in proximity) {
                val inZone = wp.intersects(hitbox)
                if (inZone && !wp.inside) wp.fire()
                wp.inside = inZone
            }
        }

        on<RenderExtractEvent> {
            if (active.isEmpty()) return@on
            val pos = mc.player?.position() ?: return@on
            val depth = !showThroughWalls

            for (wp in active) {
                val box = wp.box
                if (box.distanceToSqr(pos) > 128.0 * 128.0) continue

                if (wp.radius != null) drawCylinder(wp.cylinderBase, wp.radiusF, 0.2f, wp.color, depth = depth)
                else drawWireFrameBox(box, wp.color, depth = depth)
                if (wp.label.isNotEmpty()) drawText(wp.label, wp.labelPos, textScale, depth)
            }
        }

        on<BlockClickEvent> {
            if (clickable.isNotEmpty()) clickable.firstOrNull { it.box.intersects(pos) }?.fire()
        }

        on<LevelEvent.Load> {
            for (wp in waypoints) wp.inside = false
            active = emptyList()
            proximity = emptyList()
            clickable = emptyList()
            activeKey = null
            dirty = true
        }
    }

    private fun rebuild() {
        dirty = false
        active = activeKey?.let { key -> waypoints.filter { it.area == key && it.enabled } }.orEmpty()
        proximity = active.filter { it.trigger == Trigger.PROXIMITY }
        clickable = active.filter { it.trigger == Trigger.CLICK }

        val hitbox = mc.player?.boundingBox ?: return
        for (wp in proximity) if (wp !in proximity) wp.inside = wp.intersects(hitbox)
    }

    private fun changed() {
        dirty = true
        revision++
    }

    fun submit(wp: Waypoint, replacing: Waypoint?): String? {
        if (waypoints.any { it !== replacing && it.id == wp.id }) return "There is already a waypoint here."
        val index = waypoints.indexOfFirst { it === replacing }
        if (index == -1) waypoints.add(wp) else waypoints[index] = wp
        changed()
        return null
    }

    fun toggle(wp: Waypoint) {
        wp.enabled = !wp.enabled
        changed()
    }

    fun remove(wp: Waypoint) {
        waypoints.remove(wp)
        changed()
    }

    fun clear(area: String? = null): Int {
        val removed = waypoints.count { area == null || it.area == area }
        if (removed == 0) return 0
        waypoints.removeAll { area == null || it.area == area }
        changed()
        return removed
    }

    fun exportToClipboard(): String {
        if (waypoints.isEmpty()) return "§cYou don't have any waypoints to export."
        setClipboardContent(gson.toJson(waypoints))
        return "§aExported §f${waypoints.size} §awaypoints to the clipboard."
    }

    fun importFromClipboard(): String {
        val ids = waypoints.mapTo(HashSet()) { it.id }
        var total = 0
        val new = try {
            gson.fromJson<List<Waypoint>>(mc.keyboardHandler.clipboard, object : TypeToken<List<Waypoint>>() {}.type)
                .also { total = it.size }
                .filter { ids.add(it.id) }
        } catch (_: Exception) { return "§cNo waypoints found in your clipboard." }

        if (new.isEmpty()) return "§cNothing was imported, every waypoint already exists."
        waypoints.addAll(new)
        changed()
        return "§aImported §f${new.size} §awaypoints.${if (new.size < total) " §7(${total - new.size} skipped)" else ""}"
    }
}