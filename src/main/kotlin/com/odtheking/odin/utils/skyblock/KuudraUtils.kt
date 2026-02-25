package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.noControlCodes
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.Giant
import net.minecraft.world.entity.monster.MagmaCube
import kotlin.jvm.optionals.getOrNull

object KuudraUtils {

    inline val inKuudra get() = LocationUtils.isCurrentArea(Island.Kuudra)

    val freshers: MutableMap<String, Long?> = mutableMapOf()
    val giantZombies: ArrayList<Giant> = arrayListOf()
    var kuudraEntity: MagmaCube? = null
        private set
    var phase = 0
        private set

    val buildingPiles = arrayListOf<ArmorStand>()
    var playersBuildingAmount = 0
        private set
    var buildDonePercentage = 0
        private set

    var kuudraTier: Int = 0
        private set

    private val ownFreshRegex =
        Regex("^Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!$")
    private val buildRegex = Regex("Building Progress (\\d+)% \\((\\d+) Players Helping\\)")
    private val partyFreshRegex = Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): FRESH$")
    private val tierRegex = Regex("Kuudra's Hollow \\(T(\\d)\\)$")
    private val progressRegex = Regex("PROGRESS: (\\d+)%")

    init {
        TickTask(10) {
            if (!inKuudra) return@TickTask
            val entities = mc.level?.entitiesForRendering() ?: return@TickTask

            giantZombies.clear()
            buildingPiles.clear()

            entities.forEach { entity ->
                when (entity) {
                    is Giant ->
                        if (entity.mainHandItem?.hoverName?.string?.endsWith("Head") == true) giantZombies.add(entity)

                    is MagmaCube ->
                        if (entity.size == 30 && entity.getAttributeBaseValue(Attributes.MAX_HEALTH) == 100000.0)
                            kuudraEntity = entity

                    is ArmorStand -> {
                        if (entity.name.string.matches(progressRegex)) buildingPiles.add(entity)

                        if (phase == 2) {
                            buildRegex.find(entity.name.string)?.let {
                                playersBuildingAmount = it.groupValues[2].toIntOrNull() ?: 0
                                buildDonePercentage = it.groupValues[1].toIntOrNull() ?: 0
                            }
                        }
                        if (phase != 1 || entity.name?.string != "✓ SUPPLIES RECEIVED ✓") return@forEach
                        val x = entity.x.toInt()
                        val z = entity.z.toInt()
                        when (x) {
                            -98 if z == -112 -> Supply.Shop.isActive = false
                            -98 if z == -99 -> Supply.Equals.isActive = false
                            -110 if z == -106 -> Supply.xCannon.isActive = false
                            -106 if z == -112 -> Supply.X.isActive = false
                            -94 if z == -106 -> Supply.Triangle.isActive = false
                            -106 if z == -99 -> Supply.Slash.isActive = false
                        }
                    }
                }
            }
        }

        on<ChatPacketEvent> {
            if (!inKuudra) return@on

            when (value) {
                "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> phase = 1
                "[NPC] Elle: OMG! Great work collecting my supplies!" -> phase = 2
                "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> phase = 3
                "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> phase = 4
            }

            partyFreshRegex.find(value)?.groupValues?.get(2)?.let { playerName ->
                freshers[playerName] = System.currentTimeMillis()
                schedule(200, true) {
                    freshers[playerName] = null
                }
            }

            ownFreshRegex.find(value)?.let {
                freshers[mc.player?.name?.string ?: "self"] = System.currentTimeMillis()
                schedule(200, true) {
                    freshers[mc.player?.name?.string ?: "self"] = null
                }
            }
        }

        onReceive<ClientboundSetPlayerTeamPacket> {
            if (!inKuudra) return@onReceive
            val teamLine = parameters.getOrNull() ?: return@onReceive
            val text = teamLine.playerPrefix.string?.plus(teamLine.playerSuffix.string)?.noControlCodes ?: return@onReceive

            tierRegex.find(text)?.groupValues?.get(1)?.let { kuudraTier = it.toInt() }
        }

        on<WorldEvent.Load> {
            Supply.entries.forEach { it.isActive = true }
            playersBuildingAmount = 0
            buildDonePercentage = 0
            buildingPiles.clear()
            giantZombies.clear()
            kuudraEntity = null
            freshers.clear()
            kuudraTier = 0
            phase = 0
        }
    }
}