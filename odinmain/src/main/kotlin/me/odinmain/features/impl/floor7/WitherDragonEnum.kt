package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.features.impl.floor7.WitherDragons.bluePB
import me.odinmain.features.impl.floor7.WitherDragons.greenPB
import me.odinmain.features.impl.floor7.WitherDragons.orangePB
import me.odinmain.features.impl.floor7.WitherDragons.purplePB
import me.odinmain.features.impl.floor7.WitherDragons.redPB
import me.odinmain.features.impl.floor7.WitherDragons.sendSpawning
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.WorldUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3


enum class WitherDragonsEnum (
    val spawnPos: Vec3,
    val boxesDimensions: AxisAlignedBB,
    val colorCode: String,
    val color: Color,
    val xRange: ClosedRange<Double>,
    val zRange: ClosedRange<Double>,
    val dragonKillPBs: NumberSetting<Double>,
    private val statusAlivePos: BlockPos,
    var particleSpawnTime: Long = 0L,
    var statueAlive: Boolean = true,
    var spawning: Boolean = false,
    var dragonAlive: Boolean = false,
    var timesSpawned: Int = 0,
    var entity: Entity? = null,
) {
    Red(Vec3(27.0, 18.0, 60.0), AxisAlignedBB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5),"c", Color.RED,
        24.0..30.0, 56.0..62.0, redPB, BlockPos(32, 22, 59)),

    Orange(Vec3(84.0, 18.0, 56.0), AxisAlignedBB(72.0, 8.0,  47.0, 102.0,28.0, 77.0),"6", Color.ORANGE,
        82.0..88.0, 53.0..59.0, orangePB, BlockPos(80, 23, 56)),

    Green(Vec3(26.0, 18.0, 95.0), AxisAlignedBB(7.0,  8.0,  80.0, 37.0, 28.0, 110.0),"a", Color.GREEN,
        23.0..29.0, 91.0..97.0,  greenPB, BlockPos(32, 23, 94)),

    Blue(Vec3(84.0, 18.0, 95.0), AxisAlignedBB(71.5, 16.0, 82.5, 96.5, 26.0, 107.5),"b", Color.BLUE,
        82.0..88.0, 91.0..97.0,  bluePB, BlockPos(79, 23, 94)),

    Purple(Vec3(57.0, 18.0, 125.0),  AxisAlignedBB(45.5, 13.0, 113.5,68.5, 23.0, 136.5),"5", Color.PURPLE,
        53.0..59.0, 122.0..128.0,  purplePB, BlockPos(56, 22, 120));

    fun checkAlive() {
        this.statueAlive = !WorldUtils.isAir(this.statusAlivePos)
    }

    fun spawnTime(): Long {
        return 5000L - (System.currentTimeMillis() - this.particleSpawnTime)
    }

}

fun handleSpawnPacket(event: ReceivePacketEvent) {
    if (event.packet !is S2APacketParticles) return
    val particle = event.packet
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
        if (checkParticle(particle, dragon) && dragon.particleSpawnTime == 0L) {
            dragon.spawning = true
            if (sendSpawning) modMessage("§${dragon.colorCode}$dragon §fdragon is spawning.")
            dragon.particleSpawnTime = System.currentTimeMillis()
        }
    }
    if (WitherDragons.dragonPriorityToggle) DragonPriority.dragonPrioritySpawn()
}

private fun checkParticle(event: S2APacketParticles, color: WitherDragonsEnum): Boolean {
    return  event.xCoordinate in color.xRange &&
            event.yCoordinate in 15.0..22.0 &&
            event.zCoordinate in color.zRange
}

