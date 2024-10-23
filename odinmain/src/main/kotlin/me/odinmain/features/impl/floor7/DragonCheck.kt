package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.WitherDragons.sendNotification
import me.odinmain.features.impl.floor7.WitherDragons.sendSpray
import me.odinmain.utils.isVecInXZ
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.util.Vec3

object DragonCheck {

    var lastDragonDeath: WitherDragonsEnum = WitherDragonsEnum.None
    val dragonEntityList = mutableListOf<EntityDragon>()

    fun dragonUpdate(packet: S1CPacketEntityMetadata) {
        val dragon = WitherDragonsEnum.entries.find { it.entityId == packet.entityId } ?: return
        if (dragon.entity == null) return dragon.updateEntity(packet.entityId)
        val health = packet.func_149376_c().find { it.dataValueId == 6 }?.`object` as? Float ?: return
        if (health > 0 || dragon.state == WitherDragonState.DEAD) return
        dragon.setDead()
    }

    fun dragonSpawn(packet: S0FPacketSpawnMob) {
        val dragon = WitherDragonsEnum.entries
            .find { isVecInXZ(Vec3(packet.x / 32.0, packet.y / 32.0, packet.z / 32.0), it.boxesDimensions) }
            ?.takeIf { it.state == WitherDragonState.SPAWNING } ?: return
        dragon.setAlive(packet.entityID)
    }

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