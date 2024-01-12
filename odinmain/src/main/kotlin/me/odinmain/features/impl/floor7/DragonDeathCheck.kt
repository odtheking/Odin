package me.odinmain.features.impl.floor7

import kotlinx.coroutines.launch
import me.odinmain.OdinMain.scope
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.impl.floor7.WitherDragons.sendNotif
import me.odinmain.features.impl.floor7.WitherDragons.sendTime
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.round
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toVec3
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent

object DragonDeathCheck {

    var dragonMap: Map<Int, WitherDragonsEnum> = HashMap()
    var deadDragonMap: Map<Vec3, WitherDragonsEnum> = HashMap()


    fun dragonJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityDragon) return

        val color = WitherDragonsEnum.entries.find { color -> event.entity.positionVector.dragonCheck(color.pos.toVec3()) } ?: return

        dragonMap = dragonMap.plus(Pair(event.entity.entityId, color))
    }


    fun dragonLeaveWorld(event: LivingDeathEvent) {
        if (event.entity !is EntityDragon) return
        val color = dragonMap[event.entity.entityId] ?: return

        deadDragonMap = deadDragonMap.plus(Pair(Vec3(event.entity.posX.round(1), event.entity.posY.round(1), event.entity.posZ.round(1)), color))

        if (sendTime) {
            val dragon = WitherDragonsEnum.entries.find { color.name == it.name } ?: return
            val oldPB = dragon.setting.value
            val killTime = event.entity.ticksExisted / 20.0
            if (killTime < oldPB)
                dragon.setting.value = killTime
            modMessage("§${dragon.colorCode}$color §fdragon was alive for ${printSecondsWithColor(killTime, 3.5, 7.5, down = false)}${if (killTime < oldPB) " §7(§dNew PB§7)" else ""}.")
        }
        dragonMap = dragonMap.minus(event.entity.entityId)
    }

    fun onChatPacket(event: ChatPacketEvent) {
        if (
            !DungeonUtils.inDungeons ||
            !event.message.equalsOneOf(
                "[BOSS] Wither King: Oh, this one hurts!",
                "[BOSS] Wither King: I have more of those",
                "[BOSS] Wither King: My soul is disposable."
            )
        ) return

        val (vec, color) = deadDragonMap.entries.firstOrNull() ?: return
        deadDragonMap = deadDragonMap.minus(deadDragonMap.keys.first())

        if (sendNotif) modMessage("§${WitherDragonsEnum.entries.find { color.name == it.name }?.colorCode}$color dragon counts.")

        if (color == WitherDragonsEnum.Purple) return

        scope.launch {
            sendDataToServer("""{"dd": "$color\nx: ${vec.xCoord}\ny: ${vec.yCoord}\nz: ${vec.zCoord}"}""")
        }
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