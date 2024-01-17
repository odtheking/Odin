package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.BlessingDisplay
import me.odinmain.features.impl.floor7.WitherDragons.configEasyPower
import me.odinmain.features.impl.floor7.WitherDragons.configPower
import me.odinmain.features.impl.floor7.WitherDragons.configSoloDebuff
import me.odinmain.features.impl.floor7.WitherDragons.dragPrioSpawnToggle
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

    var firstDragonsSpawned = false
    private var dragonTracer = ""

    fun dragPrioSpawn() {
        if (!dragPrioSpawnToggle || firstDragonsSpawned) return

        val spawningDragons = WitherDragonsEnum.entries.filter { it.spawning }.toMutableList()

        if (spawningDragons.size != 1) return

        val dragon = sortPrio(spawningDragons)

        PlayerUtils.alert("§${dragon.colorCode} ${dragon.name}")
        dragonTracer = dragon.name
        firstDragonsSpawned = true
    }

    private fun sortPrio(spawningDragon: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = BlessingDisplay.Blessings.POWER.current * if (paulBuff) 1.25 else 1.0 +
                if (BlessingDisplay.Blessings.TIME.current > 0) 2.5 else 0.0

        val playerClass = DungeonUtils.teammates.find { it.name == mc.thePlayer.name }?.clazz
            ?: return modMessage("§cPlayer Class wasn't found!").let { WitherDragonsEnum.Purple }

        val orange = WitherDragonsEnum.Orange
        val blue = WitherDragonsEnum.Blue
        val purple = WitherDragonsEnum.Purple
        val green = WitherDragonsEnum.Green
        val red = WitherDragonsEnum.Red
        val dragonList = listOf(orange, green, red, blue, purple)
        val prioList =
            if (totalPower >= configPower || (spawningDragon.any { it == WitherDragonsEnum.Purple } && totalPower >= configEasyPower)) {
                if (playerClass.equalsOneOf(Classes.Berserk, Classes.Mage)) dragonList else dragonList.reversed()
            } else listOf(red, orange, blue, purple, green)

        spawningDragon.sortBy { prioList.indexOf(it) }

        if (totalPower >= configEasyPower) {
            if (configSoloDebuff) {
                if ((playerClass == Classes.Tank && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll) {
                    spawningDragon.sortByDescending { prioList.indexOf(it) }
                }

            } else {
                if ((playerClass == Classes.Healer && spawningDragon.any { it == WitherDragonsEnum.Purple }) || soloDebuffOnAll) {
                    spawningDragon.sortByDescending { prioList.indexOf(it) }
                }
            }
        }

        return spawningDragon[0]
    }


    fun renderTracerPrio(event: RenderWorldLastEvent, tracerWidth: Int) {

        val dragon = WitherDragonsEnum.entries.find { it.name == dragonTracer } ?: return

        RenderUtils.draw3DLine(
            mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), dragon.spawnPos.addVec(.5, .5, .5),
            dragon.color,
            tracerWidth, depth = true, event.partialTicks
        )
    }



}