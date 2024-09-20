package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.DragonPriority.dragonPrioritySpawn
import me.odinmain.features.impl.floor7.DragonPriority.findPriority
import me.odinmain.features.impl.floor7.WitherDragons.priorityDragon
import me.odinmain.features.impl.floor7.WitherDragons.sendSpawning
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.*

enum class WitherDragonsEnum (
    val spawnPos: Vec3,
    val boxesDimensions: AxisAlignedBB,
    val colorCode: Char,
    val color: Color,
    val xRange: ClosedRange<Double>,
    val zRange: ClosedRange<Double>,
    var timeToSpawn: Int = 100,
    var state: WitherDragonState = WitherDragonState.DEAD,
    var timesSpawned: Int = 0,
    var entity: EntityDragon? = null,
    var isSprayed: Boolean = false,
    var spawnedTime: Long = 0,
    val skipKillTime: Long = 0L
) {
    Red(Vec3(27.0, 14.0, 59.0), AxisAlignedBB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5),'c', Color.RED,
        24.0..30.0, 56.0..62.0, skipKillTime = 2500),

    Orange(Vec3(85.0, 14.0, 56.0), AxisAlignedBB(72.0, 8.0,  47.0, 102.0,28.0, 77.0),'6', Color.ORANGE,
        82.0..88.0, 53.0..59.0, skipKillTime = 3080),

    Green(Vec3(27.0, 14.0, 94.0), AxisAlignedBB(7.0,  8.0,  80.0, 37.0, 28.0, 110.0),'a', Color.GREEN,
        23.0..29.0, 91.0..97.0, skipKillTime = 2600),

    Blue(Vec3(84.0, 14.0, 94.0), AxisAlignedBB(71.5, 16.0, 82.5, 96.5, 26.0, 107.5),'b', Color.BLUE,
        82.0..88.0, 91.0..97.0, skipKillTime = 1920),

    Purple(Vec3(56.0, 14.0, 125.0), AxisAlignedBB(45.5, 13.0, 113.5,68.5, 23.0, 136.5),'5', Color.PURPLE,
        53.0..59.0, 122.0..128.0, skipKillTime = 1900),

    None(Vec3(0.0, 0.0, 0.0), AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), 'f', Color.WHITE,
        0.0..0.0, 0.0..0.0);
}

enum class WitherDragonState {
    SPAWNING,
    ALIVE,
    DEAD
}

val dragonPBs = PersonalBest("Dragons", 5)

fun handleSpawnPacket(particle: S2APacketParticles) {
    if (
        particle.particleCount != 20 ||
        particle.yCoordinate != 19.0 ||
        particle.particleType != EnumParticleTypes.FLAME ||
        particle.xOffset != 2f ||
        particle.yOffset != 3f ||
        particle.zOffset != 2f ||
        particle.particleSpeed != 0f ||
        !particle.isLongDistance ||
        particle.xCoordinate % 1 != 0.0 ||
        particle.zCoordinate % 1 != 0.0
    ) return

    WitherDragonsEnum.entries.forEach { dragon ->
        if (checkParticle(particle, dragon) && dragon.timeToSpawn == 0) {
            if (sendSpawning && WitherDragons.enabled) modMessage("§${dragon.colorCode}$dragon §fdragon is spawning.")
            dragon.state = WitherDragonState.SPAWNING
            dragon.timeToSpawn = 100
        }
    }
    val spawningDragons = WitherDragonsEnum.entries.filter { it.state == WitherDragonState.SPAWNING }.toMutableList().ifEmpty { return }
    findPriority(spawningDragons).also { if (it != WitherDragonsEnum.None) priorityDragon = it }
    if (priorityDragon.timeToSpawn in System.currentTimeMillis()-100..System.currentTimeMillis()+100) dragonPrioritySpawn(priorityDragon)
}

private fun checkParticle(event: S2APacketParticles, color: WitherDragonsEnum): Boolean {
    return event.xCoordinate in color.xRange && event.zCoordinate in color.zRange
}

