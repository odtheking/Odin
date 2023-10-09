package me.odinmain.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object WebUtils {
    fun fetchURLData(url: String): String {
        try {
            val connection = URL(url).openConnection()
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
            )

            val inputStream = connection.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                content.append(line)
            }

            reader.close()
            return content.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun sendDiscordWebhook(webhookUrl: String, title: String, message: String, color: Int) {
        var jsonBrut = ""
        jsonBrut += ("{\"embeds\": [{"
                + "\"title\": \"" + title + "\","
                + "\"description\": \"" + message + "\","
                + "\"color\": $color"
                + "}]}")
        try {
            val url = URL(webhookUrl)
            val con = url.openConnection() as HttpsURLConnection
            con.addRequestProperty("Content-Type", "application/json")
            con.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_")
            con.doOutput = true
            con.requestMethod = "POST"
            val stream = con.outputStream
            stream.write(jsonBrut.toByteArray())
            stream.flush()
            stream.close()
            con.inputStream.close()
            con.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
