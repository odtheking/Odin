package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.dragonPriorityToggle
import me.odinmain.features.impl.floor7.WitherDragons.dragonTitle
import me.odinmain.features.impl.floor7.WitherDragons.easyPower
import me.odinmain.features.impl.floor7.WitherDragons.normalPower
import me.odinmain.features.impl.floor7.WitherDragons.paulBuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuffOnAll
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.dungeon.*
import me.odinmain.utils.skyblock.modMessage

object DragonPriority {

    fun findPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val priorityList = listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)
        return if (!dragonPriorityToggle) {
            spawningDragon.sortBy { priorityList.indexOf(it) }
            spawningDragon[0]
        } else
            sortPriority(spawningDragon)
    }

    fun displaySpawningDragon(dragon: WitherDragonsEnum) {
        if (dragon == WitherDragonsEnum.None) return
        if (dragonTitle && WitherDragons.enabled) PlayerUtils.alert("§${dragon.colorCode}${dragon.name} is spawning!", 30)
        if (dragonPriorityToggle && WitherDragons.enabled) modMessage("§${dragon.colorCode}${dragon.name} §7is your priority dragon!")
    }

    private fun sortPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = Blessing.POWER.current * (if (paulBuff) 1.25 else 1.0) + (if (Blessing.TIME.current > 0) 2.5 else 0.0)
        val playerClass = DungeonUtils.currentDungeonPlayer.clazz.apply { if (this == DungeonClass.Unknown) modMessage("§cFailed to get dungeon class.") }

        val dragonList = listOf(WitherDragonsEnum.Orange, WitherDragonsEnum.Green, WitherDragonsEnum.Red, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple)
        val priorityList =
            if (totalPower >= normalPower || (spawningDragon.any { it == WitherDragonsEnum.Purple } && totalPower >= easyPower))
                if (playerClass.equalsOneOf(DungeonClass.Berserk, DungeonClass.Mage)) dragonList else dragonList.reversed()
            else listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)

        spawningDragon.sortBy { priorityList.indexOf(it) }

        if (totalPower >= easyPower) {
            if (soloDebuff == 1 && playerClass == DungeonClass.Tank && (spawningDragon.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll)) spawningDragon.sortByDescending { priorityList.indexOf(it) }
            else if (playerClass == DungeonClass.Healer && spawningDragon.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll) spawningDragon.sortByDescending { priorityList.indexOf(it) }
        }
        devMessage("§7Priority: §6$totalPower §7Class: §${playerClass.colorCode}${playerClass.name} §7Dragons: §a${spawningDragon.joinToString(", ") { it.name }} §7-> §c${priorityList.joinToString(", ") { it.name.first().toString() }}")
        return spawningDragon[0]
    }
}