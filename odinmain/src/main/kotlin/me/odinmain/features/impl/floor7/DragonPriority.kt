package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.BlessingDisplay
import me.odinmain.features.impl.floor7.DragonTimer
import me.odinmain.features.impl.floor7.WitherDragons.easyPower
import me.odinmain.features.impl.floor7.WitherDragons.normalPower
import me.odinmain.features.impl.floor7.WitherDragons.paulBuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuffOnAll
import me.odinmain.utils.addVec
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.fastEyeHeight
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toAABB


object DragonPriority {

    var firstDragons = false
    fun dragonPrioritySpawn() {
        val spawningDragons = WitherDragonsEnum.entries.filter { it.spawning }.toMutableList()

        if (spawningDragons.size != 2 || firstDragons) return
        firstDragons = true

        val dragon = sortPriority(spawningDragons)

        PlayerUtils.alert("§${dragon.colorCode} ${dragon.name}")
    }

    /** fun tracerDragonPriority() {
        val spawningDragons = WitherDragonsEnum.entries.filter { it.spawning }.toMutableList()
        if (spawningDragons.isEmpty()) return

        val dragon = sortPriority(spawningDragons)

        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (dragon.spawning && dragon.spawnTime() > 0)
                Renderer.draw3DLine(mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), dragon.spawnPos.addVec(0.5, 3.5, 0.5), dragon.color)
        }
    } */

    fun sortPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
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
        return spawningDragon[0]
    }
}