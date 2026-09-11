package com.odtheking.odin.events

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.events.core.Event
import com.odtheking.odin.features.impl.dungeon.map.tile.DungeonRoom
import com.odtheking.odin.features.impl.dungeon.map.tile.MapCheckmark
import com.odtheking.odin.utils.render.RenderConsumer
import com.odtheking.odin.utils.skyblock.dungeon.Floor
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler
import net.fabricmc.fabric.api.client.rendering.v1.level.AbstractLevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.BossEvent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

class InputEvent(val key: InputConstants.Key) : CancellableEvent() // better mixin is prob ideal no need for cancellable

class BlockUpdateEvent(val pos: BlockPos, val old: BlockState, val updated: BlockState) : Event

class BlockInteractEvent(val pos: BlockPos) : CancellableEvent()
class EntityInteractEvent(val pos: Vec3, val entity: Entity) : CancellableEvent()
class UseItemOnPostEvent(val hand: InteractionHand, val hitResult: BlockHitResult, val interactionResult: InteractionResult) : Event

open class MessageEvent(val message: String, open val component: Component) : CancellableEvent() {
    class Chat(value: String, component: Component) : MessageEvent(value, component)
    class Overlay(value: String, component: Component) : MessageEvent(value, component)
    class ModifyChat(value: String, override var component: Component) : MessageEvent(value, component)
    class ModifyOverlay(value: String, override var component: Component) : MessageEvent(value, component)
}

class MessageSentEvent(val message: String) : CancellableEvent()
class RenderBossBarEvent(val bossBar: BossEvent) : CancellableEvent()

interface SecretPickupEvent : Event { // all are currently packet based but can probably use mixins
    class Interact(val blockPos: BlockPos, val blockState: BlockState) : SecretPickupEvent
    class Item(val entity: ItemEntity) : SecretPickupEvent
    class Bat(val packet: ClientboundSoundPacket) : SecretPickupEvent
}

abstract class TerminalEvent(val terminal: TerminalHandler) : Event {
    class Open(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Close(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Solve(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Click(terminal: TerminalHandler, val slotIndex: Int, val button: Int) : TerminalEvent(terminal)
}

interface TickEvent : Event {
    class End(val level: ClientLevel) : TickEvent
    object Server : TickEvent
}

interface LevelEvent : Event {
    object Load : LevelEvent
    object Unload : LevelEvent
}

abstract class RenderEvent(open val context: AbstractLevelRenderContext) : Event {
    class Extract(override val context: LevelRenderContext, val consumer: RenderConsumer) : RenderEvent(context)
    class Last(override val context: LevelRenderContext) : RenderEvent(context)
}

abstract class PartyEvent(val members: List<String>) : Event {
    class Leave(members: List<String>) : PartyEvent(members)
}

abstract class PacketEvent(val packet: Packet<*>) : CancellableEvent() { // ideally used less
    class Receive(packet: Packet<*>) : PacketEvent(packet)
    class Send(packet: Packet<*>) : PacketEvent(packet)
}

class RoomEnterEvent(val room: DungeonRoom?) : Event
class FloorEnterEvent(val floor: Floor) : Event
class ScoreUpdateEvent(val score: Int) : Event
data object MapUpdateEvent : Event
class CheckmarkUpdateEvent(val room: DungeonRoom, val checkmark: MapCheckmark) : Event
class SecretsUpdateEvent(val room: DungeonRoom, val foundSecrets: Int) : Event

object LocationChangeEvent : Event
object ScreenCloseEvent : Event
class SetSlotEvent(val slotIndex: Int, val itemStack: ItemStack, val slots: List<Slot>, val menu: AbstractContainerMenu) : Event

abstract class EntityEvent(val entity: Entity) : Event {
    class Add(entity: Entity) : EntityEvent(entity)
    class Remove(entity: Entity) : EntityEvent(entity)
    class Move(entity: Entity, val newPos: Vec3, val yRot: Float, val xRot: Float, val onGround: Boolean) : EntityEvent(entity)
    class SetItemSlot(entity: Entity, val slot: EquipmentSlot, val stack: ItemStack) : EntityEvent(entity)
    class SetData(entity: Entity, val synchedDataValues: List<SynchedEntityData.DataValue<*>>) : EntityEvent(entity)
    class Event(entity: Entity, val id: Byte) : EntityEvent(entity)
}