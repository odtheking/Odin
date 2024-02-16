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
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraUtils {
    var kuudraTeammates = ArrayList<kuudraPlayer>()
    var giantZombies: MutableList<EntityGiantZombie> = mutableListOf()
    var supplies = BooleanArray(6) { true }
    var kuudraEntity: EntityMagmaCube = EntityMagmaCube(mc.theWorld)
    var builders = 0
    var build = 0
    var phase = 0
    var preSpot = ""
    var missing = ""

    data class kuudraPlayer(val playerName: String, var eatFresh: Boolean = false, var eatFreshTime: Long = 0, val entity: EntityPlayer? = null)
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
            if (FreshTimer.notifyOtherFresh) modMessage("$playerName has fresh tools")
            kuudraTeammates.forEach { kuudraPlayer ->
                if (kuudraPlayer.playerName !== playerName) return@forEach
                kuudraPlayer.eatFresh = true
                runIn(200) {
                    if (FreshTimer.notifyOtherFresh) modMessage("${kuudraPlayer.playerName} Fresh tools has expired")
                    kuudraPlayer.eatFresh = false
                }
            }
        }

        when (message)
        {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> {
                phase = 1

                kuudraTeammates = mc.theWorld.getLoadedEntityList().filter { it is EntityOtherPlayerMP && it.getPing() == 1 && !it.isInvisible }
                    .map { KuudraUtils.kuudraPlayer((it as EntityOtherPlayerMP).name, false, 0, it) } as ArrayList<KuudraUtils.kuudraPlayer>
                kuudraTeammates.add(KuudraUtils.kuudraPlayer(mc.thePlayer.name, false, 0, mc.thePlayer))
            }

            "[NPC] Elle: OMG! Great work collecting my supplies!" -> phase = 2

            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> phase = 3

            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> phase = 4
        }
    }


    init {
        Executor(500) {
            if (phase == 1) {
                val entities = mc.theWorld.loadedEntityList
                giantZombies = entities.filter { it is EntityGiantZombie && it.heldItem.toString() == "1xitem.skull@3" } as MutableList<EntityGiantZombie>
            }
        }.register()
    }
    @SubscribeEvent
    fun postMetaData(event: PostEntityMetadata) {
        val entity = mc.theWorld.getEntityByID(event.packet.entityId) ?: return
        if (entity.name.contains("Lv") || entity.toString().contains("name=Armor Stand")) return
        val name = entity.name.noControlCodes
        if (name.contains("Building Progress")) {
            builders = name.substring(name.indexOf("(") + 1, name.indexOf("(") +2).toIntOrNull() ?: 0
            val regex = Regex("\\D")
            build = name.substring(0, name.indexOf("%")).replace(regex, "").toIntOrNull() ?: 0
        }
    }

    @SubscribeEvent
    fun worldJoinEvent(event: EntityJoinWorldEvent) {
        if (event.entity is EntityMagmaCube && (event.entity as EntityMagmaCube).slimeSize == 30 && (event.entity as EntityMagmaCube).health <= 100000)
            kuudraEntity = event.entity as EntityMagmaCube

        if (event.entity is EntityOtherPlayerMP && event.entity.getPing() == 1 && !event.entity.isInvisible)
            kuudraTeammates.add(KuudraUtils.kuudraPlayer((event.entity as EntityOtherPlayerMP).name, false, 0, event.entity as EntityOtherPlayerMP))
    }
}