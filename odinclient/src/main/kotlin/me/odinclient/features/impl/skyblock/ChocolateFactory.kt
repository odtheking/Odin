package me.odinclient.features.impl.skyblock

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.lore
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemSkull
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.common.util.Constants
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*


object ChocolateFactory : Module(
    "Chocolate Factory",
    description = "Automatically clicks the cookie in the Chocolate Factory menu.",
    category = Category.SKYBLOCK
) {
    private val clickFactory: Boolean by BooleanSetting("Click Factory", false, description = "Click the cookie in the Chocolate Factory menu.")
    private val autoUpgrade: Boolean by BooleanSetting("Auto Upgrade", false, description = "Automatically upgrade the worker.")
    private val delay: Long by NumberSetting("Delay", 150, 50, 300, 5)
    private val upgradeDelay: Long by NumberSetting("Upgrade delay", 500, 300, 2000, 100)
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")
    private val upgradeMessage: Boolean by BooleanSetting("Odin Upgrade Message", false, description = "Prints a message when upgrading.")
    private val eggEsp: Boolean by BooleanSetting("Egg ESP", false, description = "Shows the location of the egg.")
    private var chocolate = 0
    private var chocoProduction = 0f

    val indexToName = mapOf(29 to "Bro", 30 to "Cousin", 31 to "Sis", 32 to "Daddy", 33 to "Granny")
    private val EGG_TEXTURES = arrayOf(
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0"
    )
    private var eggList: ArrayList<EntityArmorStand> = ArrayList()


    init {
        onWorldLoad { eggList.clear() }
        execute(delay = { delay }) {
            val container = mc.thePlayer?.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            if (clickFactory) windowClick(13, 1,0)
        }

        execute(delay = {upgradeDelay}) {
            val container = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            val choco = container.getSlot(13)?.stack ?: return@execute

            chocolate = choco.displayName.noControlCodes.replace(Regex("\\D"), "").toInt()
            chocoProduction = choco.lore.find { it.endsWith("§8per second") }?.noControlCodes?.replace(",", "")?.toFloatOrNull() ?: 0f

            findWorker(container)
            if(!found) return@execute
            if (chocolate > bestCost && autoUpgrade) {
                windowClick(bestWorker, 2, 3)
                if(upgradeMessage) modMessage("Trying to upgrade: Rabbit " + indexToName[bestWorker] + " with " + bestCost + " chocolate.")
            }
        }
    }

    private var bestWorker = 29
    private var bestCost = 0
    private var found = false

    private fun findWorker(container: Container) {
        val items = container.inventory ?: return
        val workers = mutableListOf<List<String?>>()
        for (i in 29 until 34) {
            workers.add(items[i]?.lore ?: return)
        }
        found = false
        var maxValue = 0;
        for (i in 0 until 5) {
            val worker = workers[i]
            if (worker.contains("climbed as far")) continue
            val index = worker.indexOfFirst { it?.contains("Cost") == true }.takeIf { it != -1} ?: continue
            val cost = worker[index + 1]?.noControlCodes?.replace(Regex("\\D"), "")?.toIntOrNull() ?: continue
            val value = cost / (i + 1).toFloat()
            if (value < maxValue || !found){
                bestWorker = 29 + i
                maxValue = value.toInt()
                bestCost = cost
                found = true
            }
        }
    }

    @SubscribeEvent
    fun guiLoad(event: GuiEvent.GuiLoadedEvent) {
        if (event.name == "Chocolate Factory") findWorker(event.gui)
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound) return
        if (event.name == "random.eat") event.result = null // This should cancel the sound event
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (mc.theWorld == null) return
        if(!eggEsp) {
            eggList.clear()
            return
        }
        val entityArmorStands = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
        if (event.phase == TickEvent.Phase.END) return

        for (stand in entityArmorStands) {
            if(matchSkullTexture(stand, *EGG_TEXTURES)){
                if(stand !in eggList) eggList.add(stand)
                if(eggList.size > 3) eggList.clear()
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (eggList.isEmpty()) return
        for(egg in eggList){
            var location = Vec3(egg.posX - 0.5, egg.posY + 1.47, egg.posZ - .5)
            Renderer.drawCustomBeacon("§6Egg§r", location, Color.GREEN, increase = true, beacon = false)

        }
    }

    /*
     * stuff from GumTuneClient
     * https://github.com/RoseGoldIsntGay/GumTuneClient/blob/main/src/main/java/rosegold/gumtuneclient/modules/render/ESPs.java#L307
     */
    private fun matchSkullTexture(entity: EntityArmorStand, vararg skullTextures: String): Boolean {
        val helmetItemStack = entity.getCurrentArmor(3)
        if (helmetItemStack != null && helmetItemStack.item is ItemSkull) {
            val textures = helmetItemStack.serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner")
                .getCompoundTag("Properties").getTagList("textures", Constants.NBT.TAG_COMPOUND)
            for (i in 0 until textures.tagCount()) {
                val finalI = i
                if (Arrays.stream(skullTextures).anyMatch { s ->
                        textures.getCompoundTagAt(finalI).getString("Value") == s
                    }) {
                    return true
                }
            }
        }

        return false
    }
}