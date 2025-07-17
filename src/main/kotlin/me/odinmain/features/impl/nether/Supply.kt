package me.odinmain.features.impl.nether

import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

enum class Supply(val pickUpSpot: Vec3, val dropOffSpot: BlockPos, var isActive: Boolean = true) {
    Triangle(Vec3(-67.0, 77.0, -122.0), BlockPos(-94, 78, -106)),
    X(Vec3(-142.0, 77.0, -151.0), BlockPos(-106, 78, -112)),
    Equals(Vec3(-65.0, 76.0, -87.0), BlockPos(-98, 78, -99)),
    Slash(Vec3(-113.0, 77.0, -68.0), BlockPos(-106, 78, -99)),
    Shop(Vec3(-81.0, 76.0, -143.0), BlockPos(-98, 78, -112)),
    xCannon(Vec3(-143.0, 76.0, -12.05), BlockPos(-110, 78, -106)),
    Square(Vec3(-143.0, 76.0, -80.0), BlockPos(0, 0, 0)),
    None(Vec3(0.0, 0.0, 0.0), BlockPos(0, 0, 0));
}