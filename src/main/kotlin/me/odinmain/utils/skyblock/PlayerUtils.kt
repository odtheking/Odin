package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGUI
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PlayerUtils {
    var shouldBypassVolume = false

    /**
     * Plays a sound at a specified volume and pitch, bypassing the default volume setting.
     *
     * @param sound The identifier of the sound to be played.
     * @param volume The volume at which the sound should be played.
     * @param pitch The pitch at which the sound should be played.
     *
     * @author Aton
     */
    fun playLoudSound(sound: String?, volume: Float, pitch: Float, pos: Vec3? = null) {
        mc.addScheduledTask {
            shouldBypassVolume = true
            mc.theWorld?.playSound(pos?.xCoord ?: posX, pos?.yCoord ?: posY, pos?.zCoord  ?: posZ, sound, volume, pitch, false)
            shouldBypassVolume = false
        }
    }

    /**
     * Displays an alert on screen and plays a sound
     *
     * @param title String to be displayed.
     * @param playSound Toggle for sound.
     *
     * @author Odtheking, Bonsai
     */
    fun alert(title: String, time: Int = 20, color: Color = Colors.WHITE, playSound: Boolean = true, displayText: Boolean = true) {
        if (playSound) playLoudSound("note.pling", 100f, 1f)
        if (displayText) Renderer.displayTitle(title , time, color = color)
    }

    inline val posX get() = mc.thePlayer?.posX ?: 0.0
    inline val posY get() = mc.thePlayer?.posY ?: 0.0
    inline val posZ get() = mc.thePlayer?.posZ ?: 0.0

    fun getPositionString(): String {
        val blockPos = BlockPos(posX, posY, posZ)
        return "x: ${blockPos.x}, y: ${blockPos.y}, z: ${blockPos.z}"
    }

    private var lastClickSent = 0L

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (event.packet !is C0EPacketClickWindow) return
        //modMessage(System.currentTimeMillis() - lastClickSent)
        lastClickSent = System.currentTimeMillis()
    }

    fun windowClick(slotId: Int, button: Int, mode: Int) {
        if (lastClickSent + 45 > System.currentTimeMillis()) return devMessage("§cIgnoring click on slot §9$slotId.")
        mc.thePlayer?.openContainer?.let {
            if (slotId !in 0 until it.inventorySlots.size) return
            if (mc.currentScreen is TermSimGUI) {
                PacketEvent.Send(C0EPacketClickWindow(-2, slotId, button, mode, it.inventorySlots[slotId].stack, 0)).postAndCatch()
                return
            }
            mc.playerController?.windowClick(it.windowId, slotId, button, mode, mc.thePlayer)
            //mc.netHandler?.networkManager?.sendPacket(C0EPacketClickWindow(it.windowId, slotId, button, mode, it.inventory[slotId], it.getNextTransactionID(mc.thePlayer?.inventory)))
        }
    }

    fun windowClick(slotId: Int, clickType: ClickType) {
        when (clickType) {
            is ClickType.Left -> windowClick(slotId, 0, 0)
            is ClickType.Right -> windowClick(slotId, 1, 0)
            is ClickType.Middle -> windowClick(slotId, 2, 3)
            is ClickType.Shift -> windowClick(slotId, 0, 1)
        }
    }
}

sealed class ClickType {
    data object Left   : ClickType()
    data object Right  : ClickType()
    data object Middle : ClickType()
    data object Shift  : ClickType()
}
