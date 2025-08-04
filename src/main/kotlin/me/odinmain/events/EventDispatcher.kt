package me.odinmain.events

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.*
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonItemDrops
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inBoss
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.isSecret
import me.odinmain.utils.skyblock.getBlockStateAt
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.entity.item.EntityItem
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EventDispatcher {

    /**
     * Dispatches [SecretPickupEvent.Item]
     */
    @SubscribeEvent
    fun onRemoveEntity(event: EntityLeaveWorldEvent) = with(event.entity) {
        if (inDungeons && this is EntityItem && this.entityItem?.unformattedName?.containsOneOf(dungeonItemDrops, true) != false && mc.thePlayer.getDistanceToEntity(this) <= 6)
            SecretPickupEvent.Item(this).postAndCatch()
    }

    /**
     * Dispatches [SecretPickupEvent.Interact]
     */
    @SubscribeEvent
    fun onPacket(event: PacketEvent.Send) = with(event.packet) {
        if (inDungeons && this is C08PacketPlayerBlockPlacement && position != null)
            SecretPickupEvent.Interact(position, getBlockStateAt(position).takeIf { isSecret(it, position) } ?: return).postAndCatch()
    }

    /**
     * Dispatches [ChatPacketEvent] and [SecretPickupEvent.Bat]
     */
    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (event.packet is S32PacketConfirmTransaction && !event.packet.func_148888_e()) ServerTickEvent().postAndCatch()

        if (event.packet is S29PacketSoundEffect && inDungeons && !inBoss && (event.packet.soundName.equalsOneOf("mob.bat.hurt", "mob.bat.death") && event.packet.volume == 0.1f)) SecretPickupEvent.Bat(event.packet).postAndCatch()

        if (event.packet is S02PacketChat && ChatPacketEvent(event.packet.chatComponent.unformattedText.noControlCodes).postAndCatch())
            event.isCanceled = true

    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {

    }
}
