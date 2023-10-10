package me.odinmain.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object WebUtils {


    private var IMGUR_KEYS = arrayOf(
        "d30c6dc9941b52b",
        "b2e8519cbb7712a",
        "eb1f61e23b9eabd",
        "d1275dca5af8904",
        "ed46361ccd67d6d"
    )

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

    fun upload(image: String): String {
        // Get a random Imgur client ID.
        val clientID = IMGUR_KEYS.random()

        // Create a POST request to upload the image to Imgur.
        val url = URL("https://api.imgur.com/3/image")
        val con = url.openConnection() as HttpsURLConnection
        con.addRequestProperty("Content-Type", "application/json")
        con.addRequestProperty("Authorization", "Client-ID $clientID")
        con.doOutput = true
        con.requestMethod = "POST"

        // Write the image URL to the request body.
        val stream = con.outputStream
        stream.write(image.toByteArray())
        stream.flush()
        stream.close()

        // Make the POST request and read the response.
        val inputStream = con.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readLine()

        // Close the connection.
        con.disconnect()

        // Return the response.
        return response
    }

    fun imgurID(URL: String): String {
        val image: Any = fetchURLData(URL)

        val imageArray = image.toString().split(",")[1]

        val imageLink  = upload(imageArray.substring(imageArray.indexOf('"') + 1, imageArray.lastIndexOf('"')).drop(6))

        return imageLink.split(",")[27].drop(31).dropLast(6)
    }
}