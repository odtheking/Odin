package me.odinclient.features.general

import cc.polyfrost.oneconfig.libs.universal.UChat
import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.LocationUtils
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Welcome {

    @SubscribeEvent
    fun onTick(event: WorldEvent.Load) {
        if (miscConfig.hasJoined || !LocationUtils.inSkyblock) return
        miscConfig.hasJoined = true
        miscConfig.saveAllConfigs()
        UChat.chat("""
            ${ChatUtils.getChatBreak().dropLast(1)}
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            
            §7Thanks for installing §3Odin§bClient ${OdinClient.VERSION}§7!

             §eUse §d§l/od §r§eto access GUI settings.
             §eUse §d§l/od help §r§efor all of of the commands.
             
             §eJoin the discord for support and suggestions.
        """.trimIndent())
        mc.thePlayer.addChatMessage(ChatComponentText(" §9https://discord.gg/2nCbC9hkxT")
            .setChatStyle(createClickStyle(ClickEvent.Action.OPEN_URL, "https://discord.gg/2nCbC9hkxT")))

        UChat.chat("""
            
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            ${ChatUtils.getChatBreak()}
        """.trimIndent())

    }

    private fun createClickStyle(action: ClickEvent.Action?, value: String): ChatStyle {
        val style = ChatStyle()
        style.chatClickEvent = ClickEvent(action, value)
        style.chatHoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            ChatComponentText(EnumChatFormatting.YELLOW.toString() + value)
        )
        return style
    }
}