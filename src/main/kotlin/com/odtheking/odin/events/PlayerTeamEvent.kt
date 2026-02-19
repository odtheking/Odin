package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.world.scores.PlayerTeam

abstract class PlayerTeamEvent : Event() {
    class AddTeam(val teamName: String, val team: PlayerTeam) : PlayerTeamEvent()

    class RemoveTeam(val team: PlayerTeam) : PlayerTeamEvent()

    class UpdateParameters(val team: PlayerTeam) : PlayerTeamEvent()
}