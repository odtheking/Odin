package me.odinmain

import kotlinx.serialization.json.*
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.updateMessage
import me.odinmain.ui.OdinGuiButton
import me.odinmain.utils.downloadFile
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.render.*
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.io.File
import java.lang.management.ManagementFactory

object OdinUpdater: GuiScreen() {

    private val logoTexture = DynamicTexture(RenderUtils.loadBufferedImage("/assets/odinmain/logo.png"))
    private val javaRuntime = "\"${System.getProperty("java.home")}${File.separatorChar}bin${File.separatorChar}javaw${if (System.getProperty("os.name").contains("win")) ".exe" else ""}\""
    private val javaUpdateGuide = ChatComponentText(null).setChatStyle(ChatStyle().setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/ChatTriggers/ChatTriggers/wiki/Fixing-broken-imports")))

    private var tag = ""
    private var isNewer = false
    private var isOutdatedJava = false
    private var scaleFactor = 1

    @SubscribeEvent()
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui !is GuiMainMenu || isNewer) return

        // To prevent this in the future do the TrustManager thing and add a X509 cert to access github in jre 51
        val javaVersion = System.getProperty("java.version")
        if (javaVersion == "1.8.0_51") {
            isOutdatedJava = true
        }

        val tags = try {
            Json.parseToJsonElement(fetchURLData("https://api.github.com/repos/odtheking/OdinClient/tags"))
        } catch (e: Exception) {
            return
        }
        tag = tags.jsonArray[0].jsonObject["name"].toString().replace("\"", "")

        isNewer = this.isSecondNewer(tag)

        if (isNewer)
            OdinMain.display = this@OdinUpdater
    }

    override fun initGui() {
        // add discord link also maybe
        // add link to / or changelog maybe
        // add a warning that updating will restart the game
        this.scaleFactor = ScaledResolution(mc).scaleFactor
        if (isOutdatedJava) {
            this.buttonList.add(OdinGuiButton(2, mc.displayWidth / 2 - 175, mc.displayHeight - 500, 350, 80, "Update Java Guide", 24f))
            this.buttonList.add(OdinGuiButton(0, mc.displayWidth / 2 - 60, mc.displayHeight - 100, 120, 50, "Close", 20f))
        } else {
            this.buttonList.add(OdinGuiButton(0, mc.displayWidth / 2 - 60, mc.displayHeight - 100, 120, 50, "Later", 20f))
            this.buttonList.add(OdinGuiButton(1, mc.displayWidth / 2 - 100, mc.displayHeight - 300, 200, 70, "Update", 24f))
        }
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawBackground(0)
        GlStateManager.pushMatrix()
        GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1f)
        this.drawLogo()
        if (isOutdatedJava) {
            text("You are using an outdated version of java (${System.getProperty("java.version")}) which does not allow the auto updater to work properly", mc.displayWidth / 2f, 500f, Color.RED, 18f, 0, TextAlign.Middle, TextPos.Middle, false)
        } else {
            text("A new version of ${if (OdinMain.isLegitVersion) "Odin" else "OdinClient"} is available!", mc.displayWidth / 2f, 450f, Color.WHITE, 18f, 0, TextAlign.Middle, TextPos.Middle, false)
            text("§fNewest: §r$tag   §fCurrent: §r${OdinMain.VERSION}", mc.displayWidth / 2f - getTextWidth("Newest: $tag   Current: ${OdinMain.VERSION}", 18f) / 2, 500f, ClickGUI.oldColor, 18f, 0, TextAlign.Left, TextPos.Middle, false)
        }
        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
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
                    val newJar = "${if (OdinMain.isLegitVersion) "odin" else "odinclient"}-$tag.jar"
                    val newDownloadUrl = "https://github.com/odtheking/OdinClient/releases/download/$tag/$newJar"
                    val newVersionPath = "${mc.mcDataDir}${File.separatorChar}mods${File.separatorChar}$newJar"
                    downloadFile(newDownloadUrl, newVersionPath)

                    val currentJarPath = "${mc.mcDataDir}${File.separatorChar}mods${File.separatorChar}${if (OdinMain.isLegitVersion) "odin" else "odinclient"}-${OdinMain.VERSION}.jar"
                    val updaterUrl = "https://github.com/odtheking/OdinUpdater/releases/download/OdinUpdater/OdinUpdater.jar"
                    val updaterPath = "${System.getProperty("java.io.tmpdir")}${File.separatorChar}OdinUpdater.jar"
                    downloadFile(updaterUrl, updaterPath)

                    val relaunchCommand: String = getRelaunchCommand()
                    val relaunchCommandDir = "${System.getProperty("java.io.tmpdir")}${File.separatorChar}odinRelaunchCommand.txt"
                    val relaunchCommandFile = File(relaunchCommandDir)
                    if (!relaunchCommandFile.exists()) relaunchCommandFile.createNewFile()
                    relaunchCommandFile.writeText(relaunchCommand, charset("UTF-8"))

                    Runtime.getRuntime().exec("$javaRuntime -jar $updaterPath \"$currentJarPath\" \"${relaunchCommandDir}\"")
                })
                mc.shutdown()
            }
            2 -> {
                this.handleComponentClick(javaUpdateGuide)
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
        GlStateManager.translate(mc.displayWidth / 2f - 384, 0f, 0f)
        GlStateManager.scale(0.4f, 0.4f, 1f)
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
