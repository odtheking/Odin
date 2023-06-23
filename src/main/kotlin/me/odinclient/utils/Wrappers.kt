package me.odinclient.utils

import me.odinclient.OdinClient
import net.minecraft.client.Minecraft

open class Wrappers {
    companion object {
        val mc: Minecraft = OdinClient.mc

        val posX
            get() = mc.thePlayer.posX

        val posY
            get() = mc.thePlayer.posY

        val posZ
            get() = mc.thePlayer.posZ

        val cfg
            get() = OdinClient.config
    }
}