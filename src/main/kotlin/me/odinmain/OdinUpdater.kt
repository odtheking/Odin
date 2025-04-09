package me.odinmain

import com.google.gson.Gson
import com.google.gson.JsonArray
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.downloadFile
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.drawDynamicTexture
import me.odinmain.utils.render.text
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.OdinGuiButton
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
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

object OdinUpdater : GuiScreen() {

    // Constants
    private const val GITHUB_TAGS_URL = "https://api.github.com/repos/odtheking/OdinClient/tags"
    private const val JAVA_UPDATE_GUIDE_URL = "https://github.com/ChatTriggers/ChatTriggers/wiki/Fixing-broken-imports"
    private const val LOGO_PATH = "/assets/odinmain/logo.png"
    private const val DEFAULT_SCALE_FACTOR = 1

    // Dynamic texture for the logo
    private val logoTexture = DynamicTexture(RenderUtils.loadBufferedImage(LOGO_PATH))

    // Java runtime path
    private val javaRuntime: String = buildString {
        append(System.getProperty("java.home"))
        append(File.separatorChar).append("bin").append(File.separatorChar).append("javaw")
        if (System.getProperty("os.name").contains("win", ignoreCase = true)) append(".exe")
    }

    // Variables
    private var latestTag: String = ""
    private var isNewerVersionAvailable = false
    private var isJavaOutdated = false
    private var scaleFactor = DEFAULT_SCALE_FACTOR

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui !is GuiMainMenu || isNewerVersionAvailable) return

        checkJavaVersion()
        fetchLatestVersionTag()?.let { tag ->
            latestTag = tag
            isNewerVersionAvailable = isVersionNewer(latestTag, OdinMain.VERSION)
        }

        //if (isNewerVersionAvailable) OdinMain.display = this
    }

    private fun checkJavaVersion() {
        isJavaOutdated = System.getProperty("java.version") == "1.8.0_51"
    }

    private fun fetchLatestVersionTag(): String? {
        return try {
            val tagsJson: JsonArray = Gson().fromJson(fetchURLData(GITHUB_TAGS_URL), JsonArray::class.java)
            tagsJson.firstOrNull()?.asJsonObject?.get("name")?.asString
        } catch (e: Exception) {
            println("Failed to fetch latest version tag: ${e.message}")
            null
        }
    }

    override fun initGui() {
        scaleFactor = ScaledResolution(mc).scaleFactor
        buttonList.clear()

        if (isJavaOutdated) {
            buttonList.add(createButton(2, "Update Java Guide", 350, 80, 24f))
            buttonList.add(createButton(0, "Close", 120, 50, 20f))
        } else {
            buttonList.add(createButton(0, "Later", 120, 50, 20f))
            buttonList.add(createButton(1, "Update", 200, 70, 24f))
        }

        super.initGui()
    }

    private fun createButton(id: Int, label: String, width: Int, height: Int, fontSize: Float): OdinGuiButton {
        val x = centerX - width / 2
        val y = if (id == 1) centerY - 100 else centerY + 100
        return OdinGuiButton(id, x, y, width, height, label, fontSize)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        GlStateManager.pushMatrix()
        GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1f)

        drawLogo()

        if (isJavaOutdated) {
            text(
                "Outdated Java (${System.getProperty("java.version")}). Update to fix issues.",
                centerX,
                450f,
                Colors.MINECRAFT_RED,
                18f
            )
        } else {
            text("New version available: $latestTag", centerX, 450f, Colors.WHITE, 18f)
            text(
                "Current: ${OdinMain.VERSION}",
                centerX,
                500f,
                ClickGUIModule.color,
                18f
            )
        }

        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawLogo() {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.translate(centerX - 384f, 0f, 0f)
        GlStateManager.scale(0.4f, 0.4f, 1f)
        drawDynamicTexture(logoTexture, 0f, 0f, 1920f, 1080f)
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    override fun actionPerformed(button: GuiButton?) {
        when (button?.id) {
            0 -> mc.displayGuiScreen(null) // Close or Later
            1 -> performUpdate() // Update
            2 -> openJavaUpdateGuide() // Java Update Guide
        }
        super.actionPerformed(button)
    }

    private fun openJavaUpdateGuide() {
        val updateGuide = ChatComponentText("").setChatStyle(ChatStyle().setChatClickEvent(
            ClickEvent(ClickEvent.Action.OPEN_URL, JAVA_UPDATE_GUIDE_URL)
        ))
        handleComponentClick(updateGuide)
    }

    private fun performUpdate() {
        Runtime.getRuntime().addShutdownHook(Thread {
            val versionType = if (!OdinMain.isLegitVersion) "Client" else ""
            val newJarName = "Odin$versionType-$latestTag.jar"
            val downloadUrl = "https://github.com/odtheking/OdinClient/releases/download/$latestTag/$newJarName"
            val destinationPath = "${mc.mcDataDir}/mods/$newJarName".replace('/', File.separatorChar)
            downloadFile(downloadUrl, destinationPath)

            val updaterUrl = "https://github.com/odtheking/OdinUpdater/releases/download/OdinUpdater/OdinUpdater.jar"
            val updaterPath = "${System.getProperty("java.io.tmpdir")}/OdinUpdater.jar".replace('/', File.separatorChar)
            downloadFile(updaterUrl, updaterPath)

            val relaunchCommand = buildRelaunchCommand()
            val relaunchFilePath = "${System.getProperty("java.io.tmpdir")}/odinRelaunchCommand.txt".replace('/', File.separatorChar)
            File(relaunchFilePath).writeText(relaunchCommand, Charsets.UTF_8)

            Runtime.getRuntime().exec("$javaRuntime -jar $updaterPath $destinationPath $relaunchFilePath")
        })

        mc.shutdown()
    }

    private fun buildRelaunchCommand(): String {
        return buildString {
            append(javaRuntime)
            ManagementFactory.getRuntimeMXBean().inputArguments.forEach { arg ->
                append(" ")
                append(if (arg.contains("-Dos.name=")) "\"$arg\"" else arg)
            }
            append(" -cp ").append(ManagementFactory.getRuntimeMXBean().classPath)
            append(" ").append(System.getProperty("sun.java.command"))
        }
    }

    private fun isVersionNewer(newVersion: String, currentVersion: String): Boolean {
        val newParts = newVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in newParts.indices) {
            val newPart = newParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }
            if (newPart > currentPart) return true
            if (newPart < currentPart) return false
        }

        return false
    }

    private val centerX: Int get() = mc.displayWidth / 2
    private val centerY: Int get() = mc.displayHeight / 2

}
