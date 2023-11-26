package me.odinmain.features.impl.floor7

import kotlinx.coroutines.launch
import me.odinmain.OdinMain.scope
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.round
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@AlwaysActive
object DragonDeathCheck : Module(
    "Dragon Death",
    category = Category.FLOOR7,
    description = "Displays data about dragon death."
) {
    private val sendNotif: Boolean by BooleanSetting("Send Dragon Confirmation", true)
    private val sendTime: Boolean by BooleanSetting("Send Dragon Time Alive", true)

    private val redPB = +NumberSetting("Panes PB", 1000.0, increment = 0.01, hidden = true)
    private val orangePB = +NumberSetting("Color PB", 1000.0, increment = 0.01, hidden = true)
    private val greenPB = +NumberSetting("Numbers PB", 1000.0, increment = 0.01, hidden = true)
    private val bluePB = +NumberSetting("Melody PB", 1000.0, increment = 0.01, hidden = true)
    private val purplePB = +NumberSetting("Starts With PB", 1000.0, increment = 0.01, hidden = true)

    private enum class Dragons(
        val pos: Vec3,
        val colorCode: String,
        val setting: NumberSetting<Double>
    ) {
        Red(Vec3(27.0, 14.0, 59.0), "c", redPB),
        Orange(Vec3(85.0, 14.0, 56.0), "6", orangePB),
        Green(Vec3(27.0, 14.0, 94.0), "a", greenPB),
        Blue(Vec3(84.0, 14.0, 94.0), "b", bluePB),
        Purple(Vec3(56.0, 14.0, 125.0), "5", purplePB)
    }

    private var dragonMap: Map<Int, Dragons> = HashMap()
    private var deadDragonMap: Map<Vec3, Dragons> = HashMap()

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        dragonMap = HashMap()
        deadDragonMap = HashMap()
    }

    private fun Vec3.dragonCheck(vec3: Vec3): Boolean {
        return this.xCoord == vec3.xCoord && this.yCoord == vec3.yCoord && this.zCoord == vec3.zCoord
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityDragon || !DungeonUtils.inDungeons) return

        val color = Dragons.entries.find { color -> event.entity.positionVector.dragonCheck(color.pos) } ?: return

        dragonMap = dragonMap.plus(Pair(event.entity.entityId, color))
    }

    @SubscribeEvent
    fun onEntityLeave(event: LivingDeathEvent) {
        if (event.entity !is EntityDragon || !DungeonUtils.inDungeons) return
        val color = dragonMap[event.entity.entityId] ?: return

        deadDragonMap = deadDragonMap.plus(Pair(Vec3(event.entity.posX.round(1), event.entity.posY.round(1), event.entity.posZ.round(1)), color))

        if (sendTime && enabled) {
            val dragon = Dragons.entries.find { color.name == it.name } ?: return
            val oldPB = dragon.setting.value
            val killTime = event.entity.ticksExisted / 20.0
            if (killTime < oldPB)
                dragon.setting.value = killTime
            modMessage("§${dragon.colorCode}$color §fdragon was alive for ${printSecondsWithColor(killTime, 3.5, 7.5, down = false)}${if (killTime < oldPB) " §7(§dNew PB§7)" else ""}.")
        }
        dragonMap = dragonMap.minus(event.entity.entityId)
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
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

        if (sendNotif && enabled) modMessage("§${Dragons.entries.find { color.name == it.name }?.colorCode}$color dragon counts.")

        if (color == Dragons.Purple) return

        scope.launch {
            sendDataToServer("""{"dd": "$color\nx: ${vec.xCoord}\ny: ${vec.yCoord}\nz: ${vec.zCoord}"}""")
        }
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