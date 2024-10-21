package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

class PostEntityStatus(val entityId: Int, val status: Byte) : Event()