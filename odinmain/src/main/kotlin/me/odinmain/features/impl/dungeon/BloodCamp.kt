package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.getSkullValue
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Items
import net.minecraft.network.play.server.S14PacketEntity.S17PacketEntityLookMove
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.roundToInt

object BloodCamp : Module(
    name = "Blood Camp",
    description = "Draws boxes to spawning mobs in the blood room. WARNING: not perfectly accurate. Mobs spawn randomly between 37 - 41 ticks, adjust offset to adjust between this tickrange.",
    category = Category.DUNGEON
) {
    private val bloodhelper: Boolean by BooleanSetting("Blood Camp Assist", default = true, description = "Renders a box where blood mobs will spawn.")
    private val fboxColor: Color by ColorSetting("Spawn Color", Color.RED, true, description = "Color for Spawn render box. Set alpha to 0 to disable.").withDependency { bloodhelper }
    private val mboxColor: Color by ColorSetting("Position Color", Color.GREEN, true, description = "Color for Mob current adjusted position render box. Set alpha to 0 to disable.").withDependency { bloodhelper }
    private val boxSize: Double by NumberSetting("Box Size", default = 1.0, increment = 0.1, min = 0.1, max = 1.0, description = "The size of the boxes. Lower values may be less accurate").withDependency { bloodhelper }
    private val drawLine: Boolean by BooleanSetting("Line", default = true, description = "Line between Final box and Spawn box").withDependency { bloodhelper }
    private val drawTime: Boolean by BooleanSetting("Time Left", default = true, description = "Time before mob spawn. Adjust offset depending on accuracy. (will always be up to 100 ms off)").withDependency { bloodhelper }
    private val advanced: Boolean by DropdownSetting("Advanced", default = false).withDependency { bloodhelper }
    private val offset: Int by NumberSetting("Offset", default = 20, increment = 1, max = 100, min = -100, description = "Spawn offset. This value can be edited to adjust average spawn point.").withDependency { advanced && bloodhelper }
    private val tick: Int by NumberSetting("Tick", default = 40, increment = 1, max = 41, min = 37, description = "Tick to assume spawn. Offset to offset this value to the ms.").withDependency { advanced && bloodhelper}
    private val watcherBar: Boolean by BooleanSetting("Watcher Bar", default = true, description = "Shows the watcher's health.")

    private var currentName: String? = null
    private val firstSpawnRegex = Regex("^\\[BOSS] The Watcher: Let's see how you can handle this.$")

    init {
        execute(100) {
            onTick()
            getWatcherHealth()
        }

        onPacket(S17PacketEntityLookMove::class.java, { bloodhelper && enabled }) {
            onPacketLookMove(it)
        }

        onMessage(firstSpawnRegex) {
            firstSpawns = false
        }

        onWorldLoad {
            entityList.clear()
            firstSpawns = true
            watcher.clear()
            forRender.clear()
            ticktime = 0
            currentName = null
        }
    }

    private fun getWatcherHealth() {
        if (!inDungeons || !BossStatus.bossName.noControlCodes.contains("The Watcher") || !watcherBar) return
        val health = BossStatus.healthScale
        val floor = LocationUtils.currentDungeon?.floor ?: return
        val amount = 12 + floor.floorNumber
        currentName = if (health < 0.05) null else " ${(amount * health).roundToInt()}/$amount"
    }

    @SubscribeEvent
    fun onRenderBossHealth(event: RenderGameOverlayEvent) {
        if (!inDungeons || event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH || currentName == null || !watcherBar) return
        if (BossStatus.bossName.noControlCodes != "The Watcher") return
        BossStatus.bossName += currentName
    }

    private val watcherSkulls = setOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlYzQwMDA4ZTFjMzFjMTk4NGY0ZDY1MGFiYjM0MTBmMjAzNzExOWZkNjI0YWZjOTUzNTYzYjczNTE1YTA3NyJ9fX0K", // M2, M3, F6
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVjMWRjNDdhMDRjZTU3MDAxYThiNzI2ZjAxOGNkZWY0MGI3ZWE5ZDdiZDZkODM1Y2E0OTVhMGVmMTY5Zjg5MyJ9fX0K", // M5
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY2ZTFlN2VkMzY1ODZjMmQ5ODA1NzAwMmJjMWFkYzk4MWUyODg5ZjdiZDdiNWIzODUyYmM1NWNjNzgwMjIwNCJ9fX0K", // M6
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkNjFlODA1NWY2ZWU5N2FiNWI2MTk2YThkN2VjOTgwNzhhYzM3ZTAwMzc2MTU3YjZiNTIwZWFhYTJmOTNhZiJ9fX0K", // F5
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM3ZGQxOGI1OTgzYTc2N2U1NTZkYzY0NDI0YWY0YjlhYmRiNzVkNGM5ZThiMDk3ODE4YWZiYzQzMWJmMGUwOSJ9fX0K", // M1, F4
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY2MmI2ZmI0YjhiNTg2ZGM0Y2RmODAzYjA0NDRkOWI0MWQyNDVjZGY2NjhkYWIzOGZhNmMwNjRhZmU4ZTQ2MSJ9fX0K", // M4, F7
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjczOWQ3ZjRlNjZhN2RiMmVhNmNkNDE0ZTRjNGJhNDFkZjdhOTI0NTVjOWZjNDJjYWFiMDE0NjY1YzM2N2FkNSJ9fX0K", // M7
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdkYjE5MjNkMDNjNGVmNGU5ZjZlODcyYzVhNmFkMjU3OGIxYWZmMmIyODFmYmMzZmZhNzQ2NmM4MjVmYjkifX19", // F1, F2
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjVmMGQ3OGZlMzhkMWQ3Zjc1ZjA4Y2RjZjJhMTg1NWQ2ZGEwMzM3ZTExNGEzYzYzZTNiZjNjNjE4YmM3MzJiMCJ9fX0K" // F3
    )

    private var ticktime: Long = 0

    private val forRender = hashMapOf<EntityArmorStand, RenderEData>()
    data class RenderEData(
        var currVector: Vec3? = null, var lastEndVector: Vec3? = null, var endVector: Vec3? = null,
        val startVector: Vec3, var time: Long? = null, var endVecUpdated: Long? = null, var speedVectors: Vec3? = null
    )

    private val entityList = hashMapOf<EntityArmorStand, EntityData>()
    data class EntityData(
        val vectors: MutableList<Vec3?> = mutableListOf(), var startVector: Vec3? = null, var finalVector: Vec3? = null,
        var time: Int? = null, val started: Long? = null, var timetook: Long? = null, var firstSpawns: Boolean = true
    )

    private var firstSpawns = true
    private var watcher = mutableListOf<Entity>()

    @SubscribeEvent
    fun onPostMetadata(event: PostEntityMetadata) {
        val entity = mc.theWorld.getEntityByID(event.packet.entityId) ?: return
        if (watcher.isNotEmpty() || entity !is EntityZombie || !bloodhelper) return

        val texture = getSkullValue(entity) ?: return
        if (watcherSkulls.contains(texture)) {
            watcher.add(entity)
            modMessage("watcher added")
        }
    }

    fun onTick() {
        if (entityList.isEmpty()) return

        entityList.filter { (entity) -> watcher.any { it.getDistanceToEntity(entity) < 20 }
        }.forEach { (entity, data) ->
            if (data.started != null) data.timetook = ticktime - data.started

            val timeTook = data.timetook ?: return@forEach
            val startVector = data.startVector ?: return@forEach
            val currVector = Vec3(entity.posX, entity.posY, entity.posZ)

            val speedVectors = Vec3(
                (currVector.xCoord - startVector.xCoord) / timeTook,
                (currVector.yCoord - startVector.yCoord) / timeTook,
                (currVector.zCoord - startVector.zCoord) / timeTook
            )

            val time = (if (data.firstSpawns) 2000 else 0) + (tick * 50) - timeTook + offset
            val endpoint = Vec3(
                currVector.xCoord + speedVectors.xCoord * time,
                currVector.yCoord + speedVectors.yCoord * time,
                currVector.zCoord + speedVectors.zCoord * time
            )

            forRender[entity]?.lastEndVector = forRender[entity]?.endVector

            if (entity !in forRender)
                forRender[entity] = RenderEData(startVector = startVector)

            forRender[entity].let {
                it?.currVector = currVector
                it?.endVector = endpoint
                it?.time = time
                it?.endVecUpdated = ticktime
                it?.speedVectors = speedVectors
            }
        }
    }


    private fun onPacketLookMove(packet: S17PacketEntityLookMove) {
        val entity = packet.getEntity(mc.theWorld) ?: return
        if (entity !is EntityArmorStand || !watcher.any { it.getDistanceToEntity(entity) < 20 }) return

        if (entity.getEquipmentInSlot(4)?.item != Items.skull || entity in entityList) return

        entityList[entity] = EntityData(startVector = entity.positionVector, started = ticktime, firstSpawns = firstSpawns)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (watcher.isEmpty() || !bloodhelper) return

        val ping = ServerUtils.averagePing

        forRender.filter { (entity) -> !entity.isDead }.forEach { (entity, data) ->
            val entityData = entityList[entity] ?: return@forEach

            val timeTook = entityData.timetook ?: return@forEach
            val startVector = entityData.startVector ?: return@forEach
            val currVector = entity.positionVector ?: return@forEach
            val endVector = data.endVector ?: return@forEach
            val lastEndVector = data.lastEndVector ?: return@forEach
            val endVectorUpdated = min(ticktime - data.endVecUpdated!!, 100)

            val speedVectors = Vec3(
                (currVector.xCoord - startVector.xCoord) / timeTook,
                (currVector.yCoord - startVector.yCoord) / timeTook,
                (currVector.zCoord - startVector.zCoord) / timeTook
            )

            val endPoint = Vec3(
                lastEndVector.xCoord + (endVector.xCoord - lastEndVector.xCoord) * endVectorUpdated / 100,
                lastEndVector.yCoord + (endVector.yCoord - lastEndVector.yCoord) * endVectorUpdated / 100,
                lastEndVector.zCoord + (endVector.zCoord - lastEndVector.zCoord) * endVectorUpdated / 100
            )

            val pingPoint = Vec3(
                currVector.xCoord + speedVectors.xCoord * ping,
                currVector.yCoord + speedVectors.yCoord * ping,
                currVector.zCoord + speedVectors.zCoord * ping
            )

            if (drawLine) {
                Renderer.draw3DLine(
                    Vec3(currVector.xCoord, currVector.yCoord + 2.0, currVector.zCoord),
                    Vec3(endPoint.xCoord, endPoint.yCoord + 2.0, endPoint.zCoord),
                    Color.RED, 3f, true
                )
            }

            val pingAABB = AxisAlignedBB(boxSize,boxSize,boxSize, 0.0, 0.0, 0.0).offset(pingPoint).offset((boxSize/2).unaryMinus(),1.5,(boxSize/2).unaryMinus())
            val endAABB = AxisAlignedBB(boxSize,boxSize,boxSize, 0.0, 0.0, 0.0).offset(endPoint).offset((boxSize/2).unaryMinus(),1.5,(boxSize/2).unaryMinus())

            val time = data.time ?: return@forEach

            if (ping > time) {
                Renderer.drawBox(pingAABB, mboxColor, fillAlpha = 0f, outlineAlpha = mboxColor.alpha, depth = true)
                Renderer.drawBox(endAABB, fboxColor, fillAlpha = 0f, outlineAlpha = fboxColor.alpha, depth = true)
            } else Renderer.drawBox(endAABB, fboxColor, fillAlpha = 0f, outlineAlpha = fboxColor.alpha, depth = true)

            val timeDisplay = (time.toFloat() - offset) / 1000
            val color = when {
                timeDisplay > 1.5 -> Color.GREEN
                timeDisplay in 0.5..1.5 -> Color.ORANGE
                timeDisplay in 0.0..0.5 -> Color.RED
                else -> Color.BLUE
            }
            if (drawTime) Renderer.drawStringInWorld("${timeDisplay}s", endPoint.addVec(y = 2), color, depth = true, scale = 0.03f)
        }
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        ticktime += 50
    }
}
