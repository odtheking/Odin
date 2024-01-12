package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.BlessingDisplay
import me.odinmain.features.impl.floor7.WitherDragons.configEasyPower
import me.odinmain.features.impl.floor7.WitherDragons.configPower
import me.odinmain.features.impl.floor7.WitherDragons.configSoloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.dragPrioSpawnToggle
import me.odinmain.features.impl.floor7.WitherDragons.paulBuff
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes


object DragonPriority {

    var firstDragonsSpawned = false
    private val spawningDragons = ArrayList<WitherDragonsEnum>()

    fun dragPrioSpawn() {
        if (!dragPrioSpawnToggle || firstDragonsSpawned) return

        WitherDragonsEnum.entries.forEach { dragon ->
            if (dragon.spawnTime() > 0) spawningDragons.add(dragon)
        }
        //modMessage("§7Dragons spawning: §d${spawningDragons.size}§7.")
        //modMessage(WitherDragonsEnum.entries.filter { it.spawnTime() > 0 }.size)
        if (spawningDragons.size != 1) return

        PlayerUtils.alert(sortPrio(spawningDragons.map { it.name }.toMutableList()), true)
        firstDragonsSpawned = true
        spawningDragons.clear()
    }

    private fun sortPrio(spawningDragons: MutableList<String>): String {
        var totalPower = BlessingDisplay.Blessings.POWER.current * if (paulBuff) 1.25 else 1.0
        if (BlessingDisplay.Blessings.TIME.current > 0) totalPower += 2.5
        val playerClass = DungeonUtils.teammates.find { it.name == mc.thePlayer.name }?.clazz ?: return "Failed to find player's class!"

        val dragonTexts = WitherDragonsEnum.entries.associateBy({ it.name }, { it.textPos })

        val prioList = if (totalPower >= configPower || (spawningDragons.contains("p") && totalPower >= configEasyPower)) {
            if (playerClass.equalsOneOf(Classes.Berserk, Classes.Mage)) {
                ArrayList(dragonTexts.keys)
            } else {
                ArrayList(dragonTexts.keys.reversed())
            }
        } else {
            "robpg".split("").toMutableList()
        }

        spawningDragons.sortByDescending { prioList.indexOf(it) }

        if (totalPower >= configEasyPower) {
            if (configSoloDebuff.toInt() == 1) {
                if (playerClass == Classes.Tank && spawningDragons.contains("p")) {
                    spawningDragons.sortWith(compareByDescending { prioList.indexOf(it) })
                }

            } else {
                if (playerClass == Classes.Healer && spawningDragons.contains("p")) {
                    spawningDragons.sortWith(compareByDescending { prioList.indexOf(it) })
                }
            }
        }

        return spawningDragons.firstOrNull() ?: ""
    }
}