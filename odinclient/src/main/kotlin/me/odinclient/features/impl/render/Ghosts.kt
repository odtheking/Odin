package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.getSBMaxHealth
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.monster.EntityCreeper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Ghosts : Module(
    name = "Ghosts",
    description = "Diverse QOL for ghosts in the Dwarven Mines.",
    category = Category.SKYBLOCK
) {
    private var showGhostNametag by BooleanSetting(name = "Show Ghost Nametag", description = "Show the ghost's name tag.")
    private var showGhosts by BooleanSetting(name = "Hide Ghosts", description = "Hide ghosts.")
    private var hideChargedLayer by BooleanSetting(name = "Hide Charged Layer", description = "Hide the charged layer of the ghost.")

    private var creeperList = mutableSetOf<EntityCreeper>()

    init {
        execute(500) {
            creeperList.clear()
             mc.theWorld?.loadedEntityList?.forEach { entity ->
                 if (entity !is EntityCreeper || entity.getSBMaxHealth() < 1000000) return@forEach
                 entity.isInvisible = showGhosts
                 entity.dataWatcher.updateObject(17, (if (hideChargedLayer) 0 else 1).toByte())
                 creeperList.add(entity)
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        creeperList.forEach { entity ->
            if (entity.isDead) return@forEach
            if (showGhostNametag) drawGhostNameTag(entity)
        }
    }

    private fun drawGhostNameTag(creeper: EntityCreeper) {
        val isRunic = creeper.getSBMaxHealth() == 4000000f
        val bracketsColor = if (isRunic) "&5" else "&8"
        val lvlColor = if (isRunic) "&d" else "&7"
        val nameColor = if (isRunic) "&5" else "&c"
        val currentHealthColor = if (isRunic) "&d" else if ( creeper.health < creeper.getSBMaxHealth() / 2) "&e" else "&a"
        val maxHealthColor = if (isRunic) "&5" else "&a"
        val name = "${bracketsColor}[${lvlColor}Lv250${bracketsColor}] ${nameColor + if (isRunic) "Runic " else ""}Ghost ${currentHealthColor + transformToSuffixedNumber(creeper.health) + "&f"}/${maxHealthColor + transformToSuffixedNumber(creeper.getSBMaxHealth()) + "&c" + "❤"}".replace("&", "§")

        Renderer.drawStringInWorld(name, creeper.renderVec.addVec(y = creeper.height + 0.5), Color.WHITE, depth = false)
    }

    private fun transformToSuffixedNumber(number: Float): String {
        val result = if (number >= 1000000) {
            val short = (number / 1000000).toString()
            val shortSplit = short.split(".")
            if (shortSplit[1] != "0") short else shortSplit[0] + "M"
        } else (number / 1000).toInt().toString() + "k"
        return result
    }
}