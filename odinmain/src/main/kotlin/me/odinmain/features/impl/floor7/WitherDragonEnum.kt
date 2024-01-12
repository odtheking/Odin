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
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3


enum class WitherDragonsEnum(
    val pos: BlockPos,
    val boxesDimentions: AxisAlignedBB,
    val colorCode: String,
    val color: Color,
    val xRange: ClosedRange<Double>,
    val zRange: ClosedRange<Double>,
    val textPos: Vec3,
    var alive: Boolean,
    val setting: NumberSetting<Double>,
    var particleSpawnTime: Long,
    val DRAGON_SPAWN_TIME: Long = 5000L

) {
    Red(BlockPos(32, 22, 59), AxisAlignedBB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5),"c",
        Color.RED, 24.0..30.0, 56.0..62.0, Vec3(27.0, 18.0, 60.0), true, redPB, 0L),

    Orange(BlockPos(80, 23, 56), AxisAlignedBB(72.0, 8.0,  47.0, 102.0,28.0, 77.0),"6",
        Color.ORANGE, 82.0..88.0, 53.0..59.0, Vec3(84.0, 18.0, 56.0), true, orangePB, 0L, 5000L),

    Green (BlockPos(32, 23, 94), AxisAlignedBB(7.0,  8.0,  80.0, 37.0, 28.0, 110.0),"a",
        Color.GREEN, 23.0..29.0, 91.0..97.0, Vec3(26.0, 18.0, 95.0), true, greenPB, 0L, 5000L),

    Blue  (BlockPos(79, 23, 94), AxisAlignedBB(71.5, 16.0, 82.5, 96.5, 26.0, 107.5),"b",
        Color.BLUE, 82.0..88.0, 91.0..97.0, Vec3(84.0, 18.0, 95.0), true, bluePB, 0L, 5000L),

    Purple(BlockPos(56, 22, 120), AxisAlignedBB(45.5, 13.0, 113.5,68.5, 23.0, 136.5),"5",
        Color.PURPLE, 53.0..59.0, 122.0..128.0, Vec3(57.0, 18.0, 125.0),true, purplePB, 0L, 5000L);

    fun checkAlive() {
        this.alive = !WorldUtils.isAir(this.pos)
    }

    fun spawnTime(): Long {
        return DRAGON_SPAWN_TIME - (System.currentTimeMillis() - this.particleSpawnTime)
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

    DragonPriority.dragPrioSpawn()
    WitherDragonsEnum.entries.forEach { dragon ->
        if (checkParticle(particle, dragon) && dragon.particleSpawnTime == 0L) {
            if (sendSpawning) modMessage("§${dragon.colorCode}$dragon §fdragon is spawning.")
            dragon.particleSpawnTime = System.currentTimeMillis()
        }
    }
}

private fun checkParticle(event: S2APacketParticles, color: WitherDragonsEnum): Boolean {
    return  event.xCoordinate in color.xRange &&
            event.yCoordinate in 15.0..22.0 &&
            event.zCoordinate in color.zRange
}

