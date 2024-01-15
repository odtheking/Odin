package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.impl.floor7.WitherDragons.sendNotif
import me.odinmain.features.impl.floor7.WitherDragons.sendSpawned
import me.odinmain.features.impl.floor7.WitherDragons.sendTime
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent

object DragonDeathCheck {

    var lastDragonDeath = ""

    fun dragonJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityDragon) return
        val dragon = WitherDragonsEnum.entries.find { dragon -> event.entity.positionVector.dragonCheck(dragon.spawnPos) } ?: return
        dragon.spawning = false
        dragon.dragonAlive = true
        dragon.timesSpawned += 1
        dragon.entityID = event.entity.entityId
        if (sendSpawned) modMessage("§${dragon.colorCode}${dragon.name} §fdragon spawned. This is the §${dragon.colorCode}${dragon.timesSpawned}§f time it has spawned.")
    }

    fun dragonLeaveWorld(event: LivingDeathEvent) {
        if (event.entity !is EntityDragon) return
        val dragon = WitherDragonsEnum.entries.find {it.entityID == event.entity.entityId} ?: return

        WitherDragonsEnum.entries.find{ it.name == dragon.name }!!.lastDeathLocation = event.entity.position

        if (sendTime) {
            val oldPB = dragon.dragonKillPBs.value
            val killTime = event.entity.ticksExisted / 20.0
            if (dragon.dragonKillPBs.value < event.entity.ticksExisted / 20.0) dragon.dragonKillPBs.value = killTime

            modMessage("§${dragon.colorCode}${dragon.name} §fdragon was alive for ${printSecondsWithColor(killTime, 3.5, 7.5, down = false)}${if (killTime < oldPB) " §7(§dNew PB§7)" else ""}.")
        }
        WitherDragonsEnum.entries.find{ dragon.name == it.name }?.dragonAlive = false
        lastDragonDeath = dragon.name
    }

    fun onChatPacket(event: ChatPacketEvent) {
        if (
            !event.message.equalsOneOf(
                "[BOSS] Wither King: Oh, this one hurts!",
                "[BOSS] Wither King: I have more of those",
                "[BOSS] Wither King: My soul is disposable."
            )
        ) return

        val dragon = WitherDragonsEnum.entries.find { lastDragonDeath == it.name } ?: return

        if (sendNotif) modMessage("§${dragon.colorCode}${dragon.name} dragon counts.")
    }

    private fun Vec3.dragonCheck(vec3: Vec3): Boolean {
        return this.xCoord == vec3.xCoord && this.yCoord == vec3.yCoord && this.zCoord == vec3.zCoord
    }

    private fun printSecondsWithColor(time1: Double, time2: Double, time3: Double, down: Boolean = true, colorCode1: String = "a", colorCode2: String = "6", colorCode3: String = "c"): String {
        val colorCode = if (down) {
            when {
                time1 <= time2 -> colorCode3
                time1 <= time3 -> colorCode2
                else -> colorCode1
            }
        } else {
            when {
                time1 <= time2 -> colorCode1
                time1 <= time3 -> colorCode2
                else -> colorCode3
            }
        }
        return "§$colorCode${time1}s"
    }
}