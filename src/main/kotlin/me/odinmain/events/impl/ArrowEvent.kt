package me.odinmain.events.impl

import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityArrow
import net.minecraftforge.fml.common.eventhandler.Event

abstract class ArrowEvent(val arrow: EntityArrow) : Event() {
    class Despawn(arrow: EntityArrow, val owner: Entity, val entitiesHit: ArrayList<Entity>) : ArrowEvent(arrow)
    class Hit(arrow: EntityArrow, val target: Entity) : ArrowEvent(arrow)
}