package me.odinmain.features.impl.render


import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.ClickGUIModule.devSize
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.fetchURLData
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager

object DevPlayers {

    data class PlayerSize(val xScale: Float, val yScale: Float, val zScale: Float)

    val devs = HashMap<String, PlayerSize>()

    private fun updateDevs() {
        val webhook: String = fetchURLData("https://pastebin.com/raw/9Lq8hKTQ")

        val keyValuePairs = webhook.split("?")
        for (keyValuePair in keyValuePairs) {
            val parts = keyValuePair.split(" to ")

            if (parts.size == 2) {

                val key = parts[0].trim(' ', '"')
                val valueString = parts[1].trim()

                val regex = Regex("""PlayerSize\((\d+\.*\d*), (\d+\.*\d*), (\d+\.*\d*)\)""")
                val match = regex.find(valueString)

                if (match != null) {
                    val (x, y, z) = match.destructured
                    val playerSize = PlayerSize(x.toFloat(), y.toFloat(), z.toFloat())
                    devs[key] = playerSize
                }
            }
        }
    }

    init {
        Executor(delay = 15000) {
            OdinMain.scope.launch {
                updateDevs()
            }
        }.register()
    }

    fun preRenderCallbackScaleHook(entityLivingBaseIn: AbstractClientPlayer ) {
        if (!devs.containsKey(entityLivingBaseIn.name)) return
        if (!devSize && entityLivingBaseIn.name == mc.thePlayer.name) return
        val dev = devs[entityLivingBaseIn.name]
        if (dev != null) { GlStateManager.scale(dev.xScale, dev.yScale, dev.zScale) }
    }
}