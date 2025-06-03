package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

data class RenderShaderEvent(val partialTicks: Float) : Event()