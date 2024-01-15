package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.BlessingDisplay
import me.odinmain.features.impl.floor7.WitherDragons.configEasyPower
import me.odinmain.features.impl.floor7.WitherDragons.configPower
import me.odinmain.features.impl.floor7.WitherDragons.configSoloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.dragPrioSpawnToggle
import me.odinmain.features.impl.floor7.WitherDragons.paulBuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuffOnAll
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes


object DragonPriority {

    var firstDragonsSpawned = false

    fun dragPrioSpawn() {
        if (!dragPrioSpawnToggle || firstDragonsSpawned) return


        //modMessage("§7Dragons spawning: §d${spawningDragons.size}§7.")
        val spawningDragons = WitherDragonsEnum.entries.filter { it.spawning }.toMutableList()

        if (spawningDragons.size != 1) return

        val dragon = sortPrio(spawningDragons)

        PlayerUtils.alert("${dragon.colorCode} ${dragon.name}")
        firstDragonsSpawned = true
    }

    private fun sortPrio(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        var totalPower = BlessingDisplay.Blessings.POWER.current * if (paulBuff) 1.25 else 1.0
        if (BlessingDisplay.Blessings.TIME.current > 0) totalPower += 2.5
        val playerClass = DungeonUtils.teammates.find { it.name == mc.thePlayer.name }?.clazz ?: return spawningDragon[0]

        val prioList: List<Char> =
            if (totalPower >= configPower || (spawningDragon.any { it == WitherDragonsEnum.Purple } && totalPower >= configEasyPower)) {
                if (playerClass.equalsOneOf(Classes.Berserk, Classes.Mage)) "ogrbp".toList() else "pbrgo".toList()
            } else "robpg".toList()

        spawningDragon.sortByDescending { prioList.indexOf(it.firstLetter) }

        if (totalPower >= configEasyPower) {
            if (configSoloDebuff.toInt() == 1) {
                if ((playerClass == Classes.Tank && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll) {
                    spawningDragon.sortByDescending { prioList.indexOf(it.firstLetter) }
                }

            } else {
                if ((playerClass == Classes.Healer && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll) {
                    spawningDragon.sortByDescending { prioList.indexOf(it.firstLetter) }
                }
            }
        }

        return spawningDragon[0]
    }
}