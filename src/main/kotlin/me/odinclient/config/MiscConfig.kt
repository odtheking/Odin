package me.odinclient.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odinclient.ModCore.Companion.scope
import me.odinclient.config.utils.ConfigFile
import java.io.IOException

object MiscConfig {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val espConfigFile = ConfigFile("esp-config")
    private val blacklistConfigFile = ConfigFile("blacklist-config")
    private val autoSellConfigFile = ConfigFile("autoSell-config")

    var espList: MutableList<String> = mutableListOf()
    var blacklist: MutableList<String> = mutableListOf()
    var autoSell: MutableList<String> = mutableListOf()

    fun loadConfig() {
        try {
            with(espConfigFile.bufferedReader().use { it.readText() }) {
                if (this != "") {
                    espList = gson.fromJson(this, object : TypeToken<MutableList<String>>() {}.type)
                }
            }
            with(blacklistConfigFile.bufferedReader().use { it.readText() }) {
                if (this != "") {
                    blacklist = gson.fromJson(this, object : TypeToken<MutableList<String>>() {}.type)
                }
            }
            with(autoSellConfigFile.bufferedReader().use { it.readText() }) {
                if (this != "") {
                    autoSell = gson.fromJson(this, object : TypeToken<MutableList<String>>() {}.type)
                }
            }
        } catch (e: JsonSyntaxException) {
            println("Error parsing configs.\n${e.message}")
        } catch (e: JsonIOException) {
            println("Error reading configs.\n${e.message}")
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
            } catch (e: IOException) {
                println("Error saving configs.\n${e.message}")
            }
        }
    }
}
