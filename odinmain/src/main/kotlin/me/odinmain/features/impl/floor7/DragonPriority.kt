package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.BlessingDisplay
import me.odinmain.features.impl.floor7.WitherDragons.dragonPriorityToggle
import me.odinmain.features.impl.floor7.WitherDragons.dragonTitle
import me.odinmain.features.impl.floor7.WitherDragons.easyPower
import me.odinmain.features.impl.floor7.WitherDragons.normalPower
import me.odinmain.features.impl.floor7.WitherDragons.paulBuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuffOnAll
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes

object DragonPriority {

    fun findPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val priorityList = listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)
        val priorityDragon = if (!dragonPriorityToggle) {
            spawningDragon.sortBy { priorityList.indexOf(it) }
            spawningDragon[0]
        } else {
            sortPriority(spawningDragon)
        }
        return priorityDragon
    }

    fun dragonPrioritySpawn(dragon: WitherDragonsEnum) {
        if (dragonTitle) PlayerUtils.alert("§${dragon.colorCode}${dragon.name} is spawning!")
        if (dragonPriorityToggle && WitherDragonsEnum.entries.filter { it.spawning }.toMutableList().size == 2) modMessage("§${dragon.colorCode}${dragon.name} §7is your priority dragon!")
    }

    private fun sortPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = BlessingDisplay.Blessings.POWER.current * if (paulBuff) 1.25 else 1.0 +
                if (BlessingDisplay.Blessings.TIME.current > 0) 2.5 else 0.0

        val playerClass = DungeonUtils.dungeonTeammates.find { it.name == mc.thePlayer.name }?.clazz
            ?: return modMessage("§cPlayer Class wasn't found! please report this").let { WitherDragonsEnum.Purple }

        val dragonList = listOf(WitherDragonsEnum.Orange, WitherDragonsEnum.Green, WitherDragonsEnum.Red, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple)
        val priorityList =
            if (totalPower >= normalPower || (spawningDragon.any { it == WitherDragonsEnum.Purple } && totalPower >= easyPower))
                if (playerClass.equalsOneOf(Classes.Berserk, Classes.Mage)) dragonList else dragonList.reversed()
            else listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)

        spawningDragon.sortBy { priorityList.indexOf(it) }

        if (totalPower >= easyPower) {
            if ((soloDebuff && playerClass == Classes.Tank && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll)
                spawningDragon.sortByDescending { priorityList.indexOf(it) }
            else if ((playerClass == Classes.Healer && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll)
                spawningDragon.sortByDescending { priorityList.indexOf(it) }
        }
        devMessage("§7 power: $totalPower")
        devMessage("§7 class: $playerClass")
        devMessage("§7 priority: ${spawningDragon?.get(0)?.name}, ${spawningDragon?.get(1)?.name}")
        devMessage("§7 priorityList: ${priorityList.joinToString(", ") { it.name }}")
        devMessage("is total power >= normal power? ${totalPower >= normalPower}")
        devMessage("is total power >= easy power? ${totalPower >= easyPower}")
        return spawningDragon[0]
    }
}