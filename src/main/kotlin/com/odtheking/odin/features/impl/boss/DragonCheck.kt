package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.renderPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import java.util.*

object DragonCheck {

    val dragonHealthMap = mutableMapOf<UUID, Pair<Vec3, Float>>()
    var lastDragonDeath: WitherDragonsEnum? = null

    fun dragonUpdate(packet: ClientboundSetEntityDataPacket) {
        val entity = mc.level?.getEntity(packet.id) as? EnderDragon ?: return
        val dragonHealth = (packet.packedItems.find { it.id == 9 }?.value as? Float) ?: return
        dragonHealthMap[entity.uuid] = Pair(entity.renderPos, dragonHealth)

        WitherDragonsEnum.entries.firstOrNull { it.entityUUID == entity.uuid }?.let {
            if (dragonHealth <= 0 && it.state != WitherDragonState.DEAD) it.setDead(true)
        }
    }

    fun dragonSpawn(packet: ClientboundAddEntityPacket) {
        if (packet.type == EntityType.ENDER_DRAGON)
            WitherDragonsEnum.entries.find {
                it.aabbDimensions.contains(Vec3(packet.x, packet.y, packet.z))
            }?.setAlive(packet.uuid)
    }

    fun dragonSprayed(packet: ClientboundSetEquipmentPacket) {
        if (packet.slots.none { it.second.item == Items.PACKED_ICE }) return

        val sprayedEntity = mc.level?.getEntity(packet.entity) as? ArmorStand ?: return

        WitherDragonsEnum.entries.forEach { dragon ->
            val entity = mc.level?.getEntity(dragon.entityUUID ?: return@forEach) as? EnderDragon ?: return@forEach
            if (dragon.isSprayed || dragon.state != WitherDragonState.ALIVE || sprayedEntity.distanceTo(entity) > 8) return@forEach

            if (WitherDragons.sendSpray) {
                modMessage("§${dragon.colorCode}${dragon.name} §fdragon was sprayed in §c${(WitherDragons.currentTick - dragon.spawnedTime).let { 
                    "$it §ftick${if (it > 1) "s" else ""}" 
                }}.")
            }
            dragon.isSprayed = true
        }
    }
}

