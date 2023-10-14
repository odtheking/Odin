package me.odinmain.features.impl.floor7

import kotlinx.coroutines.launch
import me.odinmain.OdinMain.scope
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.WebUtils
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@AlwaysActive
object DragonDeathCheck : Module(
    "Dragon Death",
    category = Category.FLOOR7

) {
    private val sendNotif: Boolean by BooleanSetting("Send dragon confirmation", true)
    private val sendTime: Boolean by BooleanSetting("Send dragon time alive", true)

    private enum class Dragons(
        val pos: Vec3,
        val colorCodes: String
    ) {
        Red(Vec3(27.0, 14.0, 59.0), "c"),
        Orange(Vec3(85.0, 14.0, 56.0), "6"),
        Green(Vec3(27.0, 14.0, 94.0), "a"),
        Blue(Vec3(84.0, 14.0, 94.0), "b"),
        Purple(Vec3(56.0, 14.0, 125.0), "5")
    }

    private var dragonMap: Map<Int, Dragons> = HashMap()
    private var deadDragonMap: Map<Vec3, Dragons> = HashMap()
    private val webhook: String = WebUtils.fetchURLData("https://pastebin.com/raw/NM5WD0Ym")

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

        if (sendTime && enabled) ChatUtils.modMessage("§${Dragons.entries.find { color.name == it.name }?.colorCodes}$color §fdragon was alive for ${printSecondsWithColor(event.entity.ticksExisted.toFloat() / 20.0, 3.5, 7.5, down = false)}.")
        dragonMap = dragonMap.minus(event.entity.entityId)
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        if (
            !DungeonUtils.inDungeons ||
            deadDragonMap.entries.firstOrNull() == null ||
            webhook.isEmpty() ||
            (
               message != "[BOSS] Wither King: Oh, this one hurts!" &&
               message != "[BOSS] Wither King: I have more of those" &&
               message != "[BOSS] Wither King: My soul is disposable."
            )
        ) return

        val (vec, color) = deadDragonMap.entries.firstOrNull()!!
        deadDragonMap = deadDragonMap.minus(deadDragonMap.keys.first())

        if (sendNotif && enabled) ChatUtils.modMessage("§${Dragons.entries.find { color.name == it.name }?.colorCodes}$color dragon counts.")
        if (color == Dragons.Purple) return

        scope.launch{
            WebUtils.sendDiscordWebhook(
            webhook,
            "Dragon Counted",
            "Color: $color x: ${vec.xCoord} y: ${vec.yCoord} z: ${vec.zCoord}",
            4081151)
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