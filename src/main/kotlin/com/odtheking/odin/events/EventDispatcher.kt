package com.odtheking.odin.events

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.utils.ChatManager
import com.odtheking.odin.utils.containsOneOf
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.RenderBatchManager
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.isSecret
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.network.protocol.game.*
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.phys.Vec3

object EventDispatcher {

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> LevelEvent.Load.postAndCatch() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> LevelEvent.Unload.postAndCatch() }

        ClientTickEvents.START_LEVEL_TICK.register { world -> TickEvent.Start(world).postAndCatch() }
        ClientTickEvents.END_LEVEL_TICK.register { world -> TickEvent.End(world).postAndCatch() }

        LevelRenderEvents.END_EXTRACTION.register { handler -> RenderEvent.Extract(handler, RenderBatchManager.renderConsumer).postAndCatch() }
        LevelRenderEvents.END_MAIN.register { context -> RenderEvent.Last(context).postAndCatch() }

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ -> ScreenEvent.Open(screen).postAndCatch() }
        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            ScreenEvents.remove(screen).register {
                ScreenEvent.Close(screen).postAndCatch()
            }
            ScreenMouseEvents.allowMouseClick(screen).register { screen, event ->
                !ScreenEvent.MouseClick(screen, event).postAndCatch()
            }
            ScreenMouseEvents.allowMouseRelease(screen).register { screen, event ->
                !ScreenEvent.MouseRelease(screen, event).postAndCatch()
            }
            ScreenKeyboardEvents.allowKeyPress(screen).register { screen, event ->
                !ScreenEvent.KeyPress(screen, event).postAndCatch()
            }
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { text, overlay ->
            if (overlay) return@register true
            !ChatManager.shouldCancelMessage(text)
        }

        onReceive<ClientboundTakeItemEntityPacket> {
            if (mc.player == null || !DungeonUtils.inClear) return@onReceive
            val itemEntity = mc.level?.getEntity(itemId) as? ItemEntity ?: return@onReceive
            if (itemEntity.item.hoverName.string.containsOneOf(dungeonItemDrops, true) && itemEntity.distanceTo(mc.player ?: return@onReceive) <= 6)
                SecretPickupEvent.Item(itemEntity).postAndCatch()
        }

        onReceive<ClientboundRemoveEntitiesPacket> {
            if (mc.player == null || !DungeonUtils.inClear) return@onReceive
            entityIds.forEach { id ->
                val entity = mc.level?.getEntity(id) as? ItemEntity ?: return@forEach
                if (entity.item.hoverName.string.containsOneOf(dungeonItemDrops, true) && entity.distanceTo(mc.player ?: return@onReceive) <= 6)
                    SecretPickupEvent.Item(entity).postAndCatch()
            }
        }

        onReceive<ClientboundSoundPacket> {
            if (!DungeonUtils.inClear) return@onReceive
            if (sound.value().equalsOneOf(SoundEvents.BAT_HURT, SoundEvents.BAT_DEATH) && volume == 0.1f)
                SecretPickupEvent.Bat(this).postAndCatch()
        }

        onSend<ServerboundUseItemOnPacket> {
            if (!DungeonUtils.inDungeons || hand == InteractionHand.OFF_HAND) return@onSend
            val blockState = mc.level?.getBlockState(hitResult.blockPos) ?: return@onSend
            if (blockState.block is SkullBlock) {
                val distance = mc.player?.eyePosition?.distanceToSqr(Vec3(hitResult.blockPos)) ?: return@onSend
                if (distance > 20.25) return@onSend
            }

            if (isSecret(blockState, hitResult.blockPos)) SecretPickupEvent.Interact(hitResult.blockPos, blockState).postAndCatch()
        }

        onReceive<ClientboundSystemChatPacket> {
            if (!overlay) ChatPacketEvent(content.string.noControlCodes, content).postAndCatch()
        }
    }

    private val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft",
        "Secret Dye", "Candycomb"
    )
}