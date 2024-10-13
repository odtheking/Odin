package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.impl.nether.NoPre
import me.odinmain.utils.*
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraUtils {


    // minor thing - make array or arraylist atleast
    var kuudraTeammates: List<KuudraPlayer> = emptyList()

    // pointless no?
    var kuudraTeammatesNoSelf: List<KuudraPlayer> = emptyList()

    // arraylist
    var giantZombies: ArrayList<EntityGiantZombie> = arrayListOf()

    var supplies = BooleanArray(6) { true }

    // nullable? more sense imo
    var kuudraEntity: EntityMagmaCube = EntityMagmaCube(mc.theWorld)

    //build helper?
    var builders = 0
    var build = 0
    private val buildRegex = Regex("Building Progress (\\d+)% \\((\\d+) Players Helping\\)")

    var phase = 0

    //build helper
    var buildingPiles = arrayListOf<EntityArmorStand>()
    private val progressRegex = Regex("PROGRESS: (\\d+)%")

    inline val inKuudra get() = LocationUtils.currentArea.isArea(Island.Kuudra)

    data class KuudraPlayer(val playerName: String, var eatFresh: Boolean = false, var eatFreshTime: Long = 0, var entity: EntityPlayer? = null)

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        kuudraTeammates = emptyList()
        kuudraTeammatesNoSelf = emptyList()

        giantZombies = arrayListOf()
        supplies = BooleanArray(6) { true }
        kuudraEntity = EntityMagmaCube(mc.theWorld)
        builders = 0
        build = 0
        phase = 0
        buildingPiles = arrayListOf()
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
    }


    init {
        Executor(500) {
            if (!inKuudra) return@Executor
            val entities = mc.theWorld?.loadedEntityList ?: return@Executor

            // maybe possible to add on spawn and remove on leave
            giantZombies.clear()
            buildingPiles.clear()

            // todo: replace with loop from ui branch
            entities.forEach { entity ->
                when (entity) {
                    is EntityGiantZombie -> {
                        if (entity.heldItem.unformattedName == "Head") {
                            giantZombies.add(entity)
                        }
                    }
                    is EntityMagmaCube -> {
                        if (
                            entity.slimeSize == 30 &&
                            entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toFloat() == 100000f
                        ) {
                            kuudraEntity = entity
                        }
                    }
                    is EntityArmorStand -> {
                        if (entity.name.noControlCodes.matches(progressRegex)) {
                            buildingPiles.add(entity)
                        }

                        if (phase == 2) {
                            buildRegex.find(entity.name.noControlCodes)?.let {
                                build = it.groupValues[1].toIntOrNull() ?: 0
                                builders = it.groupValues[2].toIntOrNull() ?: 0
                            }
                        }
                        if (phase != 1 || !entity.name.contains("SUPPLIES RECEIVED")) return@forEach
                        val x = entity.posX.toInt()
                        val z = entity.posY.toInt()
                        when {
                            x == -98 && z == -112 -> supplies[0] = false
                            x == -98 && z == -99 -> supplies[1] = false
                            x == -110 && z == -106 -> supplies[2] = false
                            x == -106 && z == -112 -> supplies[3] = false
                            x == -94 && z == -106 -> supplies[4] = false
                            x == -106 && z == -99 -> supplies[5] = false
                        }
                    }
                }
            }
        }.register()
    }

    @SubscribeEvent
    fun handleTabListPacket(event: PacketReceivedEvent) {
        if (!inKuudra || event.packet !is S38PacketPlayerListItem || !event.packet.action.equalsOneOf(S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, S38PacketPlayerListItem.Action.ADD_PLAYER)) return
        kuudraTeammates = updateKuudraTeammates(kuudraTeammates.toMutableList(), event.packet.entries)
        kuudraTeammatesNoSelf = kuudraTeammates.filter { it.playerName != mc.thePlayer?.name }
    }

    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+)")

    private fun updateKuudraTeammates(previousTeammates: MutableList<KuudraPlayer>, tabList: List<S38PacketPlayerListItem.AddPlayerData>): List<KuudraPlayer> {

        for (line in tabList) {
            val text = line.displayName?.unformattedText?.noControlCodes ?: continue
            val (_, name) = tablistRegex.find(text)?.destructured ?: continue

            previousTeammates.find { it.playerName == name }?.let { kuudraPlayer ->
                kuudraPlayer.entity = mc.theWorld?.getPlayerEntityByName(name)
            } ?: previousTeammates.add(KuudraPlayer(name, entity = mc.theWorld?.getPlayerEntityByName(name)))
        }
        return previousTeammates
    }
}