package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.DragonCheck.dragonEntityList
import me.odinmain.features.impl.floor7.DragonCheck.lastDragonDeath
import me.odinmain.features.impl.floor7.DragonPriority.displaySpawningDragon
import me.odinmain.features.impl.floor7.DragonPriority.findPriority
import me.odinmain.features.impl.floor7.WitherDragons.currentTick
import me.odinmain.features.impl.floor7.WitherDragons.priorityDragon
import me.odinmain.features.impl.floor7.WitherDragons.sendArrowHit
import me.odinmain.features.impl.floor7.WitherDragons.sendSpawned
import me.odinmain.features.impl.floor7.WitherDragons.sendSpawning
import me.odinmain.features.impl.floor7.WitherDragons.sendTime
import me.odinmain.features.impl.skyblock.ArrowHit.onDragonSpawn
import me.odinmain.features.impl.skyblock.ArrowHit.resetOnDragons
import me.odinmain.utils.render.Color
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.Colors
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3

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
    var entityId: Int? = null,
    var entity: EntityDragon? = null,
    var isSprayed: Boolean = false,
    var spawnedTime: Long = 0,
    val skipKillTime: Int = 0,
    val arrowsHit: HashMap<String, Int> = HashMap()
) {
    Red(   Vec3(27.0, 14.0, 59.0), AxisAlignedBB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5),   'c', Colors.MINECRAFT_RED,   24.0..30.0, 56.0..62.0,  skipKillTime = 50),

    Orange(Vec3(85.0, 14.0, 56.0), AxisAlignedBB(72.0, 8.0,  47.0, 102.0,28.0, 77.0),   '6', Colors.MINECRAFT_GOLD,  82.0..88.0, 53.0..59.0,  skipKillTime = 62),

    Green( Vec3(27.0, 14.0, 94.0), AxisAlignedBB(7.0,  8.0,  80.0, 37.0, 28.0, 110.0),  'a', Colors.MINECRAFT_GREEN, 23.0..29.0, 91.0..97.0,  skipKillTime = 52),

    Blue(  Vec3(84.0, 14.0, 94.0), AxisAlignedBB(71.5, 16.0, 82.5, 96.5, 26.0, 107.5),  'b', Colors.MINECRAFT_AQUA,  82.0..88.0, 91.0..97.0,  skipKillTime = 47),

    Purple(Vec3(56.0, 14.0, 125.0), AxisAlignedBB(45.5, 13.0, 113.5,68.5, 23.0, 136.5), '5', Colors.MINECRAFT_DARK_PURPLE, 53.0..59.0, 122.0..128.0, skipKillTime = 38),

    None(  Vec3(0.0, 0.0, 0.0),     AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),        'f', Colors.WHITE, 0.0..0.0, 0.0..0.0);

    fun setAlive(entityId: Int) {
        state = WitherDragonState.ALIVE

        timeToSpawn = 100
        timesSpawned++
        this.entityId = entityId
        spawnedTime = currentTick
        isSprayed = false
        arrowsHit.clear()

        if (resetOnDragons && WitherDragons.enabled) onDragonSpawn()
        if (sendArrowHit && WitherDragons.enabled) {
            runIn(skipKillTime, true) {
                if (entity?.isEntityAlive == true) modMessage("§fArrows Hit on §${colorCode}${name}§f in §c${(skipKillTime / 20f).toFixed()}s§7: ${arrowsHit.entries.joinToString(", ") { "§f${it.key}§7: §6${it.value}§7" }}.")
            }
        }
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

    fun setDead() {
        state = WitherDragonState.DEAD
        dragonEntityList.remove(entity)
        entityId = null
        entity = null
        lastDragonDeath = this
        if (sendArrowHit && WitherDragons.enabled && currentTick - spawnedTime < skipKillTime)
            modMessage("§fArrows Hit on §${colorCode}${name}§7: ${arrowsHit.entries.joinToString(", ") { "§f${it.key}§7: §6${it.value}§7" }}.")
        if (priorityDragon == this) priorityDragon = None

        if (sendTime && WitherDragons.enabled)
            dragonPBs.time(ordinal, (currentTick - spawnedTime) / 20.0, "s§7!", "§${colorCode}${name} §7was alive for §6", addPBString = true, addOldPBString = true)
    }

    fun updateEntity(entityId: Int) {
        entity = (mc.theWorld.getEntityByID(entityId) as? EntityDragon)?.also { dragonEntityList.add(it) } ?: return
    }

    companion object {
        fun reset(soft: Boolean = false) {
            if (soft) return WitherDragonsEnum.entries.forEach {
                it.state = WitherDragonState.DEAD
                it.timesSpawned++
            }

            WitherDragonsEnum.entries.forEach {
                it.timeToSpawn = 100
                it.timesSpawned = 0
                it.state = WitherDragonState.DEAD
                it.entityId = null
                it.entity = null
                it.isSprayed = false
                it.spawnedTime = 0
            }
            dragonEntityList.clear()
            priorityDragon = None
            lastDragonDeath = None
        }
    }
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

    val (spawned, dragons) = WitherDragonsEnum.entries.fold(0 to mutableListOf<WitherDragonsEnum>()) { (spawned, dragons), dragon ->
        val newSpawned = spawned + dragon.timesSpawned

        if (dragon.state == WitherDragonState.SPAWNING) {
            if (dragon !in dragons) dragons.add(dragon)
            return@fold newSpawned to dragons
        }

        if (particle.xCoordinate !in dragon.xRange || particle.zCoordinate !in dragon.zRange) return@fold newSpawned to dragons
        if (sendSpawning && WitherDragons.enabled) modMessage("§${dragon.colorCode}$dragon §fdragon is spawning.")

        dragon.state = WitherDragonState.SPAWNING
        dragons.add(dragon)
        newSpawned to dragons
    }

    if (dragons.isNotEmpty() && (dragons.size == 2 || spawned >= 2) && (priorityDragon == WitherDragonsEnum.None || priorityDragon.entity?.isDead == false))
        priorityDragon = findPriority(dragons).also { displaySpawningDragon(it) }
}