package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.BlessingDisplay
import me.odinmain.features.impl.floor7.WitherDragons.configEasyPower
import me.odinmain.features.impl.floor7.WitherDragons.configPower
import me.odinmain.features.impl.floor7.WitherDragons.configSoloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.paulBuff
import me.odinmain.features.impl.floor7.WitherDragons.soloDebuffOnAll
import me.odinmain.utils.addVec
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.fastEyeHeight
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.client.event.RenderWorldLastEvent


object DragonPriority {

    private var dragonTracer = ""
    var firstOnce = false
    fun dragonPrioritySpawn() {
        val spawningDragons = WitherDragonsEnum.entries.filter { it.spawning }.toMutableList()

        if (spawningDragons.size != 2 || firstOnce) return
        firstOnce = true

        val dragon = sortPriority(spawningDragons)

        PlayerUtils.alert("§${dragon.colorCode} ${dragon.name}")
    }

    private fun sortPriority(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = BlessingDisplay.Blessings.POWER.current * if (paulBuff) 1.25 else 1.0 +
                if (BlessingDisplay.Blessings.TIME.current > 0) 2.5 else 0.0

        val playerClass = DungeonUtils.teammates.find { it.name == mc.thePlayer.name }?.clazz
            ?: return modMessage("§cPlayer Class wasn't found! please report this").let { WitherDragonsEnum.Purple }

        val dragonList = listOf(WitherDragonsEnum.Orange, WitherDragonsEnum.Green, WitherDragonsEnum.Red, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple)
        val priorityList =
            if (totalPower >= configPower || (spawningDragon.any { it == WitherDragonsEnum.Purple } && totalPower >= configEasyPower))
                if (playerClass.equalsOneOf(Classes.Berserk, Classes.Mage)) dragonList else dragonList.reversed()
            else listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)

        spawningDragon.sortBy { priorityList.indexOf(it) }

        if (totalPower >= configEasyPower) {
            if ((configSoloDebuff && playerClass == Classes.Tank && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll)
                spawningDragon.sortByDescending { priorityList.indexOf(it) }
            else if ((playerClass == Classes.Healer && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll)
                spawningDragon.sortByDescending { priorityList.indexOf(it) }
        }
        return spawningDragon[0]
    }

    fun renderTracerPriority(event: RenderWorldLastEvent, tracerWidth: Int) {

        val dragon = sortPriority(WitherDragonsEnum.entries.filter { it.spawning }.toMutableList())
        if (dragon.entity == null) return
        RenderUtils.draw3DLine(
            mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), dragon.entity!!.renderVec.addVec(.5, .5, .5),
            dragon.color,
            tracerWidth, depth = true
        )
    }
}