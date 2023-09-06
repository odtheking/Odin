package me.odinclient.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinclient.OdinClient.Companion.scope
import me.odinclient.features.impl.floor7.p3.TerminalTimes
import java.io.File
import java.io.IOException

class MiscConfig(path: File) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val espConfigFile = File(path, "esp-config.json")
    private val blacklistConfigFile = File(path, "blacklist-config.json")
    private val autoSellConfigFile = File(path, "autoSell-config.json")
    private val terminalPBFile = File(path, "terminalPB.json")

    var espList: MutableList<String> = mutableListOf()
    var blacklist: MutableList<String> = mutableListOf()
    var autoSell: MutableList<String> = mutableListOf()
    private inline val terminalPB get() = TerminalTimes.Times.values().map { "${it.fullName}: ${it.time}"} // todo: change this into a number setting and make it hidden.

    init {
        try {
            if (!path.exists()) path.mkdirs()
            espConfigFile.createNewFile()
            blacklistConfigFile.createNewFile()
            autoSellConfigFile.createNewFile()
            terminalPBFile.createNewFile()
        } catch (e: Exception) {
            println("Error initializing configs.")
        }
    }


    fun loadConfig() {
        try {
            with(espConfigFile.bufferedReader().use { it.readText() }) {
                if (this == "") return
                espList = gson.fromJson(this, object : TypeToken<MutableList<String>>() {}.type)
            }
            with(blacklistConfigFile.bufferedReader().use { it.readText() }) {
                if (this == "") return
                blacklist = gson.fromJson(this, object : TypeToken<MutableList<String>>() {}.type)
            }
            with(autoSellConfigFile.bufferedReader().use { it.readText() }) {
                if (this == "") return
                autoSell = gson.fromJson(this, object : TypeToken<MutableList<String>>() {}.type)
            }
            with(terminalPBFile.bufferedReader().use { it.readText() }) {
                if (this == "") return
                val a: MutableList<String> = gson.fromJson(this, object : TypeToken<MutableList<String>>() {}.type)
                a.forEach {
                    val (name, time) = it.split(": ")
                    TerminalTimes.Times.values().find { a -> a.fullName == name }?.let { b ->
                        b.time = time.toDouble()
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            println("Error parsing configs.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading configs.")
        }
    }

    fun saveAllConfigs() {
        scope.launch(Dispatchers.IO) {
            try {
                espConfigFile.bufferedWriter().use {
                    it.write(gson.toJson(espList))
                }
                blacklistConfigFile.bufferedWriter().use {
                    it.write(gson.toJson(blacklist))
                }
                autoSellConfigFile.bufferedWriter().use {
                    it.write(gson.toJson(autoSell))
                }
                terminalPBFile.bufferedWriter().use {
                    it.write(gson.toJson(terminalPB))
                }
            } catch (e: IOException) {
                println("Error saving configs.")
            }
        }
    }
}