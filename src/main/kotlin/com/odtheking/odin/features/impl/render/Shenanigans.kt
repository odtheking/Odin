package com.odtheking.odin.features.impl.render

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.render.drawTexturedQuad
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.Vec3
import java.time.Instant
import java.time.ZoneOffset

object Shenanigans {

    private data class Logos(
        val identifier: Identifier,
        val position: Vec3,
        val width: Float,
        val height: Float,
        val yaw: Float
    )

    private val logoList = listOf(
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(73.5, 224.5, 11.0), 6f, 3f, 0f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(73.5, 216.5, 50.0), 10f, 7f, 180f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(73.5, 216.5, 32.0), 10f, 7f, 0f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(100.5, 127.5, 143.0), 8f, 5f, 180f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(111.0, 127.5, 132.5), 8f, 5f, 90f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(8.5, 127.5, 143.0), 8f, 5f, 180f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(-2.0, 127.5, 132.5), 8f, 5f, 270f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(-2.0, 127.5, 40.5), 8f, 5f, 270f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(8.5, 127.5, 30.0), 8f, 5f, 0f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(54.5, 116.5, 118.0), 9f, 6f, 180f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/logo.png"), Vec3(54.5, 64.0, 84.0), 3f, 2f, 0f),
        Logos(Identifier.fromNamespaceAndPath("odin", "textures/job.png"), Vec3(54.5, 23.5, 40.0), 25f, 35f, 0f)
    )

    private var enabled = false

    init {
        on<RenderEvent.Extract> {
            if (!enabled) return@on
            if (!DungeonUtils.isFloor(7) || !DungeonUtils.inBoss) return@on

            logoList.forEach { (identifier, pos, width, height, yaw) ->
                drawTexturedQuad(identifier, pos, width, height, yaw)
            }
        }

        on<LevelEvent.Load> {
            val now = Instant.now().atZone(ZoneOffset.UTC)

            enabled = (now.monthValue == 4 && now.dayOfMonth == 1) || run {
                val window = now.toEpochSecond() / 600L
                ((window xor (window shr 3) xor (window shl 1)) and Long.MAX_VALUE) % 288L == 0L
            }
        }
    }
}