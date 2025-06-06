package me.odinmain.events

import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.events.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonItemDrops
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inBoss
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.isSecret
import me.odinmain.utils.skyblock.getBlockStateAt
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EventDispatcher {

    /**
     * Dispatches [SecretPickupEvent.Item]
     */
    @SubscribeEvent
    fun onRemoveEntity(event: EntityLeaveWorldEvent) = with(event.entity) {
        if (!inDungeons || this !is EntityItem) return
        val name = this.entityItem?.unformattedName ?: return
        if (!name.containsOneOf(dungeonItemDrops, ignoreCase = true)) return
        if (mc.thePlayer.getDistanceSqToEntity(this) > 36) return
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
        if (event.packet is S29PacketSoundEffect && inDungeons && !inBoss && (event.packet.soundName.equalsOneOf("mob.bat.hurt", "mob.bat.death") && event.packet.volume == 0.1f)) SecretPickupEvent.Bat(event.packet).postAndCatch()

        if (event.packet !is S02PacketChat || !ChatPacketEvent(event.packet.chatComponent.unformattedText.noControlCodes).postAndCatch()) return
        event.isCanceled = true
    }

    /**
     * Dispatches [GuiEvent.Loaded]
     */
    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) = scope.launch {
        val chestGui = event.gui as? GuiChest ?: return
        val container = chestGui.inventorySlots as? ContainerChest ?: return

        scope.launch {
            val deferred = waitUntilLastItem(container)
            try { deferred.await() } catch (_: Exception) { return@launch }

            GuiEvent.Loaded(container, container.name).postAndCatch()
        }
    }
}
