package me.odinclient.features.impl.skyblock

import cc.polyfrost.oneconfig.libs.universal.UScreen
import cc.polyfrost.oneconfig.utils.Multithreading
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.StringSetting
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object AutoDaed : Module(
    "Auto Daed Swap",
    category = Category.SKYBLOCK
) {

    private val daedSwapHealthType: Boolean by BooleanSetting("Health Detection Type")
    private val manualHealthDaed: String by StringSetting("Manual Boss Health", "1M", 10, description = "Select the health exchange amount to daed, for example: '10.2M'")

    private val customMobs = hashMapOf<Entity, EntityLivingBase>()
    private var bossActive = false
    private var maxBossHealth = -1.0
    private var dead = false
    private var switching = false
    private var lastHealth = -1.0
    private var switched = false
    @SubscribeEvent
    fun checkForBoss(event: RenderWorldLastEvent){
        if (UScreen.currentScreen.toString().contains("gg.essential.vigilance.gui.SettingsGui")) return
        val world = Minecraft.getMinecraft().theWorld
        val entityList = world.loadedEntityList
        for (entity in entityList){
            if (!entity.hasCustomName()) continue
            val name = entity.customNameTag.noControlCodes
            if (name.contains("ⓉⓎⓅⒽⓄⒺⓊⓈ") || name.contains("ⓆⓊⒶⓏⒾⒾ")) return
            if (name != "Spawned by: ${mc.thePlayer.name}") continue
            bossActive = true
            val mob = customMobs[entity]
            if (mob != null) {
                val healthNumber = if (mob.customNameTag.contains("Hit")) lastHealth
                else {
                    val health = mob.customNameTag.noControlCodes.split(" ").last().replace("❤", "").lowercase()
                    if (health == "0") 0.0 else when (health.drop(health.length - 1)) {
                        "k" -> health.dropLast(1).toDouble() * 1000
                        "m" -> health.dropLast(1).toDouble() * 1000000
                        else -> health.toDouble()
                    }
                }
                lastHealth = healthNumber
                if (lastHealth == -1.0) return
                if (healthNumber == 0.0) {
                    if (dead) return
                    Multithreading.runAsync {
                        Thread.sleep(1000)
                        switchBack()
                        slotWithMainWeapon = -1
                        dead = true
                        return@runAsync
                    }
                } else dead = false
                if (maxBossHealth < 0) maxBossHealth = healthNumber
                if (daedSwapHealthType) {
                    val healthNumberToSwapAt =
                        when (manualHealthDaed.drop(manualHealthDaed.length - 1).lowercase(Locale.getDefault())) {
                            "k" -> manualHealthDaed.dropLast(1).toDouble() * 1000
                            "m" -> manualHealthDaed.dropLast(1).toDouble() * 1000000
                            else -> manualHealthDaed.toDouble()
                        }
                    if (healthNumber <= healthNumberToSwapAt) {
                        val slotWithDead = lookForDead()
                        if (switching) return
                        when (slotWithDead) {
                            null -> {
                                if (System.currentTimeMillis()/1000 - lastTimeChecked < 5 && lastTimeChecked > 0) return
                                modMessage("Did not find the Daed")
                                lastTimeChecked = System.currentTimeMillis()/1000
                            }
                            -1 -> return
                            else -> {
                                switching = true
                                switchToDead(slotWithDead)
                                switching = false
                            }
                        }
                    }
                }
            } else {
                getMobsWithinAABB(entity)
            }
            return
        }
        if (bossActive) return
        bossActive = false
        maxBossHealth = -1.0
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent){
        val message = event.message.unformattedText
        if (message.contains("SLAYER QUEST COMPLETE!") || message.startsWith(" ☠ You")) bossActive = false
    }

    private fun getMobsWithinAABB(entity: Entity) {
        val aabb = AxisAlignedBB(entity.posX + 0.4, entity.posY - 2.0, entity.posZ + 0.4, entity.posX - 0.4, entity.posY + 0.2, entity.posZ - 0.4)
        val i = MathHelper.floor_double(aabb.minX - 1.0) shr 4
        val j = MathHelper.floor_double(aabb.maxX + 1.0) shr 4
        val k = MathHelper.floor_double(aabb.minZ - 1.0) shr 4
        val l = MathHelper.floor_double(aabb.maxZ + 1.0) shr 4
        for (i1 in i..j)
            for (j1 in k..l)
                getMobsWithinAABBForEntity(mc.theWorld.getChunkFromChunkCoords(i1, j1), entity, aabb)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load){
        lastTimeSwitched = -1
        customMobs.clear()
        bossActive = false
        switched = false
        lastTimeChecked = -1
        maxBossHealth = -1.0
    }


    private var lastTimeSwitched : Long = -1
    private var lastTimeChecked : Long = -1

    private fun lookForDead() : Int?{
        if (switching) return -1
        for (i in 0..7 ){
            val itemStack = mc.thePlayer.inventory.mainInventory[i] ?: continue
            if (itemStack.displayName.noControlCodes.contains("Daedalus Axe")){
                return i
            }
        }
        for (i in 9..35){
            val itemStack = mc.thePlayer.inventory.mainInventory[i] ?: continue
            if (!itemStack.displayName.noControlCodes.contains("Daedalus Axe")) continue
            return i
        }
        return null
    }

    private var slotWithMainWeapon : Int = -1

    private fun switchToDead(slotIndex: Int){
        if (System.currentTimeMillis()/1000 - lastTimeSwitched < 2) return
        lastTimeSwitched = System.currentTimeMillis()/1000
        if (slotIndex in 0..7){
            if (slotWithMainWeapon < 0) slotWithMainWeapon = mc.thePlayer.inventory.currentItem
            mc.thePlayer.inventory.currentItem = slotIndex
        } else {
            mc.displayGuiScreen(GuiInventory(mc.thePlayer))
            val windowId = GuiInventory(mc.thePlayer).inventorySlots.windowId
            var itemStack = mc.thePlayer.inventory.mainInventory[slotIndex]
            mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slotIndex,0,0,itemStack,0))
            itemStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem]
            mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, mc.thePlayer.inventory.currentItem + 36,0,0,itemStack,0))
            itemStack = mc.thePlayer.inventory.mainInventory[slotIndex]
            slotWithMainWeapon = slotIndex
            mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slotIndex,0,0,itemStack,0))
            mc.thePlayer.closeScreen()
        }
    }

    private fun switchBack() {
        if (slotWithMainWeapon == -1) return
        if (slotWithMainWeapon in 0..7){
            mc.thePlayer.inventory.currentItem = slotWithMainWeapon
        } else {
            mc.displayGuiScreen(GuiInventory(mc.thePlayer))
            val windowId = GuiInventory(mc.thePlayer).inventorySlots.windowId
            var itemStack = mc.thePlayer.inventory.mainInventory[slotWithMainWeapon]
            mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slotWithMainWeapon,0,0,itemStack,0))
            itemStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem]
            mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, mc.thePlayer.inventory.currentItem + 36,0,0,itemStack,0))
            itemStack = mc.thePlayer.inventory.mainInventory[slotWithMainWeapon]
            mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slotWithMainWeapon,0,0,itemStack,0))
            slotWithMainWeapon = -1
            mc.thePlayer.closeScreen()
        }
    }

    private fun getMobsWithinAABBForEntity(chunk: Chunk, entityIn: Entity, aabb: AxisAlignedBB) {
        val entityLists = chunk.entityLists
        var i = MathHelper.floor_double((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0)
        var j = MathHelper.floor_double((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0)
        i = MathHelper.clamp_int(i, 0, entityLists.size - 1)
        j = MathHelper.clamp_int(j, 0, entityLists.size - 1)
        for (k in i..j) {
            if (entityLists[k].isEmpty()) continue
            entity@ for (e in entityLists[k]) {
                if (!e.entityBoundingBox.intersectsWith(aabb)) continue@entity
                if (!e.hasCustomName()) continue@entity
                if (!e.customNameTag.noControlCodes.contains("☠")) continue@entity
                customMobs[entityIn] = e as EntityLivingBase
            }
        }
    }

}