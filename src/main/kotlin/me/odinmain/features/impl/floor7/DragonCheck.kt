package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.WitherDragons.currentTick
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
import java.util.concurrent.CopyOnWriteArrayList

object DragonCheck {

    var lastDragonDeath: WitherDragonsEnum = WitherDragonsEnum.None
    val dragonEntityList = CopyOnWriteArrayList<EntityDragon>()

    fun dragonUpdate(packet: S1CPacketEntityMetadata) {
        val dragon = WitherDragonsEnum.entries.find { it.entityId == packet.entityId }?.apply { if (entity == null) updateEntity(packet.entityId) } ?: return
        (packet.func_149376_c().find { it.dataValueId == 6 }?.`object` as? Float)?.let { health ->
            if (health <= 0 && dragon.state != WitherDragonState.DEAD) dragon.setDead()
        }
    }

    fun dragonSpawn(packet: S0FPacketSpawnMob) {
        WitherDragonsEnum.entries.find {
            isVecInXZ(Vec3(packet.x / 32.0, packet.y / 32.0, packet.z / 32.0), it.boxesDimensions) && it.state == WitherDragonState.SPAWNING
        }?.setAlive(packet.entityID)
    }

    fun dragonSprayed(packet: S04PacketEntityEquipment) {
        if (packet.itemStack?.item != Item.getItemFromBlock(Blocks.packed_ice)) return
        val sprayedEntity = mc.theWorld?.getEntityByID(packet.entityID) as? EntityArmorStand ?: return

        WitherDragonsEnum.entries.forEach { dragon ->
            if (dragon.isSprayed || dragon.state != WitherDragonState.ALIVE || dragon.entity == null || sprayedEntity.getDistanceToEntity(dragon.entity) > 8) return@forEach
            if (sendSpray) modMessage("§${dragon.colorCode}${dragon.name} §fdragon was sprayed in §c${(currentTick - dragon.spawnedTime).let { "$it §ftick${if (it > 1) "s" else ""}" }}.")
            dragon.isSprayed = true
        }
    }
}