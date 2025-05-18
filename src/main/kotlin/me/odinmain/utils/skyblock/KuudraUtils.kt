package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.getSBMaxHealth
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.LocationUtils.currentArea
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.util.Vec3
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraUtils {

    var kuudraTeammates: ArrayList<KuudraPlayer> = ArrayList(4)
    var giantZombies: ArrayList<EntityGiantZombie> = arrayListOf()
    var supplies = BooleanArray(6) { true }
    var kuudraEntity: EntityMagmaCube? = null

    var buildingPiles = arrayListOf<EntityArmorStand>()
    var playersBuildingAmount = 0
    var buildDonePercentage = 0

    var kuudraTier: Int = 0
        private set
    private val tierRegex = Regex("Kuudra's Hollow \\(T(\\d)\\)\$")

    private val freshRegex = Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): FRESH\$")
    private val buildRegex = Regex("Building Progress (\\d+)% \\((\\d+) Players Helping\\)")
    private val progressRegex = Regex("PROGRESS: (\\d+)%")

    var phase = 0

    inline val inKuudra get() = LocationUtils.currentArea.isArea(Island.Kuudra)

    data class KuudraPlayer(val playerName: String, var eatFresh: Boolean = false, var eatFreshTime: Long = 0, var entity: EntityPlayer? = null)

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        supplies = BooleanArray(6) { true }
        playersBuildingAmount = 0
        buildDonePercentage = 0
        kuudraTeammates.clear()
        buildingPiles.clear()
        giantZombies.clear()
        kuudraEntity = null
        phase = 0
        kuudraTier = 0
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (!inKuudra) return

        when (event.message) {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> phase = 1
            "[NPC] Elle: OMG! Great work collecting my supplies!" -> phase = 2
            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> phase = 3
            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> phase = 4
        }

        if (event.message.matches(freshRegex)) {
            val playerName = freshRegex.find(event.message)?.groupValues?.get(1) ?: return

            kuudraTeammates.find { it.playerName == playerName }?.let { kuudraPlayer ->
                if (mc.thePlayer?.name == kuudraPlayer.playerName)
                kuudraPlayer.eatFresh = true
                runIn(200) {
                    kuudraPlayer.eatFresh = false
                }
            }
        }
    }

    init {
        Executor(500, "KuudraUtils") {
            if (!inKuudra) return@Executor
            val entities = mc.theWorld?.loadedEntityList ?: return@Executor

            giantZombies.clear()
            buildingPiles.clear()

            entities.forEach { entity ->
                when (entity) {
                    is EntityGiantZombie ->
                        if (entity.heldItem?.unformattedName?.endsWith("Head") == true) giantZombies.add(entity)

                    is EntityMagmaCube ->
                        if (entity.slimeSize == 30 && entity.getSBMaxHealth() == 100000f) kuudraEntity = entity

                    is EntityArmorStand -> {
                        if (entity.name.noControlCodes.matches(progressRegex)) buildingPiles.add(entity)

                        if (phase == 2) {
                            buildRegex.find(entity.name.noControlCodes)?.let {
                                buildDonePercentage = it.groupValues[1].toIntOrNull() ?: 0
                                playersBuildingAmount = it.groupValues[2].toIntOrNull() ?: 0
                            }
                        }
                        if (phase != 1 || entity.name.noControlCodes != "✓ SUPPLIES RECEIVED ✓") return@forEach
                        val x = entity.posX.toInt()
                        val z = entity.posZ.toInt()
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
    fun handleTabListPacket(event: PacketEvent.Receive) {
        if (event.packet !is S38PacketPlayerListItem ||
            !event.packet.action.equalsOneOf(S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, S38PacketPlayerListItem.Action.ADD_PLAYER) ||
            !LocationUtils.currentArea.isArea(Island.Unknown, Island.Kuudra)) return
        kuudraTeammates = updateKuudraTeammates(kuudraTeammates, event.packet.entries)
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        when (event.packet) {
            is S3EPacketTeams -> {
                if (!currentArea.isArea(Island.Kuudra) || event.packet.action != 2) return
                val text = event.packet.prefix?.plus(event.packet.suffix)?.noControlCodes ?: return

                tierRegex.find(text)?.groupValues?.get(1)?.let { kuudraTier = it.toInt() }
            }
        }
    }

    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+)")

    private fun updateKuudraTeammates(previousTeammates: ArrayList<KuudraPlayer>, tabList: List<S38PacketPlayerListItem.AddPlayerData>): ArrayList<KuudraPlayer> {
        val world = mc.theWorld ?: return previousTeammates
        for (line in tabList) {
            val text = line.displayName?.unformattedText?.noControlCodes ?: continue
            val (_, name) = tablistRegex.find(text)?.destructured ?: continue

            previousTeammates.find { it.playerName == name }?.let { kuudraPlayer ->
                kuudraPlayer.entity = world.getPlayerEntityByName(name) ?: kuudraPlayer.entity
            } ?: previousTeammates.add(KuudraPlayer(name, entity = world.getPlayerEntityByName(name) ?: continue))
        }
        return previousTeammates
    }

    enum class SupplyPickUpSpot(val location: Vec3) {
        Triangle(Vec3(-67.5, 77.0, -122.5)),
        X(Vec3(-142.5, 77.0, -151.0)),
        Equals(Vec3(-65.5, 76.0, -87.5)),
        Slash(Vec3(-113.5, 77.0, -68.5)),
        Shop(Vec3(-81.0, 76.0, -143.0)),
        xCannon(Vec3(-143.0, 76.0, -125.0)),
        Square(Vec3(-143.0, 76.0, -80.0)),
        None(Vec3(0.0, 0.0, 0.0))
    }
}