package com.odtheking.odin.utils

import net.minecraft.world.level.ChunkPos

@JvmInline
value class IVec2 private constructor(val data: Long) {

    constructor(x: Int, z: Int) : this(
        (z.toLong() shl 32) or (x.toLong() and 0xFFFF_FFFFL)
    )

    inline val x: Int
        get() = data.toInt()


    inline val z: Int
        get() = (data shr 32).toInt()

    operator fun component1(): Int {
        return x
    }

    operator fun component2(): Int {
        return z
    }

    operator fun plus(rhs: Int): IVec2 {
        return IVec2(x + rhs, z + rhs)
    }

    operator fun minus(rhs: Int): IVec2 {
        return IVec2(x - rhs, z - rhs)
    }

    operator fun times(rhs: Int): IVec2 {
        return IVec2(x * rhs, z * rhs)
    }

    operator fun div(rhs: Int): IVec2 {
        return IVec2(x / rhs, z / rhs)
    }

    override fun toString(): String {
        return "IVec2(x=$x, z=$z)"
    }

    fun flip(): IVec2 {
        return IVec2(z, x)
    }
}

fun ChunkPos.toIVec2(): IVec2 {
    return IVec2(x, z)
}