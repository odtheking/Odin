package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.features.impl.boss.DragonCheck.lastDragonDeath
import com.odtheking.odin.features.impl.boss.DragonPriority.findPriority
import com.odtheking.odin.features.impl.boss.WitherDragons.currentTick
import com.odtheking.odin.features.impl.boss.WitherDragons.dragonPriorityToggle
import com.odtheking.odin.features.impl.boss.WitherDragons.priorityDragon
import com.odtheking.odin.features.impl.boss.WitherDragons.sendSpawned
import com.odtheking.odin.features.impl.boss.WitherDragons.sendSpawning
import com.odtheking.odin.features.impl.boss.WitherDragons.sendTime
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.modMessage
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.phys.AABB
import java.util.*

enum class WitherDragonsEnum(
    val spawnPos: BlockPos,
    val statuePos: BlockPos,
    val aabbDimensions: AABB,
    val colorCode: Char,
    val color: Color,
    val xRange: ClosedFloatingPointRange<Double>,
    val zRange: ClosedFloatingPointRange<Double>,
    var timeToSpawn: Int = 100,
    var state: WitherDragonState = WitherDragonState.DEAD,
    var timesSpawned: Int = 0,
    var entityUUID: UUID? = null,
    var isSprayed: Boolean = false,
    var spawnedTime: Long = 0,
    val skipKillTime: Int = 0
) {
    Red(BlockPos(27, 14, 59), BlockPos(32, 22, 59), AABB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5), 'c', Colors.MINECRAFT_RED, 24.0..30.0, 56.0..62.0, skipKillTime = 50),
    Orange(BlockPos(85, 14, 56), BlockPos(80, 23, 56), AABB(72.0, 8.0, 47.0, 102.0, 28.0, 77.0), '6', Colors.MINECRAFT_GOLD, 82.0..88.0, 53.0..59.0, skipKillTime = 62),
    Green(BlockPos(27, 14, 94), BlockPos(32, 23, 94), AABB(7.0, 8.0, 80.0, 37.0, 28.0, 110.0), 'a', Colors.MINECRAFT_GREEN, 23.0..29.0, 91.0..97.0, skipKillTime = 52),
    Blue(BlockPos(84, 14, 94), BlockPos(79, 23, 94), AABB(71.5, 13.0, 82.5, 96.5, 26.0, 107.5), 'b', Colors.MINECRAFT_AQUA, 82.0..88.0, 91.0..97.0, skipKillTime = 47),
    Purple(BlockPos(56, 14, 125), BlockPos(56, 22, 120), AABB(45.5, 13.0, 113.5, 68.5, 23.0, 136.5), '5', Colors.MINECRAFT_DARK_PURPLE, 53.0..59.0, 122.0..128.0, skipKillTime = 38);

    fun setAlive(entityId: UUID?) {
        if (entityId != null) this.entityUUID = entityId

        if (state == WitherDragonState.ALIVE || state != WitherDragonState.SPAWNING) return
        state = WitherDragonState.ALIVE

        timesSpawned++
        spawnedTime = currentTick
        isSprayed = false

        if (sendSpawned && WitherDragons.enabled) {
            val numberSuffix = when (timesSpawned) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
            modMessage("§${colorCode}${name} §fdragon spawned. This is the §${colorCode}${timesSpawned}${numberSuffix}§f time it has spawned.")
        }
    }

    fun setDead(realTime: Boolean) {
        state = WitherDragonState.DEAD
        entityUUID = null
        lastDragonDeath = this

        if (priorityDragon == this) priorityDragon = null

        if (sendTime && WitherDragons.enabled && realTime)
            WitherDragons.dragonPBs.time(name, (currentTick - spawnedTime) / 20f, "s§7!", "§${colorCode}${name} §7was alive for §6")
    }

    companion object {
        fun reset(soft: Boolean = false) {
            if (soft) {
                WitherDragonsEnum.entries.forEach {
                    it.state = WitherDragonState.DEAD
                    it.timesSpawned++
                }
                return
            }

            WitherDragonsEnum.entries.forEach {
                it.timeToSpawn = 0
                it.timesSpawned = 0
                it.state = WitherDragonState.DEAD
                it.entityUUID = null
                it.isSprayed = false
                it.spawnedTime = 0
            }
            priorityDragon = null
            lastDragonDeath = null
        }
    }
}

enum class WitherDragonState {
    SPAWNING,
    ALIVE,
    DEAD
}

fun handleSpawnPacket(particle: ClientboundLevelParticlesPacket) {
    if (
        particle.count != 20 ||
        particle.y != 19.0 ||
        particle.particle.type != ParticleTypes.FLAME ||
        particle.xDist != 2f ||
        particle.yDist != 3f ||
        particle.zDist != 2f ||
        particle.maxSpeed != 0f ||
        particle.x % 1 != 0.0 ||
        particle.z % 1 != 0.0
    ) return

    val (spawned, dragons) = WitherDragonsEnum.entries.fold(0 to mutableListOf<WitherDragonsEnum>()) { (spawned, dragons), dragon ->
        val newSpawned = spawned + dragon.timesSpawned

        if (dragon.state == WitherDragonState.SPAWNING) {
            if (dragon !in dragons) dragons.add(dragon)
            return@fold newSpawned to dragons
        }

        if (particle.x !in dragon.xRange || particle.z !in dragon.zRange) return@fold newSpawned to dragons

        if (sendSpawning && WitherDragons.enabled) modMessage("§${dragon.colorCode}$dragon §fdragon is spawning.")

        dragon.state = WitherDragonState.SPAWNING
        dragon.timeToSpawn = 100
        dragons.add(dragon)
        newSpawned to dragons
    }

    if (dragons.isNotEmpty() && (dragons.size == 2 || spawned >= 2) && priorityDragon == null)
        priorityDragon = findPriority(dragons).also { dragon ->
            if (WitherDragons.dragonTitle && WitherDragons.enabled) alert("§${dragon.colorCode}${dragon.name} is spawning!", true)
            if (dragonPriorityToggle && WitherDragons.enabled) modMessage("${dragons.joinToString(", ") { "§${it.colorCode}${it.name}" }}§r -> §${dragon.colorCode}${dragon.name} §7is your priority dragon!")
        }
}

