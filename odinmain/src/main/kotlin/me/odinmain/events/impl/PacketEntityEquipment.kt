package me.odinmain.events.impl

import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraftforge.fml.common.eventhandler.Event
class PacketEntityEquipment(val packet: S04PacketEntityEquipment) : Event()