package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoReady : Module(
    name = "Auto Ready",
    description = "Automatically ready up in dungeons",
    category = Category.DUNGEON
) {

    private var tped = false
    private var click = false
    private var playerReady = false

    private val autoMort: Boolean by BooleanSetting("Auto Mort")

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        tped = false
        click = false
        playerReady = false
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (autoMort || !DungeonUtils.inDungeons || playerReady || click) return
        val mort = mc.theWorld?.loadedEntityList?.find { it.name.contains("Mort") } as? EntityLivingBase ?: return
        val dist = mc.thePlayer.getDistanceToEntity(mort)
        if (dist <= 5) {
            PlayerUtils.interactWithEntity(mort)
            click = true

            GlobalScope.launch {
                delay(4000L)
                if (playerReady) return@launch
                mc.thePlayer.closeScreen()
                PlayerUtils.interactWithEntity(mort)
            }
        }
        if (tped) return
        PlayerUtils.useItem("Aspect of the Void")
        tped = true

    }

    @SubscribeEvent
    fun playerReady(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        if (message == "${mc.thePlayer.name} is now ready!") {
            playerReady = true
            mc.thePlayer.closeScreen()
        }
    }

    @SubscribeEvent
    fun autoReady(event: GuiOpenEvent) {
        if (playerReady || !DungeonUtils.inDungeons) return

        PlayerUtils.clickItemInContainer("Start Dungeon?", "Start Dungeon?", event, true)
        PlayerUtils.clickItemInContainer("Catacombs -", mc.thePlayer?.name.toString(), event, true)

    }
}