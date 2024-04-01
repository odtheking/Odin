package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.monster.EntityCreeper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Ghosts : Module(
    name = "Ghosts",
    description = "Diverse QOL for ghosts in the Dwarven Mines.",
    category = Category.SKYBLOCK
) {
    private var showGhostNametag: Boolean by BooleanSetting(name = "Show Ghost Nametag")
    private var showGhosts: Boolean by BooleanSetting(name = "Hide Ghosts")
    private var hideChargedLayer: Boolean by BooleanSetting(name = "Hide Charged Layer")

    init {
        execute(500) {
            mc.theWorld.loadedEntityList
                .filterIsInstance<EntityCreeper>()
                .filter { entityCreeper -> entityCreeper.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue >= 1000000 }
                .forEach { creeper ->
                    creeper.isInvisible = showGhosts
                    creeper.dataWatcher.updateObject(17, (if (hideChargedLayer) 0 else 1).toByte())

                    if (showGhostNametag) drawGhostNameTag(creeper)
                }
        }
    }

    private fun drawGhostNameTag(creeper: EntityCreeper) {
        val currentHealth = creeper.health
        val maxHealth = creeper.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue
        val isRunic = maxHealth == 4000000.0
        val bracketsColor = if (isRunic) "&5" else "&8"
        val lvlColor = if (isRunic) "&d" else "&7"
        val nameColor = if (isRunic) "&5" else "&c"
        val currentHealthColor = if (isRunic) "&d" else if (currentHealth < maxHealth / 2) "&e" else "&a"
        val maxHealthColor = if (isRunic) "&5" else "&a"
        val name = "${bracketsColor}[${lvlColor}Lv250${bracketsColor}] ${nameColor + if (isRunic) "Runic " else ""}Ghost ${currentHealthColor + transformToSuffixedNumber(currentHealth.toDouble()) + "&f"}/${maxHealthColor + transformToSuffixedNumber(maxHealth) + "&c" + "❤"}".replace("&", "§")

        Renderer.drawStringInWorld(name, creeper.renderVec.addVec(y = creeper.height + 0.5), Color.WHITE, depth = false)
    }

    private fun transformToSuffixedNumber(number: Double): String {
        val result: String = if (number >= 1000000) {
            val short = (number / 1000000).toString()
            val shortSplit = short.split(".")
            (if (shortSplit[1] != "0") short else shortSplit[0]) + "M"
        } else {
            (number / 1000).toInt().toString() + "k"
        }
        return result
    }
}