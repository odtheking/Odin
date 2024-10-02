package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.DragonPriority.displaySpawningDragon
import me.odinmain.features.impl.floor7.DragonPriority.findPriority
import me.odinmain.features.impl.floor7.WitherDragons.arrowDeath
import me.odinmain.features.impl.floor7.WitherDragons.arrowSpawn
import me.odinmain.features.impl.floor7.WitherDragons.priorityDragon
import me.odinmain.features.impl.floor7.WitherDragons.sendArrowHit
import me.odinmain.features.impl.floor7.WitherDragons.sendNotification
import me.odinmain.features.impl.floor7.WitherDragons.sendSpawned
import me.odinmain.features.impl.floor7.WitherDragons.sendSpray
import me.odinmain.features.impl.floor7.WitherDragons.sendTime
import me.odinmain.features.impl.skyblock.ArrowHit.onDragonSpawn
import me.odinmain.features.impl.skyblock.ArrowHit.resetOnDragons
import me.odinmain.utils.isVecInXZ
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent

object DragonCheck {

    var lastDragonDeath: WitherDragonsEnum = WitherDragonsEnum.None
    var dragonEntityList = emptyList<EntityDragon>()

    fun dragonJoinWorld(event: EntityJoinWorldEvent) {
        val entity = event.entity as? EntityDragon ?: return
        val dragon = WitherDragonsEnum.entries.find { isVecInXZ(entity.positionVector, it.boxesDimensions) } ?: return
        if (dragon.state == WitherDragonState.DEAD) return

        dragon.state = WitherDragonState.ALIVE
        dragon.timeToSpawn = 100
        dragon.timesSpawned += 1
        dragon.entity = entity
        dragon.spawnedTime = System.currentTimeMillis()
        dragon.isSprayed = false

        if (sendArrowHit && WitherDragons.enabled) arrowSpawn(dragon)
        if (resetOnDragons && WitherDragons.enabled) onDragonSpawn()
        if (sendSpawned && WitherDragons.enabled) {
            val numberSuffix = when (dragon.timesSpawned) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
            modMessage("§${dragon.colorCode}${dragon.name} §fdragon spawned. This is the §${dragon.colorCode}${dragon.timesSpawned}${numberSuffix}§f time it has spawned.")
        }
    }

    fun dragonLeaveWorld(event: LivingDeathEvent) {
        val entity = event.entity as? EntityDragon ?: return
        val dragon = WitherDragonsEnum.entries.find { it.entity?.entityId == entity.entityId } ?: return
        dragon.state = WitherDragonState.DEAD
        lastDragonDeath = dragon

        if (sendTime && WitherDragons.enabled)
            dragonPBs.time(dragon.ordinal, entity.ticksExisted / 20.0, "s§7!", "§${dragon.colorCode}${dragon.name} §7was alive for §6", addPBString = true, addOldPBString = true)

        if (sendArrowHit && WitherDragons.enabled) arrowDeath(dragon)
    }

    fun dragonSprayed(packet: S04PacketEntityEquipment) {
        if (packet.itemStack?.item != Item.getItemFromBlock(Blocks.packed_ice)) return
        val sprayedEntity = mc.theWorld?.getEntityByID(packet.entityID) as? EntityArmorStand ?: return

        WitherDragonsEnum.entries.filter{ !it.isSprayed && it.state == WitherDragonState.ALIVE && sprayedEntity.getDistanceToEntity(it.entity) <= 8 }.forEach {
            if (sendSpray) modMessage("§${it.colorCode}${it.name} §fdragon was sprayed in §c${System.currentTimeMillis() - it.spawnedTime}§fms ")
            it.isSprayed = true
        }
    }

    fun onChatPacket() {
        WitherDragonsEnum.entries.find { lastDragonDeath == it }?.let {
            if (sendNotification && WitherDragons.enabled) modMessage("§${it.colorCode}${it.name} dragon counts.")
        }
    }

    fun dragonStateConfirmation() {
        dragonEntityList = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityDragon>() ?: return
        WitherDragonsEnum.entries.forEach { dragon ->
            dragon.state = if (dragon.entity !in dragonEntityList && dragon.state == WitherDragonState.ALIVE) WitherDragonState.DEAD else dragon.state
            dragon.state = if (dragon.state == WitherDragonState.SPAWNING && dragon.timeToSpawn == 0) WitherDragonState.ALIVE else dragon.state
        }
    }

    fun updateTime() {
        WitherDragonsEnum.entries.forEach { dragon ->
            if (dragon.state != WitherDragonState.SPAWNING) return@forEach
            dragon.timeToSpawn--.coerceAtLeast(0)
            if (dragon.timeToSpawn != 98) return@forEach
            priorityDragon = findPriority(WitherDragonsEnum.entries.filter { it.state == WitherDragonState.SPAWNING }.toMutableList())
            displaySpawningDragon(priorityDragon)
        }
    }
}