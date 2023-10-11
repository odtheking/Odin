package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.VecUtils.addVec
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.monster.EntityCreeper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Ghosts : Module(
    name = "Ghosts",
    description = "Diverse QOL for ghosts in the Dwarven Mines.",
    category = Category.SKYBLOCK,
    tag = TagType.NEW
) {

    private var showGhosts: Boolean by BooleanSetting(name = "Show Ghosts")
    private var showGhostNametag: Boolean by BooleanSetting(name = "Show Ghost Nametag")
    private var hideChargedLayer: Boolean by BooleanSetting(name = "Hide Charged Layer")

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent)
    {
        val creepers = mc.theWorld.loadedEntityList.filterIsInstance<EntityCreeper>().filter { entityCreeper -> entityCreeper.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue >= 1000000 }

        for (creeper in creepers)
        {
            creeper.isInvisible = !showGhosts

            val chargedGhostLayerWatcher: Byte = creeper.dataWatcher.getWatchableObjectByte(17)
            if (hideChargedLayer)
            {
                if (chargedGhostLayerWatcher == 1.toByte()) creeper.dataWatcher.updateObject(17, 0.toByte())
            }
            else
            {
                if (chargedGhostLayerWatcher == 0.toByte()) creeper.dataWatcher.updateObject(17, 1.toByte())
            }

            if (showGhostNametag)
            {
                drawGhostNameTag(creeper)
            }
        }
    }

    private fun drawGhostNameTag(creeper: EntityCreeper) {
        val currentHealth = creeper.health
        val maxHealth = creeper.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue
        val isRunic = maxHealth == 4000000.0
        var bracketsColor = "&8"
        var lvlColor = "&7"
        var nameColor = "&c"
        var currentHealthColor = if (currentHealth < maxHealth / 2) "&e" else "&a"
        var maxHealthColor = "&a"
        if (isRunic) {
            bracketsColor = "&5"
            lvlColor = "&d"
            nameColor = "&5"
            currentHealthColor = "&d"
            maxHealthColor = "&5"
        }
        val name = "${bracketsColor}[${lvlColor}Lv250${bracketsColor}] ${nameColor + if (isRunic) "Runic " else ""}Ghost ${currentHealthColor + transformToSuffixedNumber(currentHealth.toDouble()) + "&f"}/${maxHealthColor + transformToSuffixedNumber(maxHealth) + "&c" + "❤"}".replace("&", "§")
        RenderUtils.drawStringInWorld(
            name,
            creeper.renderVec.addVec(y = creeper.height + 0.5),
            0,
            renderBlackBox = true,
            increase = false,
            depthTest = false,
            0.016666668f * 1.6f,
        )
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