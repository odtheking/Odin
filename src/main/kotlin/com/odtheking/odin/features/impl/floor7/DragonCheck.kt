package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.EntityEvent
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.renderPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import java.util.*

object DragonCheck {

    val dragonHealthMap = mutableMapOf<UUID, Pair<Vec3, Float>>()
    var lastDragonDeath: WitherDragonsEnum? = null

    fun dragonUpdate(event: EntityEvent.SetData) {
        val entity = event.entity as? EnderDragon ?: return
        val dragonHealth = (event.synchedDataValues.find { it.id == 9 }?.value as? Float) ?: return
        dragonHealthMap[entity.uuid] = Pair(entity.renderPos, dragonHealth)

        WitherDragonsEnum.entries.firstOrNull { it.entityUUID == entity.uuid }?.let {
            if (dragonHealth <= 0 && it.state != WitherDragonState.DEAD) it.setDead(true)
        }
    }

    fun dragonSpawn(event: EntityEvent.Add) {
        if (event.entity.type == EntityType.ENDER_DRAGON)
            WitherDragonsEnum.entries.find {
                it.aabbDimensions.contains(Vec3(event.entity.x, event.entity.y, event.entity.z))
            }?.setAlive(event.entity.uuid)
    }

    fun dragonSprayed(event: EntityEvent.SetItemSlot) {
        if (event.stack.item == Items.PACKED_ICE ) return

        val sprayedEntity = event.entity as? ArmorStand ?: return

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

