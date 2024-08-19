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

    fun dragonJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityDragon) return
        val dragon = WitherDragonsEnum.entries.find { isVecInXZ(event.entity.positionVector, it.boxesDimensions) } ?: return

        dragon.state = WitherDragonState.ALIVE
        dragon.particleSpawnTime = 0L
        dragon.timesSpawned += 1
        dragon.entity = event.entity
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
        if (event.entity !is EntityDragon) return
        val dragon = WitherDragonsEnum.entries.find {it.entity?.entityId == event.entity?.entityId} ?: return
        dragon.state = WitherDragonState.DEAD
        lastDragonDeath = dragon

        if (sendTime && WitherDragons.enabled)
            dragonPBs.time(dragon.ordinal, event.entity.ticksExisted / 20.0, "s§7!", "§${dragon.colorCode}${dragon.name} §7was alive for §6", addPBString = true, addOldPBString = true)

        if (sendArrowHit && WitherDragons.enabled) arrowDeath(dragon)
    }

    fun dragonSprayed(packet: S04PacketEntityEquipment) {
        if (packet.itemStack?.item != Item.getItemFromBlock(Blocks.packed_ice)) return

        val sprayedEntity = mc.theWorld?.getEntityByID(packet.entityID) as? EntityArmorStand ?: return

        WitherDragonsEnum.entries.filter{ !it.isSprayed && it.state == WitherDragonState.ALIVE && sprayedEntity.getDistanceToEntity(it.entity) <= 8 }.forEach {
            val sprayedIn = (System.currentTimeMillis() - it.spawnedTime)
            if (sendSpray) modMessage("§${it.colorCode}${it.name} §fdragon was sprayed in §c${sprayedIn}§fms ")
            it.isSprayed = true
        }
    }

    fun onChatPacket() {
        val dragon = WitherDragonsEnum.entries.find { lastDragonDeath == it } ?: return
        if (sendNotification && WitherDragons.enabled) modMessage("§${dragon.colorCode}${dragon.name} dragon counts.")
    }

    fun dragonStateConfirmation() {
        val entities = mc.theWorld?.loadedEntityList.orEmpty()
        WitherDragonsEnum.entries.forEach { dragon ->
            dragon.state = if (dragon.entity !in entities && dragon.state == WitherDragonState.ALIVE) WitherDragonState.DEAD else dragon.state
        }
    }
}