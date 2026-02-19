package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.events.core.Event
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class RoomEnterEvent(val room: Room?) : CancellableEvent()

abstract class SecretPickupEvent : Event() { // all are currently packet based but can probably use mixins
    class Interact(val blockPos: BlockPos, val blockState: BlockState) : SecretPickupEvent()
    class Item(val entity: ItemEntity) : SecretPickupEvent()
    class Bat(val position: Vec3) : SecretPickupEvent()
}

abstract class TerminalEvent(val terminal: TerminalHandler) : Event() { // first 2 are packet based can use mixins
    class Open(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Close(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Solve(terminal: TerminalHandler) : TerminalEvent(terminal)
}

abstract class PartyEvent(val members: List<String>) : Event() {
    class Leave(members: List<String>) : PartyEvent(members)
}
