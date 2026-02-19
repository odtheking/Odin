package com.odtheking.odin.events

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.utils.ChatManager
import com.odtheking.odin.utils.containsOneOf
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.RenderBatchManager
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.isSecret
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.item.ItemEntity

object EventDispatcher {

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            WorldEvent.Load.postAndCatch()
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            WorldEvent.Unload.postAndCatch()
        }

        ClientTickEvents.START_WORLD_TICK.register { world ->
            mc.level?.let { TickEvent.Start(world).postAndCatch() }
        }

        ClientTickEvents.END_WORLD_TICK.register { world ->
            mc.level?.let { TickEvent.End(world).postAndCatch() }
        }

        WorldRenderEvents.END_EXTRACTION.register { handler ->
            mc.level?.let { RenderEvent.Extract(handler, RenderBatchManager.renderConsumer).postAndCatch() }
        }

        WorldRenderEvents.END_MAIN.register { context ->
            mc.level?.let { RenderEvent.Last(context).postAndCatch() }
        }

        ClientReceiveMessageEvents.MODIFY_GAME.register { component, overlay ->
            if (!overlay) return@register component
            component.string.noControlCodes.let { OverlayPacketEvent(it, component).postAndCatch() }
            return@register component
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { text, overlay ->
            if (overlay) return@register true
            !(ChatManager.shouldCancelMessage(text) || text.string.noControlCodes.let { ChatPacketEvent(it, text).postAndCatch() })
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            EntityEvent.Remove(entity).postAndCatch()
        }

        onReceive<ClientboundTakeItemEntityPacket> {
            if (mc.player == null || !DungeonUtils.inClear) return@onReceive
            val itemEntity = mc.level?.getEntity(itemId) as? ItemEntity ?: return@onReceive
            if (itemEntity.item?.hoverName?.string?.containsOneOf(dungeonItemDrops, true) == true && itemEntity.distanceTo(mc.player ?: return@onReceive) <= 6)
                SecretPickupEvent.Item(itemEntity).postAndCatch()
        }

        on<EntityEvent.Remove> {
            if (mc.player == null || !DungeonUtils.inClear) return@on
            val entity = entity as? ItemEntity ?: return@on
            if (
                entity.item?.hoverName?.string?.containsOneOf(dungeonItemDrops, true) == true &&
                entity.distanceTo(mc.player ?: return@on) <= 6
            ) SecretPickupEvent.Item(entity).postAndCatch()
        }

        on<PlaySoundEvent> {
            if (!DungeonUtils.inClear) return@on
            if (sound.equalsOneOf(SoundEvents.BAT_HURT, SoundEvents.BAT_DEATH) && volume == 0.1f)
                SecretPickupEvent.Bat(pos).postAndCatch()
        }

        onSend<ServerboundUseItemOnPacket> {
            if (!DungeonUtils.inDungeons || hand == InteractionHand.OFF_HAND) return@onSend
            SecretPickupEvent.Interact(
                hitResult.blockPos,
                mc.level?.getBlockState(hitResult.blockPos)?.takeIf { isSecret(it, hitResult.blockPos) } ?: return@onSend
            ).postAndCatch()
        }
    }

    private val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft",
        "Secret Dye", "Candycomb"
    )
}