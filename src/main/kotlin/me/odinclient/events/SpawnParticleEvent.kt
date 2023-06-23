package me.odinclient.events

import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.Event

class SpawnParticleEvent(val enumParticleTypes: EnumParticleTypes, val longDistance: Boolean, val x: Double, val y: Double, val z: Double): Event()