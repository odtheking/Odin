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
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.modMessage

object DragonPriority {

    fun dragonPrioritySpawn(dragon: WitherDragonsEnum) {
        if (dragonTitle) PlayerUtils.alert("§${dragon.colorCode}${dragon.name} is spawning!")
        if (dragonPriorityToggle && WitherDragonsEnum.entries.filter { it.spawning }.toMutableList().size == 2) modMessage("§${dragon.colorCode}${dragon.name} §7is your priority dragon!")
    }

    fun sortPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = BlessingDisplay.Blessings.POWER.current * if (paulBuff) 1.25 else 1.0 +
                if (BlessingDisplay.Blessings.TIME.current > 0) 2.5 else 0.0

        val playerClass = DungeonUtils.dungeonTeammates.find { it.name == mc.thePlayer.name }?.clazz
        if (playerClass == null) modMessage("Player class not found, Please report this! ${if (dragonPriorityToggle) "Defaulting to normal priority." else ""}")

        val dragonList = listOf(WitherDragonsEnum.Orange, WitherDragonsEnum.Green, WitherDragonsEnum.Red, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple)
        val priorityList =
            if (totalPower >= normalPower || (spawningDragon.any { it == WitherDragonsEnum.Purple } && totalPower >= easyPower) && dragonPriorityToggle && playerClass != null)
                if (playerClass.equalsOneOf(Classes.Berserk, Classes.Mage)) dragonList else dragonList.reversed()
            else listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)

        spawningDragon.sortBy { priorityList.indexOf(it) }

        if (totalPower >= easyPower && dragonPriorityToggle && playerClass != null) {
            if ((soloDebuff && playerClass == Classes.Tank && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll)
                spawningDragon.sortByDescending { priorityList.indexOf(it) }
            else if ((playerClass == Classes.Healer && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll)
                spawningDragon.sortByDescending { priorityList.indexOf(it) }
        }
        return spawningDragon[0]
    }
}