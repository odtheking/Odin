package me.odinmain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.ClickGUIModule.updateMessage
import me.odinmain.font.OdinFont
import me.odinmain.ui.OdinGuiButton
import me.odinmain.utils.downloadFile
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.render.*
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.io.File
import java.lang.management.ManagementFactory


object OdinUpdater: GuiScreen() {

    private val logoTexture = DynamicTexture(RenderUtils.loadBufferedImage("/assets/odinmain/logo.png"))
    private val javaRuntime = "\"${System.getProperty("java.home")}${File.separatorChar}bin${File.separatorChar}javaw${if (System.getProperty("os.name").lowercase().contains("win")) ".exe" else ""}\""

    private var link = ""
    private var tag = ""
    private var isNewer = false
    private val seeChangelog: ChatComponentText = ChatComponentText("See Changelog")

    @SubscribeEvent()
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui !is GuiMainMenu || isNewer) return

        val tags = try {
            Json.parseToJsonElement(fetchURLData("https://api.github.com/repos/odtheking/OdinClient/tags"))
        } catch (e: Exception) {
            return
        }
        tag = tags.jsonArray[0].jsonObject["name"].toString().replace("\"", "")
        link = "https://github.com/odtheking/OdinClient/releases/tag/$tag"

        seeChangelog.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, link)

        isNewer = this.isSecondNewer(tag)

        //if (isNewer)
            //OdinMain.display = this@OdinUpdater
    }

    override fun initGui() {
        // add discord link also maybe
        this.buttonList.add(OdinGuiButton(0, width / 2 - 40, height - 50, 80, 20, "Later", 10f))
        this.buttonList.add(OdinGuiButton(1, width / 2 - 60, height - 130, 120, 20, "Update", 10f))
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawBackground(0)
        this.drawLogo()
        text("A new version of ${if (OdinMain.onLegitVersion) "Odin" else "OdinClient"} is available!", width / 2f, 250f, Color.WHITE, 14f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, false)
        text("§fNewest: §r$tag   §fCurrent: §r${OdinMain.VERSION}", width / 2f - getTextWidth("Newest: $tag   Current: ${OdinMain.VERSION}", 14f) / 2, 300f, ClickGUIModule.color, 14f, OdinFont.REGULAR, TextAlign.Left, TextPos.Middle, false)
        text(seeChangelog.chatComponentText_TextValue, width / 2f, 355f, Color.WHITE, 12f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, false)
        this.drawHorizontalLine(width / 2 - getTextWidth(seeChangelog.chatComponentText_TextValue, 12f).toInt() / 2 - 2, width / 2 + getTextWidth(seeChangelog.chatComponentText_TextValue, 12f).toInt() / 2, 363, Color.WHITE.rgba)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val onChangeLogX = mouseX in (width / 2 - getTextWidth(seeChangelog.unformattedText, 12f).toInt() / 2) .. (width / 2 + getTextWidth(seeChangelog.unformattedText, 12f).toInt() / 2)
        val onChangeLogY = mouseY in 340 .. 350 + getTextHeight(seeChangelog.chatComponentText_TextValue, 12f).toInt()
        if (onChangeLogX && onChangeLogY) {
            this.handleComponentClick(seeChangelog)
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton?) {
        if (button == null) return
        when (button.id) {
            0 -> {
                isNewer = true
                mc.displayGuiScreen(null)
            }
            1 -> {
                Runtime.getRuntime().addShutdownHook(Thread {
                    val newJar = "${if (OdinMain.onLegitVersion) "odin" else "odinclient"}-$tag.jar"
                    val newDownloadUrl = "https://github.com/odtheking/OdinClient/releases/download/$tag/$newJar"
                    val newVersionPath = "${mc.mcDataDir}${File.separatorChar}mods${File.separatorChar}$newJar"
                    downloadFile(newDownloadUrl, newVersionPath)

                    val currentJarPath = "${mc.mcDataDir}${File.separatorChar}mods${File.separatorChar}${if (OdinMain.onLegitVersion) "odin" else "odinclient"}-${OdinMain.VERSION}.jar"
                    val updaterUrl = "https://github.com/odtheking/OdinUpdater/releases/download/OdinUpdater/OdinUpdater.jar"
                    val updaterPath = "${System.getProperty("java.io.tmpdir")}${File.separatorChar}OdinUpdater.jar"
                    downloadFile(updaterUrl, updaterPath)

                    val relaunchCommand: String = getRelaunchCommand()
                    val relaunchCommandDir = "${System.getProperty("java.io.tmpdir")}${File.separatorChar}odinRelaunchCommand.txt"
                    val relaunchCommandFile = File(relaunchCommandDir)
                    if (!relaunchCommandFile.exists()) relaunchCommandFile.createNewFile()
                    relaunchCommandFile.writeText(relaunchCommand)

                    Runtime.getRuntime().exec("$javaRuntime -jar $updaterPath \"$currentJarPath\" \"${relaunchCommandDir}\"")
                })
                mc.shutdown()
            }
        }
        super.actionPerformed(button)
    }

    private fun getRelaunchCommand(): String {
        var command = javaRuntime
        for (inputArg in ManagementFactory.getRuntimeMXBean().inputArguments) {
            command += if (inputArg.contains("-Dos.name=")) " \"$inputArg\"" else " $inputArg"
        }
        command += " -cp ${ManagementFactory.getRuntimeMXBean().classPath} ${System.getProperty("sun.java.command")}"
        return command
    }

    private fun drawLogo() {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.translate(width / 2f - 192, 25f, 0f)
        GlStateManager.scale(0.2f, 0.2f, 1f)
        drawDynamicTexture(logoTexture, 0f, 0f, 1920f, 1080f)
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    private fun isSecondNewer(second: String?): Boolean {
        val currentVersion = OdinMain.VERSION
        if (currentVersion.isEmpty() || second.isNullOrEmpty()) return false // Handle null or empty strings appropriately

        val (major, minor, patch, beta) = currentVersion.split(".").mapNotNull { it.toIntOrNull() ?: if (it.startsWith("beta") && updateMessage == 1) it.substring(4).toIntOrNull() else 99 }.plus(listOf(99, 99, 99, 99))
        val (major2, minor2, patch2, beta2) = second.split(".").mapNotNull { it.toIntOrNull() ?: if (it.startsWith("beta")  && updateMessage == 1) it.substring(4).toIntOrNull() else 99 }.plus(listOf(99, 99, 99, 99))

        return when {
            major > major2 -> false
            major < major2 -> true
            minor > minor2 -> false
            minor < minor2 -> true
            patch > patch2 -> false
            patch < patch2 -> true
            beta > beta2 -> false
            beta < beta2 -> true
            else -> false // equal, or something went wrong, either way it's best to assume it's false.
        }
    }

}