package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.gui.Gui.drawRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.util.EnumChatFormatting
import kotlin.math.max

object Sidebar : Module(
    name = "Sidebar",
    desc = "Various settings to change the look of the minecraft sidebar."
) {
    private var variableScoreObjective: ScoreObjective? = null
    private val hud by HudSetting("Hud", 500f, 500f, 2f, false) {
        val scoreObjective = variableScoreObjective ?: return@HudSetting 0f to 0f
        val scoreboard: Scoreboard = scoreObjective.scoreboard
        val scoreList: MutableList<Score> = ArrayList()
        var width: Int = getStringWidth(scoreObjective.displayName)
        for (score in scoreboard.getSortedScores(scoreObjective)) {
            val name = score.playerName
            if (scoreList.size < 15 && name != null && !name.startsWith("#")) {
                val scorePlayerTeam = scoreboard.getPlayersTeam(name)
                val str = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, name) + (if (this.redNumbers) (": " + EnumChatFormatting.RED + score.scorePoints) else "")
                width = max(width, getStringWidth(str))
                scoreList.add(score)
            }
        }
        var index = 0
        val color: Int = backgroundColor.rgba
        for (score in scoreList.reversed()) {
            index++
            val team = scoreboard.getPlayersTeam(score.playerName)
            val s1 = ScorePlayerTeam.formatPlayerName(team, score.playerName)
            val s2 = "§c" + score.scorePoints
            val scoreY: Int = index * mc.fontRendererObj.FONT_HEIGHT
            drawRect(-2, scoreY, width, scoreY + mc.fontRendererObj.FONT_HEIGHT, color)
            drawString(s1, 0, scoreY)
            if (this.redNumbers) drawString(s2, width - getStringWidth(s2), scoreY)
            if (index == scoreList.size) {
                val s3 = scoreObjective.displayName
                drawRect(-2, 0, width, mc.fontRendererObj.FONT_HEIGHT, color)
                drawString(s3, width - getStringWidth(s3), 1)
            }
        }
        GlStateManager.resetColor()
        width.toFloat() to (scoreList.size + 1) * mc.fontRendererObj.FONT_HEIGHT.toFloat()
    }
    private val customFont by BooleanSetting("Custom Font", false, desc = "Whether to use a custom font for the sidebar.")
    private val textShadow by BooleanSetting("Text Shadow", true, desc = "Whether to render a shadow behind the text.")
    private val redNumbers by BooleanSetting("Show Red Numbers", true, desc = "Whether to show the numbers in red.")
    private val backgroundColor by ColorSetting("Background Color", Colors.MINECRAFT_GRAY.withAlpha(.5f), allowAlpha = true, desc = "The color of the sidebar background.")

    @JvmStatic
    fun renderSidebar(scoreObjective: ScoreObjective, scaledResolution: ScaledResolution): Boolean {
        variableScoreObjective = scoreObjective
        return this.enabled
    }

    private fun drawString(str: String, x: Int, y: Int) {
        if (customFont)
            text(str, x - 1, y + 3, Colors.WHITE, 7, shadow = textShadow)
        else
            RenderUtils.drawText(str, x.toFloat(), y.toFloat(), 1f, Colors.WHITE, shadow = textShadow, center = false)
    }

    private fun getStringWidth(str: String): Int {
        return if (customFont) (getTextWidth(str, 7f) * 1.5).toInt() else mc.fontRendererObj.getStringWidth(str)
    }
}