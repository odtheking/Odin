package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import java.io.File
import java.io.IOException

object PosMessagesConfig {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    var PosMessages: MutableList<PosMessage> = mutableListOf()
    data class PosMessage(val x: Double, val y: Double, val z: Double, val delay: Long, val message: String, var sent: Boolean = false)

    private val configFile = File(mc.mcDataDir, "config/odin/PosMessages-Config.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") return

                PosMessages = gson.fromJson(
                    this,
                    object : TypeToken<MutableList<PosMessage>>() {}.type
                )
            }
        }  catch (e: JsonSyntaxException) {
            println("Error parsing configs.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading configs.")
        }
    }

    fun saveConfig() {
        scope.launch(Dispatchers.IO) {
            try {
                configFile.bufferedWriter().use {
                    it.write(gson.toJson(PosMessages))
                }
            } catch (e: IOException) {
                println("Error saving Pos Message config.")
            }
        }
    }
}