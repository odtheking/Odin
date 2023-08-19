package me.odinclient.features.impl.floor7

import me.odinclient.utils.WebUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// This is currently broken and i cba to fix it
object DragonDeathCheck {
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
    private var webhook: String? = null
    private var last: Pair<Vec3, DragonColors>? = null

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        webhook = WebUtils.fetchURLData("https://pastebin.com/raw/NM5WD0Ym")
        dragonMap = HashMap()
        last = null
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (!DungeonUtils.inDungeons) return
        val entity = event.entity
        if (entity !is EntityDragon) return

        val color = DragonColors.values().find { it.pos == Vec3(entity.posX, entity.posY, entity.posZ) } ?: return
        dragonMap.plus(Pair(entity.entityId, color))
    }

    @SubscribeEvent
    fun onEntityLeave(event: LivingDeathEvent) {
        if (event.entity !is EntityDragon || !DungeonUtils.inDungeons) return
        val color = dragonMap[event.entity.entityId] ?: return
        last = Pair(Vec3(event.entity.posX, event.entity.posY, event.entity.posZ), color)
        dragonMap = dragonMap.minus(event.entity.entityId)
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        if (
            !DungeonUtils.inDungeons ||
            last == null ||
            webhook == null ||
            (message != "[BOSS] Wither King: Oh, this one hurts!" &&
            message != "[BOSS] Wither King: I have more of those" &&
            message != "[BOSS] Wither King: My soul is disposable.")
        ) return

        val (vec, color) = last!!
        //ChatUtils.modMessage("$color dragon counted!")
        if (color == DragonColors.Purple) return

        WebUtils.sendDiscordWebhook(
            webhook!!,
            "Dragon Counted",
            "Color: $color x: ${vec.xCoord} y: ${vec.yCoord} z: ${vec.zCoord}",
            4081151
        )
    }
}