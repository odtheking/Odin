package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.isItem
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.phys.AABB
import kotlin.math.ln

object SpringBoots : Module(
    name = "Spring Boots",
    description = "Shows the current jump height of your spring boots."
) {
    private val hud by HUD("Spring Boots", "Shows how high you will jump.") {
        if (blockAmount == 0f && !it) return@HUD 0 to 0
        var width = 1
        width += textDim("Height: ", width, 1, Colors.MINECRAFT_LIGHT_PURPLE, true).first
        width += textDim(getColor(blockAmount), width, 1, Colors.WHITE, true).first
        width to mc.font.lineHeight
    }

    private val pitchSet = setOf(0.82539684f, 0.8888889f, 0.93650794f, 1.0476191f, 1.1746032f, 1.3174603f, 1.7777778f)
    private var blockAmount = 0f
    private var plingCount = 0
    private var lowPlings = 0
    private var ticksSincePling = 0
    private var uncrouchTicks = 0

    private val gapIncreases = intArrayOf(10, 17, 25, 32)

    init {
        onReceive<ClientboundSoundPacket> {
            if (!LocationUtils.isInSkyblock) return@onReceive
            val id = sound.value().location

            when {
                SoundEvents.NOTE_BLOCK_PLING.`is`(id) && mc.player?.isCrouching == true && EquipmentSlot.FEET.isItem("SPRING_BOOTS") -> {
                    when (pitch) {
                        0.6984127f if lowPlings < 2 -> lowPlings++
                        in pitchSet -> {}
                        else -> return@onReceive
                    }
                    plingCount++
                    ticksSincePling = 0
                    blockAmount = currentHeight()
                }

                SoundEvents.FIREWORK_ROCKET_LAUNCH.location == id && pitch.equalsOneOf(0.0952381f, 1.6984127f) -> reset()
            }
        }

        on<TickEvent.End> {
            if (!LocationUtils.isInSkyblock || !EquipmentSlot.FEET.isItem("SPRING_BOOTS")) return@on
            when {
                mc.player?.isCrouching == true -> {
                    uncrouchTicks = 0
                    if (plingCount > 0) {
                        ticksSincePling++
                        blockAmount = currentHeight()
                    }
                }
                blockAmount != 0f -> if (++uncrouchTicks > 20) reset()
            }
        }

        on<RenderExtractEvent> {
            if (!LocationUtils.isInSkyblock || blockAmount == 0f) return@on
            mc.player?.position()?.addVec(y = blockAmount)?.let { drawWireFrameBox(AABB.unitCubeFromLowerCorner(it), Colors.MINECRAFT_RED) }
        }
    }

    private fun reset() {
        plingCount = 0
        lowPlings = 0
        ticksSincePling = 0
        uncrouchTicks = 0
        blockAmount = 0f
    }

    private fun currentHeight(): Float {
        val chargeTicks = (1 until plingCount).sumOf { p -> 2.0 * (1 + gapIncreases.count { plingCount >= p }) } + ticksSincePling + 5.0
        return (15.4306 * ln(chargeTicks +  7.0) + -30.5623).coerceAtLeast(0.0).toFloat()
    }

    private fun getColor(blocks: Float): String {
        return when {
            blocks <= 13.5 -> "§c"
            blocks <= 22.5 -> "§e"
            blocks <= 33.0 -> "§6"
            blocks <= 43.5 -> "§a"
            else -> "§b"
        } + "%.1f".format(blocks)
    }
}
