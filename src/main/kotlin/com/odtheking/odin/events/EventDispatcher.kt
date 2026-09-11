package com.odtheking.odin.events

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
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
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.phys.Vec3

object EventDispatcher {

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> LevelEvent.Load.postAndCatch() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> LevelEvent.Unload.postAndCatch() }

        ClientTickEvents.END_LEVEL_TICK.register { world -> TickEvent.End(world).postAndCatch() }

        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register {
            context -> RenderEvent.Extract(context, RenderBatchManager.renderConsumer).postAndCatch()
            RenderEvent.Last(context).postAndCatch()
        }

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
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

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ -> EntityEvent.Add(entity).postAndCatch() }
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ -> EntityEvent.Remove(entity).postAndCatch() }

        ClientReceiveMessageEvents.MODIFY_GAME.register { message, overlay ->
            if (overlay) MessageEvent.ModifyOverlay(message.string.noControlCodes, message).apply { postAndCatch() }.component
            else MessageEvent.ModifyChat(message.string.noControlCodes, message).apply { postAndCatch() }.component
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (overlay) !MessageEvent.Overlay(message.string.noControlCodes, message).postAndCatch()
            else !MessageEvent.Chat(message.string.noControlCodes, message).postAndCatch()
        }

        onReceive<ClientboundTakeItemEntityPacket> {
            if (!DungeonUtils.inClear) return@onReceive
            val itemEntity = mc.level?.getEntity(itemId) as? ItemEntity ?: return@onReceive
            if (itemEntity.item.hoverName.string.containsOneOf(dungeonItemDrops, true) && itemEntity.distanceTo(mc.player ?: return@onReceive) <= 6)
                SecretPickupEvent.Item(itemEntity).postAndCatch()
        }

        on<EntityEvent.Remove> {
            if (!DungeonUtils.inClear) return@on
            val entity = entity as? ItemEntity ?: return@on
            if (
                entity.item.hoverName.string.containsOneOf(dungeonItemDrops, true) &&
                entity.distanceTo(mc.player ?: return@on) <= 6
            ) SecretPickupEvent.Item(entity).postAndCatch()
        }

        onReceive<ClientboundSoundPacket> {
            if (!DungeonUtils.inClear) return@onReceive
            if (sound.value().equalsOneOf(SoundEvents.BAT_HURT, SoundEvents.BAT_DEATH) && volume == 0.1f)
                SecretPickupEvent.Bat(this).postAndCatch()
        }

        on<BlockInteractEvent> {
            if (!DungeonUtils.inDungeons) return@on
            val blockState = mc.level?.getBlockState(pos) ?: return@on
            if (blockState.block is SkullBlock) {
                val distance = mc.player?.eyePosition?.distanceToSqr(Vec3(pos)) ?: return@on
                if (distance > 20.25) return@on
            }

            if (isSecret(blockState, pos)) SecretPickupEvent.Interact(pos, blockState).postAndCatch()
        }
    }

    private val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft",
        "Secret Dye", "Candycomb"
    )
}