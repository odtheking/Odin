package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.impl.nether.NoPre
import me.odinmain.utils.*
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraUtils {
    var kuudraTeammates: List<KuudraPlayer> = emptyList()
    var kuudraTeammatesNoSelf: List<KuudraPlayer> = emptyList()
    var giantZombies: List<EntityGiantZombie> = mutableListOf()
    var supplies = BooleanArray(6) { true }
    var kuudraEntity: EntityMagmaCube = EntityMagmaCube(mc.theWorld)
    var builders = 0
    var build = 0
    var phase = 0
    var buildingPiles = listOf<EntityArmorStand>()

    inline val inKuudra get() = LocationUtils.currentArea.isArea(Island.Kuudra)

    data class KuudraPlayer(val playerName: String, var eatFresh: Boolean = false, var eatFreshTime: Long = 0, var entity: EntityPlayer? = null)

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        phase = 0
        kuudraTeammates = mutableListOf()
        kuudraTeammatesNoSelf = mutableListOf()
        supplies = BooleanArray(6) { true }
        giantZombies = mutableListOf()
        kuudraEntity = EntityMagmaCube(mc.theWorld)
        NoPre.missing = ""
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val message = event.message

        if (message.matches(Regex("^Party > ?(?:\\[\\S+])? (\\S{1,16}): FRESH"))) {
            val playerName = Regex("^Party > ?(?:\\[\\S+])? (\\S{1,16}): FRESH").find(message)?.groupValues?.get(1)?.takeIf { it == mc.thePlayer?.name } ?: return

            kuudraTeammates.find { it.playerName == playerName }?.let { kuudraPlayer ->
                kuudraPlayer.eatFresh = true
                runIn(200) {
                    kuudraPlayer.eatFresh = false
                }
            }
        }

        when (message) {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> phase = 1

            "[NPC] Elle: OMG! Great work collecting my supplies!" -> phase = 2

            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> phase = 3

            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> phase = 4
        }
    }


    init {
        Executor(500) {
            if (!LocationUtils.currentArea.isArea(Island.Kuudra)) return@Executor
            val entities = mc.theWorld?.loadedEntityList ?: return@Executor
            giantZombies = entities.filterIsInstance<EntityGiantZombie>().filter{ it.heldItem.unformattedName == "Head" }.toMutableList()

            kuudraEntity = entities.filterIsInstance<EntityMagmaCube>().filter { it.slimeSize == 30 && it.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toFloat() == 100000f }[0]

            entities.filterIsInstance<EntityArmorStand>().forEach {
                if (phase == 2) {
                    val message = Regex("Building Progress (\\d+)% \\((\\d+) Players Helping\\)").find(it.name.noControlCodes)
                    if (message != null) {
                        build = message.groupValues[1].toIntOrNull() ?: 0
                        builders = message.groupValues[2].toIntOrNull() ?: 0
                    }
                }

                if (phase != 1 || it.name.contains("SUPPLIES RECEIVED")) return@forEach
                val x = it.posX.toInt()
                val z = it.posZ.toInt()
                if (x == -98 && z == -112) supplies[0] = false
                if (x == -98 && z == -99) supplies[1] = false
                if (x == -110 && z == -106) supplies[2] = false
                if (x == -106 && z == -112) supplies[3] = false
                if (x == -94 && z == -106) supplies[4] = false
                if (x == -106 && z == -99) supplies[5] = false
            }

            buildingPiles = entities.filterIsInstance<EntityArmorStand>().filter { it.name.noControlCodes.matches(Regex("PROGRESS: (\\d+)%")) }.map { it }

            kuudraTeammates = updateKuudraTeammates(kuudraTeammates)
            kuudraTeammatesNoSelf = kuudraTeammates.filter { it.playerName != mc.thePlayer?.name }
        }.register()
    }

    private fun updateKuudraTeammates(previousTeammates: List<KuudraPlayer>): List<KuudraPlayer> {
        val teammates = mutableListOf<KuudraPlayer>()
        val tabList = getTabList

        tabList.forEach { entry ->
            val text = entry.first.displayName?.formattedText?.noControlCodes ?: return@forEach
            val (_, _, name) = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+)").find(text)?.groupValues ?: return@forEach
            val previousTeammate = previousTeammates.find { it.playerName == name }
            val entity = mc.theWorld?.getPlayerEntityByName(name)
            teammates.add(KuudraPlayer(name, previousTeammate?.eatFresh ?: false, previousTeammate?.eatFreshTime ?: 0, entity))
        }
        return teammates
    }
}