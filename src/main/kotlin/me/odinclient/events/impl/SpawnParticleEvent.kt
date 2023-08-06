package me.odinclient.events.impl

import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.mixin.MixinNetHandlerPlayClient.redirectHandleParticles
 */
class SpawnParticleEvent(val enumParticleTypes: EnumParticleTypes, val longDistance: Boolean, val x: Double, val y: Double, val z: Double): Event()