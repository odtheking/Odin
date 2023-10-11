package me.odinclient.dungeonmap.core

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage

data class DungeonPlayer(val skin: ResourceLocation) {
    var name = ""

    /** Minecraft formatting code for the player's name */
    var colorPrefix = 'f'

    var mapX = 0
    var mapZ = 0
    var yaw = 0f

    /** Has information from player entity been loaded */
    var playerLoaded = false
    var icon = ""
    var renderHat = false
    var dead = false
    var uuid = ""

    /** Set player data that requires entity to be loaded */
    fun setData(player: EntityPlayer) {
        renderHat = player.isWearing(EnumPlayerModelParts.HAT)
        uuid = player.uniqueID.toString()
        playerLoaded = true
    }

    var bufferedImage: BufferedImage = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB)
}