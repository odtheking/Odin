package me.odinmain.events.impl

import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityArrow
import net.minecraftforge.fml.common.eventhandler.Event

class ArrowDespawnEvent(val arrow: EntityArrow, val owner: Entity, val entitiesHit: ArrayList<Entity>) : Event()