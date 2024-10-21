package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.WitherDragons.arrowDeath
import me.odinmain.features.impl.floor7.WitherDragons.arrowSpawn
import me.odinmain.features.impl.floor7.WitherDragons.sendArrowHit
import me.odinmain.features.impl.floor7.WitherDragons.sendNotification
import me.odinmain.features.impl.floor7.WitherDragons.sendSpawned
import me.odinmain.features.impl.floor7.WitherDragons.sendSpray
import me.odinmain.features.impl.floor7.WitherDragons.sendTime
import me.odinmain.features.impl.skyblock.ArrowHit.onDragonSpawn
import me.odinmain.features.impl.skyblock.ArrowHit.resetOnDragons
import me.odinmain.utils.isVecInXZ
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent

object DragonCheck {

    var lastDragonDeath: WitherDragonsEnum = WitherDragonsEnum.None
    val dragonEntityList = mutableListOf<EntityDragon>()

//    fun dragonJoinWorld(event: EntityJoinWorldEvent) {
//        val entity = event.entity as? EntityDragon ?: return
//        val dragon = WitherDragonsEnum.entries.find { isVecInXZ(entity.positionVector, it.boxesDimensions) } ?: return
//        if (dragon.state == WitherDragonState.DEAD) return
//
//        dragon.state = WitherDragonState.ALIVE
//        dragon.timeToSpawn = 100
//        dragon.timesSpawned += 1
//        dragon.entity = entity
//        dragon.spawnedTime = System.currentTimeMillis()
//        dragon.isSprayed = false
//
//        if (sendArrowHit && WitherDragons.enabled) arrowSpawn(dragon)
//        if (resetOnDragons && WitherDragons.enabled) onDragonSpawn()
//        if (sendSpawned && WitherDragons.enabled) {
//            val numberSuffix = when (dragon.timesSpawned) {
//                1 -> "st"
//                2 -> "nd"
//                3 -> "rd"
//                else -> "th"
//            }
//            modMessage("§${dragon.colorCode}${dragon.name} §fdragon spawned. This is the §${dragon.colorCode}${dragon.timesSpawned}${numberSuffix}§f time it has spawned.")
//        }
//    }

    fun dragonUpdate(packet: S1CPacketEntityMetadata) {
        val dragon = WitherDragonsEnum.entries.find { it.entityId == packet.entityId } ?: return
        if (dragon.entity == null) return dragon.updateEntity(packet.entityId)
        val health = packet.func_149376_c().find { it.dataValueId == 6 }?.`object` as? Float ?: return
        if (health > 0 || dragon.state == WitherDragonState.DEAD) return
        dragon.setDead()
    }

    fun dragonSpawn(packet: S0FPacketSpawnMob) {
        val dragon = WitherDragonsEnum.entries.find { isVecInXZ(Vec3(packet.x / 32.0, packet.y / 32.0, packet.z / 32.0), it.boxesDimensions) } ?: return
        if (dragon.state != WitherDragonState.SPAWNING) return
        dragon.setAlive(packet.entityID)
    }

//    fun dragonDeath(entityId: Int) {
//        val dragon = WitherDragonsEnum.entries.find { it.entity?.entityId == entityId } ?: return
//        modMessage("dragon death from status")
//        dragon.state = WitherDragonState.DEAD
//        lastDragonDeath = dragon
//
//        if (sendTime && WitherDragons.enabled)
//            dragonPBs.time(dragon.ordinal, (System.currentTimeMillis() - dragon.spawnedTime) / 1000.0, "s§7!", "§${dragon.colorCode}${dragon.name} §7was alive for §6", addPBString = true, addOldPBString = true)
//
//        if (sendArrowHit && WitherDragons.enabled) arrowDeath(dragon)
//    }

    fun dragonSprayed(packet: S04PacketEntityEquipment) {
        if (packet.itemStack?.item != Item.getItemFromBlock(Blocks.packed_ice)) return
        val sprayedEntity = mc.theWorld?.getEntityByID(packet.entityID) as? EntityArmorStand ?: return

        WitherDragonsEnum.entries.forEach { dragon ->
            if (!dragon.isSprayed && dragon.state == WitherDragonState.ALIVE && dragon.entity != null && sprayedEntity.getDistanceToEntity(dragon.entity) <= 8) {
                if (sendSpray) modMessage("§${dragon.colorCode}${dragon.name} §fdragon was sprayed in §c${System.currentTimeMillis() - dragon.spawnedTime}§fms ")
                dragon.isSprayed = true
            }
        }
    }

    fun onChatPacket() {
        WitherDragonsEnum.entries.find { lastDragonDeath == it }?.let {
            if (lastDragonDeath == WitherDragonsEnum.None) return
            if (sendNotification && WitherDragons.enabled) modMessage("§${it.colorCode}${it.name} dragon counts.")
        }
    }

    fun updateTime() {
        WitherDragonsEnum.entries.forEach { dragon ->
            if (dragon.state != WitherDragonState.SPAWNING) return@forEach
            dragon.timeToSpawn = (dragon.timeToSpawn - 1).coerceAtLeast(0)
        }
    }
}