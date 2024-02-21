package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.impl.kuudra.FreshTimer
import me.odinmain.utils.ServerUtils.getPing
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.runIn
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraUtils {
    var kuudraTeammates = ArrayList<KuudraPlayer>()
    var giantZombies: MutableList<EntityGiantZombie> = mutableListOf()
    var supplies = BooleanArray(6) { true }
    var kuudraEntity: EntityMagmaCube = EntityMagmaCube(mc.theWorld)
    var builders = 0
    var build = 0
    var phase = 0
    var buildingPiles = mutableListOf<Vec3>()

    data class KuudraPlayer(val playerName: String, var eatFresh: Boolean = false, var eatFreshTime: Long = 0, val entity: EntityPlayer? = null)
    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        phase = 0
        kuudraTeammates = ArrayList()
        supplies = BooleanArray(6) { true }
        giantZombies = mutableListOf()
        kuudraEntity = EntityMagmaCube(mc.theWorld)
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val message = event.message

        if (message.matches("^Party > ?(?:\\[.+])? (.{0,16}): FRESH".toRegex())) {
            val (playerName) = Regex("^Party > ?(?:\\[.+])? (.{0,16}): FRESH").find(message)?.destructured ?: return
            if (playerName == mc.thePlayer.name) return
            kuudraTeammates.forEach { kuudraPlayer ->
                if (kuudraPlayer.playerName != playerName) return@forEach
                kuudraPlayer.eatFresh = true
                runIn(200) {
                    kuudraPlayer.eatFresh = false
                }
            }
        }

        when (message) {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> {
                kuudraTeammates.add(KuudraPlayer(mc.thePlayer.name, false, 0, mc.thePlayer))
                phase = 1
            }

            "[NPC] Elle: OMG! Great work collecting my supplies!" -> phase = 2

            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> phase = 3

            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> phase = 4
        }
    }


    init {
        Executor(1000) {
            val entities = mc.theWorld.loadedEntityList
            giantZombies = entities.filter { it is EntityGiantZombie && it.heldItem.toString() == "1xitem.skull@3" } as MutableList<EntityGiantZombie>
            kuudraEntity = entities.filter { it is EntityMagmaCube && it.slimeSize == 30 && it.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toFloat() == 100000f }[0] as EntityMagmaCube
            entities.forEach {
                if (it.name.contains("Lv") || it.toString().contains("name=Armor Stand")) return@forEach
                val name = it.name.noControlCodes
                if (name.contains("Building Progress")) {
                    builders = name.substring(name.indexOf("(") + 1, name.indexOf("(") +2).toIntOrNull() ?: 0
                    val regex = Regex("\\D")
                    build = name.substring(0, name.indexOf("%")).replace(regex, "").toIntOrNull() ?: 0
                }
            }
            entities.forEach {
                if (it !is EntityArmorStand || phase != 1 || it.name.contains("Lv") || it.toString().contains("name=Armor Stand")) return@forEach
                if (!it.name.contains("SUPPLIES RECEIVED")) return@forEach
                val x = it.posX.toInt()
                val z = it.posZ.toInt()
                if (x == -98 && z == -112) supplies[0] = false
                if (x == -98 && z == -99) supplies[1] = false
                if (x == -110 && z == -106) supplies[2] = false
                if (x == -106 && z == -112) supplies[3] = false
                if (x == -94 && z == -106) supplies[4] = false
                if (x == -106 && z == -99) supplies[5] = false
            }

            buildingPiles = mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.name.contains("PUNCH") }.map { it.positionVector } as MutableList<Vec3>

        }.register()
    }

    @SubscribeEvent
    fun worldJoinEvent(event: EntityJoinWorldEvent) {
        if (event.entity is EntityOtherPlayerMP && event.entity.getPing() == 1 && !event.entity.isInvisible)
            kuudraTeammates.add(KuudraPlayer((event.entity as EntityOtherPlayerMP).name, false, 0, event.entity as EntityOtherPlayerMP))
    }
}