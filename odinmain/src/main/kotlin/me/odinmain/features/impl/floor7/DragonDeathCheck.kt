package me.odinmain.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.Utils.round
import me.odinmain.utils.VecUtils.equal
import me.odinmain.utils.WebUtils
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
    "Dragon death check",
    category = Category.FLOOR7

) {
    private val sendNotif: Boolean by BooleanSetting("Send dragon confirmation", true)

    private enum class DragonColors(
        val pos: Vec3
    ) {
        Red(Vec3(27.0,14.0,59.0)),
        Orange(Vec3(85.0, 14.0, 56.0)),
        Green(Vec3(27.0,14.0,94.0)),
        Blue(Vec3(84.0,14.0,94.0)),
        Purple(Vec3(56.0,14.0,125.0))
    }

    private var dragonMap: Map<Int, DragonColors> = HashMap()
    private val webhook: String = WebUtils.fetchURLData("https://pastebin.com/raw/NM5WD0Ym")
    private var last: Pair<Vec3, DragonColors>? = null

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        dragonMap = HashMap()
        last = null
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityDragon || !DungeonUtils.inDungeons) return

        val entityPos = event.entity.positionVector
        val color = DragonColors.entries.find { color -> entityPos.equal(color.pos) } ?: return
        dragonMap = dragonMap.plus(Pair(event.entity.entityId, color))
    }

    @SubscribeEvent
    fun onEntityLeave(event: LivingDeathEvent) {
        if (event.entity !is EntityDragon || !DungeonUtils.inDungeons) return
        val color = dragonMap[event.entity.entityId] ?: return
        last = Pair(Vec3(event.entity.posX.round(1), event.entity.posY.round(1), event.entity.posZ.round(1)), color)
        dragonMap = dragonMap.minus(event.entity.entityId)
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        if (
            !DungeonUtils.inDungeons ||
            last == null ||
            webhook.isEmpty() ||
            (message != "[BOSS] Wither King: Oh, this one hurts!" &&
                    message != "[BOSS] Wither King: I have more of those" &&
                    message != "[BOSS] Wither King: My soul is disposable." &&
                    !message.contains("hi"))
        ) return

        val (vec, color) = last!!
        last = null
        if (sendNotif) ChatUtils.modMessage("$color dragon counts!")
        if (color == DragonColors.Purple) return
        WebUtils.sendDiscordWebhook(
            webhook,
            "Dragon Counted",
            "Color: $color x: ${vec.xCoord} y: ${vec.yCoord} z: ${vec.zCoord}",
            4081151
        )
    }
}