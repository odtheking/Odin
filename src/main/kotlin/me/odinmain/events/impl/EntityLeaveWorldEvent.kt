package me.odinmain.events.impl

import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.Event

class EntityLeaveWorldEvent(val entity: Entity) : Event()