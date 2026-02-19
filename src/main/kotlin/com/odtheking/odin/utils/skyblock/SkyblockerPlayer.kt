package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.OverlayPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import net.minecraft.world.entity.ai.attributes.Attributes
import kotlin.math.floor

object SkyblockPlayer {
    /*
    in module there should be:
    health display current/Max
    health bar
    defense display
    mana display current/Max
    mana bar
    current speed
    current ehp
    current overflow mana
     */

    private val HEALTH_REGEX = Regex("([\\d|,]+)/([\\d|,]+)❤")
    private val MANA_REGEX = Regex("([\\d|,]+)/([\\d|,]+)✎")
    private val OVERFLOW_MANA_REGEX = Regex("([\\d|,]+)ʬ")
    private val DEFENSE_REGEX = Regex("([\\d|,]+)❈ Defense")

    var currentHealth: Int = 0
        private set
    var maxHealth: Int = 0
    var currentMana: Int = 0
    var maxMana: Int = 0
    var currentSpeed: Int = 0
    var currentDefense: Int = 0
    var overflowMana: Int = 0
    var effectiveHP: Int = 0

    init {
        on<TickEvent.End> {
            currentHealth = (mc.player?.let { player -> (maxHealth * player.health / player.maxHealth).toInt() } ?: 0)
            currentSpeed = floor((mc.player?.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue?.toFloat() ?: 0f) * 1000f).toInt()
        }

        on<OverlayPacketEvent> {
            HEALTH_REGEX.find(value)?.destructured?.let { (_, maxHp) ->
                maxHealth = maxHp.replace(",", "").toIntOrNull() ?: maxHealth
            }

            MANA_REGEX.find(value)?.destructured?.let { (cMana, mMana) ->
                currentMana = cMana.replace(",", "").toIntOrNull() ?: currentMana
                maxMana = mMana.replace(",", "").toIntOrNull() ?: maxMana
            }

            OVERFLOW_MANA_REGEX.find(value)?.groupValues?.get(1)?.let {
                overflowMana = it.replace(",", "").toIntOrNull() ?: overflowMana
            }

            DEFENSE_REGEX.find(value)?.groupValues?.get(1)?.let {
                currentDefense = it.replace(",", "").toIntOrNull() ?: currentDefense
            }

            effectiveHP = (currentHealth * (1 + currentDefense / 100))
        }
    }
}