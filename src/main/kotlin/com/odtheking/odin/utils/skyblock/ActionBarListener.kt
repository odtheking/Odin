package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.SecretsUpdateEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.world.entity.ai.attributes.Attributes
import kotlin.math.floor

object ActionBarListener {
    private val HEALTH_REGEX = Regex("([\\d|,]+)/([\\d|,]+)\\uE010")
    private val MANA_REGEX = Regex("([\\d|,]+)/([\\d|,]+)\\uE003")
    private val OVERFLOW_MANA_REGEX = Regex("([\\d|,]+)\\uE017")
    private val DEFENSE_REGEX = Regex("([\\d|,]+)\\uE008( Defense)?")
    private val VITALITY_REGEX = Regex("([\\d.,]+)/([\\d.,]+)\\uE028")
    private val SECRETS_REGEX = Regex("(\\d+)/(\\d+) Secrets")

    var currentHealth: Int = 0
        private set
    var maxHealth: Int = 0
        private set
    var currentMana: Int = 0
        private set
    var maxMana: Int = 0
        private set
    var currentSpeed: Int = 0
        private set
    var currentDefense: Int = 0
        private set
    var overflowMana: Int = 0
        private set
    var effectiveHP: Int = 0
        private set
    var currentVitality: Int = 0
        private set
    var maxVitality: Int = 0
        private set
    var isVitalityShown: Boolean = false
        private set

    init {
        on<TickEvent.End> {
            currentHealth = (mc.player?.let { player -> (maxHealth * player.health / player.maxHealth).toInt() } ?: 0)
            currentSpeed = floor((mc.player?.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue?.toFloat() ?: 0f) * 1000f).toInt()
        }

        onReceive<ClientboundSystemChatPacket> {
            if (!overlay) return@onReceive
            val msg = content.string.noControlCodes

            SECRETS_REGEX.find(msg)?.destructured?.let { (found, max) ->
                DungeonUtils.currentRoom?.let {
                    val updatedFoundSecrets = found.toIntOrNull() ?: return@let
                    if (it.data?.maxSecrets != max.toIntOrNull() || (it.foundSecrets ?: -1) >= updatedFoundSecrets) return@let
                    it.foundSecrets = updatedFoundSecrets
                    SecretsUpdateEvent(it, updatedFoundSecrets).postAndCatch()
                }
            }

            HEALTH_REGEX.find(msg)?.destructured?.let { (_, maxHp) ->
                maxHealth = maxHp.replace(",", "").toIntOrNull() ?: maxHealth
            }

            MANA_REGEX.find(msg)?.destructured?.let { (cMana, mMana) ->
                currentMana = cMana.replace(",", "").toIntOrNull() ?: currentMana
                maxMana = mMana.replace(",", "").toIntOrNull() ?: maxMana
            }

            OVERFLOW_MANA_REGEX.find(msg)?.groupValues?.get(1)?.let {
                overflowMana = it.replace(",", "").toIntOrNull() ?: overflowMana
            }

            DEFENSE_REGEX.find(msg)?.groupValues?.get(1)?.let {
                currentDefense = it.replace(",", "").toIntOrNull() ?: currentDefense
            }

            val vitalityMatch = VITALITY_REGEX.find(msg)
            isVitalityShown = vitalityMatch != null
            vitalityMatch?.destructured?.let { (cVitality, mVitality) ->
                currentVitality = cVitality.replace(",", "").toDoubleOrNull()?.toInt() ?: currentVitality
                maxVitality = mVitality.replace(",", "").toDoubleOrNull()?.toInt() ?: maxVitality
            }

            effectiveHP = (currentHealth * (1 + currentDefense / 100))
        }
    }
}