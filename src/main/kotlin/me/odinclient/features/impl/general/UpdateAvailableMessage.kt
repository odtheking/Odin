package me.odinclient.features.impl.general

import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.odinclient.OdinClient.Companion.VERSION
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.utils.AsyncUtils
import me.odinclient.utils.WebUtils
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object UpdateAvailableMessage {
    private var hasSentMessage = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) = scope.launch {
        if (hasSentMessage) return@launch

        val newestVersion = try {
            Json.parseToJsonElement(WebUtils.fetchURLData("https://api.github.com/repos/odtheking/OdinClient/releases/latest"))
        } catch (e: Exception) { return@launch }

        val link = newestVersion.jsonObject["html_url"].toString().replace("\"", "")
        val tag = newestVersion.jsonObject["tag_name"].toString().replace("\"", "")
        var vers = try { tag.toFloat() } catch (e: Exception) { 0f }
        var curVers = try { VERSION.replace(".", "").toFloat() } catch (e: Exception) { 0f }
        if (vers > 1000f) vers /= 1000f
        if (curVers > 1000f) curVers /= 1000f

        if (vers > curVers) {
            hasSentMessage = true

            val def = AsyncUtils.waitUntilPlayer()
            try { def.await() } catch (e: Exception) { return@launch }

            mc.thePlayer.addChatMessage(
                ChatComponentText("§3Odin§bClient §8»§r §7Update available! §r${newestVersion.jsonObject["tag_name"].toString()} §e$link")
                    .setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.OPEN_URL, link))
            )
        }
    }
}