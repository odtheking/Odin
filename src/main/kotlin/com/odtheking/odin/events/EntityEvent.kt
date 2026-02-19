package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

abstract class EntityEvent(val entity: Entity) : CancellableEvent() {
    class Add(entity: Entity) : EntityEvent(entity)

    class Remove(entity: Entity) : EntityEvent(entity)

    class Move(entity: Entity, val newPos: Vec3, val yRot: Float, val xRot: Float, val onGround: Boolean) : EntityEvent(entity)

    class SetItemSlot(entity: Entity, val slot: EquipmentSlot, val stack: ItemStack) : EntityEvent(entity)

    class SetData(entity: Entity, val synchedDataValues: List<SynchedEntityData.DataValue<*>>) : EntityEvent(entity)

    class Event(entity: Entity, val id: Byte) : EntityEvent(entity)
}