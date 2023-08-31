package me.odinclient.features.impl.floor7

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.Vec3
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object RelicAura : Module(
    name = "Relic Aura",
    category = Category.FLOOR7,
    description = "Automatically picks up relics in the Wither King boss-fight.",
    risky = true
){
    private var disabler = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        disabler = false
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Wither King: You.. again?") disabler = true
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (disabler || !DungeonUtils.inDungeons) return
        val armorStands = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>() ?: return
        for (armorStand in armorStands) {
            if (armorStand.inventory?.get(4)?.displayName?.contains("Relic") != true || mc.thePlayer.getDistanceToEntity(armorStand) > 4) continue
            interactWithEntity(armorStand)
        }
    }

    private fun interactWithEntity(entity: Entity) {
        val objectMouseOver = mc.objectMouseOver.hitVec
        val dx = objectMouseOver.xCoord - entity.posX
        val dy = objectMouseOver.yCoord - entity.posY
        val dz = objectMouseOver.zCoord - entity.posX
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, Vec3(dx, dy, dz)))
    }
}