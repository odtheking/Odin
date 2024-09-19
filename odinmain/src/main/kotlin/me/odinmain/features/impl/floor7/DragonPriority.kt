package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.dragonPriorityToggle
import me.odinmain.features.impl.floor7.WitherDragons.dragonTitle
import me.odinmain.features.impl.floor7.WitherDragons.easyPower
import me.odinmain.features.impl.floor7.WitherDragons.normalPower
import me.odinmain.features.impl.floor7.WitherDragons.paulBuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuffOnAll
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.*

object DragonPriority {

    fun findPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val priorityList = listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)
        val priorityDragon = if (!dragonPriorityToggle) {
            spawningDragon.sortBy { priorityList.indexOf(it) }
            spawningDragon[0]
        } else
            sortPriority(spawningDragon)
        return priorityDragon
    }

    fun dragonPrioritySpawn(dragon: WitherDragonsEnum) {
        if (dragonTitle && WitherDragons.enabled) PlayerUtils.alert("§${dragon.colorCode}${dragon.name} is spawning!")
        if (dragonPriorityToggle && WitherDragons.enabled && WitherDragonsEnum.entries.filter { it.state == WitherDragonState.SPAWNING }.toMutableList().size == 2) modMessage("§${dragon.colorCode}${dragon.name} §7is your priority dragon!")
    }

    private fun sortPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = Blessing.POWER.current * (if (paulBuff) 1.25 else 1.0) + (if (Blessing.TIME.current > 0) 2.5 else 0.0)

        val playerClass = DungeonUtils.currentDungeonPlayer.clazz.also { if (it == DungeonClass.Unknown) modMessage("§cFailed to get dungeon class.") }
        devMessage("§8Getting priority dragon for §${playerClass.colorCode}${playerClass.name}§8 with §4$totalPower§8 power.")
        val dragonList = listOf(WitherDragonsEnum.Orange, WitherDragonsEnum.Green, WitherDragonsEnum.Red, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple)
        val priorityList =
            if (totalPower >= normalPower || (spawningDragon.any { it == WitherDragonsEnum.Purple } && totalPower >= easyPower))
                if (playerClass.equalsOneOf(DungeonClass.Berserk, DungeonClass.Mage)) dragonList else dragonList.reversed()
            else listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)

        spawningDragon.sortBy { priorityList.indexOf(it) }

        if (totalPower >= easyPower) {
            if (soloDebuff) {
                if (playerClass == DungeonClass.Tank) {
                    if (spawningDragon.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll) spawningDragon.sortByDescending { priorityList.indexOf(it) }
                }
            } else if (playerClass == DungeonClass.Healer) {
                if (spawningDragon.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll) spawningDragon.sortByDescending { priorityList.indexOf(it) }
            }
        }

        return spawningDragon[0]
    }
}