package com.odtheking.odin.events

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.events.core.Event
import net.minecraft.client.resources.sounds.Sound
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource
import net.minecraft.world.BossEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class PlaySoundEvent(
    val sound: Sound,
    val source: SoundSource,
    val pos: Vec3,
    val volume: Float,
    val pitch: Float,
) : CancellableEvent()

class InputEvent(val key: InputConstants.Key) : CancellableEvent() // better mixin is prob ideal no need for cancellable

class BlockUpdateEvent(val pos: BlockPos, val old: BlockState, val updated: BlockState) : Event()

class BlockInteractEvent(val pos: BlockPos) : CancellableEvent()
class EntityInteractEvent(val pos: Vec3, val entity: Entity) : CancellableEvent()

class ChatPacketEvent(val value: String, val component: Component) : Event() // mixin instead of packet (still needs to run before vanilla processing for hideMessage()
class OverlayPacketEvent(val value: String, val component: Component) : Event()
class MessageSentEvent(val message: String) : CancellableEvent()

class RenderBossBarEvent(val bossBar: BossEvent) : CancellableEvent()

class ParticleAddEvent(val particle: ParticleType<*>, val overrideDelimiter: Boolean, val alwaysShow: Boolean, val pos: Vec3, val delta: Vec3) : CancellableEvent()
class GameTimeUpdateEvent(val gameTime: Long, val dayTime: Long, val newShouldTickDayTime: Boolean) : Event()
class TabListUpdateEvent(val header: Component?, val footer: Component?) : Event()
