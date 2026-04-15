package com.odtheking.odin.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object EnglishLanguage {

    private val map: Map<String, String> by lazy {
        val stream = EnglishLanguage::class.java.classLoader
            .getResourceAsStream("assets/minecraft/lang/en_us.json")
            ?: error("Missing en_us.json")

        stream.use {
            Gson().fromJson<Map<String, String>>(
                InputStreamReader(it),
                object : TypeToken<Map<String, String>>() {}.type
            )
        }
    }

    fun get(key: String): String = map[key] ?: key
}
