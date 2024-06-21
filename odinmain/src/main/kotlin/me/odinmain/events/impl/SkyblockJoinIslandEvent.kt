package me.odinmain.events.impl

import me.odinmain.utils.skyblock.Island
import net.minecraftforge.fml.common.eventhandler.Event

class SkyblockJoinIslandEvent(val island: Island) : Event()