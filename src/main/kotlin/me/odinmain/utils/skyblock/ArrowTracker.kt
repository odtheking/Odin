package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ArrowEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.utils.*
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S13PacketDestroyEntities
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.Vec3i
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap

object ArrowTracker {
    private val ownedArrows = ConcurrentHashMap<Vec3i, OwnedData>()
    private val arrows = ConcurrentHashMap<Int, ArrowData>()

    private var currentTick = 0L

    @SubscribeEvent
    fun onArrowHit(event: ArrowEvent.Hit) {
        if (event.target.isEntityAlive) arrows[event.arrow.entityId]?.entitiesHit?.add(event.target)
    }

    @SubscribeEvent
    fun onMetadata(event: PostEntityMetadata) = with(event.packet) {
        arrows[entityId]?.takeIf { it.arrow == null }?.let {
            val arrow = mc.theWorld.getEntityByID(entityId) as? EntityArrow ?: return@with

            it.arrow = arrow
            it.owner = findOwner(arrow)
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) = with(event.packet) {
        when (this) {
            is S0EPacketSpawnObject -> if (type == 60) arrows[entityID] = ArrowData()
            is S13PacketDestroyEntities -> entityIDs.forEach {
                arrows.remove(it)?.run {
                    ArrowEvent.Despawn(arrow ?: return@run, owner ?: return@run, entitiesHit).postAndCatch()
                }
            }
            is S32PacketConfirmTransaction -> {
                currentTick++
                ownedArrows.entries.removeAll { currentTick - it.value.addedTime > 12 }
            }
            else -> return@with
        }
    }

    private fun findOwner(packet: EntityArrow): Entity? = with(packet) {
        arrows[entityId]?.owner?.let { return it }
        val arrowPos = Vec3i(serverPosX, serverPosY, serverPosZ)

        shootingEntity?.let {
            ownedArrows[arrowPos] = OwnedData(it, currentTick)
            return it
        }

        return (ownedArrows[arrowPos] ?: ownedArrows[arrowPos.addVec(y=16)])?.owner
    }

    data class OwnedData(val owner: Entity, val addedTime: Long)
    data class ArrowData(var owner: Entity? = null, var arrow: EntityArrow? = null, val entitiesHit: ArrayList<Entity> = ArrayList())
}