package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Items
import net.minecraft.network.play.server.S14PacketEntity.S17PacketEntityLookMove
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.common.util.Constants

object BloodCamp : Module(
    name = "Blood Camp",
    description = "Draws boxes to spawning mobs in the blood room. WARNING: not perfectly accurate. Mobs spawn randomly between 38 - 40 ticks, adjust offset to adjust between this tickrange.",
    category = Category.DUNGEON
) {
    private val fboxColor: Color by ColorSetting("Spawn Color", Color.RED, true, description = "Color for Spawn render box. Set alpha to 0 to disable.")
    private val mboxColor: Color by ColorSetting("Position Color", Color.GREEN, true, description = "Color for Mob current adjusted position render box. Set alpha to 0 to disable.")
    private val boxSize: Double by NumberSetting("Box Size", default = 1.0, increment = 0.1, min = 0.1, max = 1.0, description = "The size of the boxes. Lower values may be less accurate")
    private val drawLine: Boolean by BooleanSetting("Line", default = true, description = "Line between Final box and Spawn box")
    private val drawTime: Boolean by BooleanSetting("Time Left", default = true, description = "Time before mob spawn. Adjust offset depending on accuracy. (will always be up to 100 ms off)")
    private val advanced: Boolean by DropdownSetting("Advanced", default = false)
    private val offset: Int by NumberSetting("Offset", default = 35, increment = 1, max = 100, min = -100, description = "Spawn offset. This value can be edited to adjust average spawn point.").withDependency { advanced }
    private val tick: Int by NumberSetting("Tick", default = 39, increment = 1, max = 41, min = 37, description = "Tick to assume spawn. Offset to offset this value to the ms.").withDependency { advanced }

    private val watcherSkulls = setOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlYzQwMDA4ZTFjMzFjMTk4NGY0ZDY1MGFiYjM0MTBmMjAzNzExOWZkNjI0YWZjOTUzNTYzYjczNTE1YTA3NyJ9fX0K",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVjMWRjNDdhMDRjZTU3MDAxYThiNzI2ZjAxOGNkZWY0MGI3ZWE5ZDdiZDZkODM1Y2E0OTVhMGVmMTY5Zjg5MyJ9fX0K",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY2ZTFlN2VkMzY1ODZjMmQ5ODA1NzAwMmJjMWFkYzk4MWUyODg5ZjdiZDdiNWIzODUyYmM1NWNjNzgwMjIwNCJ9fX0K",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkNjFlODA1NWY2ZWU5N2FiNWI2MTk2YThkN2VjOTgwNzhhYzM3ZTAwMzc2MTU3YjZiNTIwZWFhYTJmOTNhZiJ9fX0K"
    )

    private var ticktime: Long = 0

    private val forRender = hashMapOf<EntityArmorStand, RenderEData>()
    data class RenderEData(
        var currVector: Vec3? = null,
        var lastEndVector: Vec3? = null,
        var endVector: Vec3? = null,
        val startVector: Vec3,
        var time: Long? = null,
        var endVecUpdated: Long? = null,
        var speedVectors: Vec3? = null
    )

    private val entityList = hashMapOf<EntityArmorStand, EntityData>()
    data class EntityData(
        val vectors: MutableList<Vec3?> = mutableListOf(),
        var startVector: Vec3? = null,
        var finalVector: Vec3? = null,
        var time: Int? = null,
        val started: Long? = null,
        var timetook: Long? = null
    )

    private val firstspawnregex = Regex("^\\[BOSS] The Watcher: Let's see how you can handle this.$")

    private var firstSpawns = true
    private var watcher = mutableListOf<Entity>()

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (watcher.isEmpty() && event.entity is EntityZombie) {
            runIn(2) {
                (event.entity as EntityZombie).apply {
                    val helmet = getEquipmentInSlot(4)
                    if (helmet == null || helmet.item != Items.skull) return@runIn
                    val nbt = helmet.tagCompound
                    val texture = nbt.getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0).getString("Value")
                    if (texture != null && !watcherSkulls.contains(texture)) {
                        watcher.add(event.entity)
                    }
                }
            }
        }
    }

    fun onTick() {
        if (entityList.isEmpty()) return
        entityList.filter { (entity) -> watcher.any { it.getDistanceToEntity(entity) < 20}  }.forEach { (entity, data) ->
            if (data.started != null) { data.timetook = ticktime - data.started }

            if (data.timetook != null) {
                val startPoint = Vec3(entity.posX, entity.posY, entity.posZ)

                val speedVectors = Vec3(
                    (startPoint.xCoord - (data.startVector?.xCoord ?: return)) / data.timetook!!,
                    (startPoint.yCoord - (data.startVector?.yCoord ?: return)) / data.timetook!!,
                    (startPoint.zCoord - (data.startVector?.zCoord ?: return)) / data.timetook!!
                )
                val time = (if (firstSpawns) 2000 else 0) + (tick*50) - data.timetook!! + offset
                val endpoint = Vec3(
                    startPoint.xCoord + speedVectors.xCoord * time,
                    startPoint.yCoord + speedVectors.yCoord * time,
                    startPoint.zCoord + speedVectors.zCoord * time
                )

                if (forRender[entity]?.endVector != null) {
                    forRender[entity]?.lastEndVector = forRender[entity]?.endVector
                }

                if (entity !in forRender) {
                    forRender[entity] = RenderEData(startVector = data.startVector!!,)
                }

                val it = forRender[entity]
                it?.currVector = startPoint
                it?.endVector = endpoint
                it?.time = time
                it?.endVecUpdated = ticktime
                it?.speedVectors = speedVectors
            }
        }
    }

    private fun onPacketLookMove(packet: S17PacketEntityLookMove) {
        val entity = packet.getEntity(mc.theWorld ?: return) ?: return
        if (entity !is EntityArmorStand) return
        if (watcher.any { it.getDistanceToEntity(entity) < 20}) {

            if (entity.getEquipmentInSlot(4)?.item != Items.skull || entity in entityList) return

            entityList[entity] = EntityData(
                startVector = entity.positionVector,
                started = ticktime
            )
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (watcher.isEmpty()) return
        forRender.filter { (entity) -> !entity.isDead }.forEach { (entity, data) ->
            val dataS = entityList[entity] ?: return@forEach
            val startVector = dataS.startVector ?: return@forEach
            val currVector = entity.positionVector  ?: return@forEach
            val endVector = data.endVector  ?: return@forEach
            val lastEndVector = data.lastEndVector  ?: return@forEach
            val endVectorUpdated = min(ticktime - data.endVecUpdated!!, 100)

            val speedVectors = Vec3(
                (currVector.xCoord - (startVector.xCoord)) / dataS.timetook!!,
                (currVector.yCoord - (startVector.yCoord)) / dataS.timetook!!,
                (currVector.zCoord - (startVector.zCoord)) / dataS.timetook!!
            )

            val time = data.time ?: return@forEach
            val ping = ServerUtils.averagePing

            val endPoint = Vec3(
                lastEndVector.xCoord + (((endVector.xCoord) - lastEndVector.xCoord) * endVectorUpdated) / 100,
                lastEndVector.yCoord + (((endVector.yCoord) - lastEndVector.yCoord) * endVectorUpdated) / 100,
                lastEndVector.zCoord + (((endVector.zCoord) - lastEndVector.zCoord) * endVectorUpdated) / 100,
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

            if (ping < time) {
                Renderer.drawBox(pingAABB, mboxColor, fillAlpha = 0f, outlineAlpha = mboxColor.alpha)
                Renderer.drawBox(endAABB, fboxColor, fillAlpha = 0f, outlineAlpha = fboxColor.alpha)
            } else {
                Renderer.drawBox(endAABB, Color.PINK, fillAlpha = 0f)
            }
            val timeDisplay = (time.toFloat() - offset) / 1000
            val color = when {
                timeDisplay > 1.5 -> Color.GREEN
                timeDisplay in 0.5..1.5 -> Color.ORANGE
                timeDisplay in 0.0..0.5 -> Color.RED
                else -> Color.BLUE
            }
            if (drawTime) Renderer.drawStringInWorld("${timeDisplay}s", Vec3(endPoint.xCoord, endPoint.yCoord + 2f, endPoint.zCoord), color)
        }

    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        ticktime += 50
    }


    init {
        execute(100) {
            onTick()
        }

        onPacket(S17PacketEntityLookMove::class.java) {
            onPacketLookMove(it)
        }

        onMessage(firstspawnregex) {
            firstSpawns = false
        }

        onWorldLoad {
            entityList.clear()
            firstSpawns = true
            watcher.clear()
            forRender.clear()
            ticktime = 0
        }
    }

}
