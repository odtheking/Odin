package me.odinmain.utils

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import me.odinmain.features.impl.render.DevPlayers
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

private var IMGUR_KEYS = arrayOf(
    "d30c6dc9941b52b",
    "b2e8519cbb7712a",
    "eb1f61e23b9eabd",
    "d1275dca5af8904",
    "ed46361ccd67d6d"
)
/**
 * Sends a POST request to a specified server URL with the provided request body.
 *
 * @param body The content of the request body to be sent to the server.
 * @param url The URL of the server to which the request will be sent. Defaults to a predefined URL.
 *
 * @returns The response code from the server (can be used as a bad get request)
 */
suspend fun sendDataToServer(body: String, url: String = "https://gi2wsqbyse6tnfhqakbnq6f2su0vujgz.lambda-url.eu-north-1.on.aws/"): String {
    return try {
        val connection = withContext(Dispatchers.IO) {
            URL(url).openConnection()
        } as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        val writer = OutputStreamWriter(connection.outputStream)
        withContext(Dispatchers.IO) {
            writer.write(body)
        }
        withContext(Dispatchers.IO) {
            writer.flush()
        }

        val responseCode = connection.responseCode
        if (DevPlayers.isDev) println("Response Code: $responseCode")

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }
        if (DevPlayers.isDev) println("Response: $response")

        connection.disconnect()

        response
    } catch (_: Exception) { "" }
}

/**
 * Fetches data from a specified URL and returns it as a string.
 *
 * @param url The URL from which to fetch data.
 * @return A string containing the data fetched from the URL, or an empty string in case of an exception.
 */
suspend fun getDataFromServer(url: String): String {
    return try {
        val connection = withContext(Dispatchers.IO) {
            URL(url).openConnection()
        } as HttpURLConnection
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        if (DevPlayers.isDev) println("Response Code: $responseCode")
        if (responseCode != 200) return ""
        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }
        if (DevPlayers.isDev) println("Response: $response")

        connection.disconnect()

        response
    } catch (_: Exception) { "" }
}

/**
 * Fetches data from a specified URL and returns it as a string.
 *
 * @param url The URL from which to fetch data.
 * @return A string containing the data fetched from the URL, or an empty string in case of an exception.
 */
fun fetchURLData(url: String): String {
    try {
        // Open a connection to the specified URL
        val connection = URL(url).openConnection()

        // Set the user agent to emulate a web browser
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
        )

        // Read the content from the input stream and build a string
        val inputStream = connection.getInputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            content.append(line)
        }

        // Close the reader and return the content as a string
        reader.close()
        return content.toString()
    } catch (e: Exception) {
        // Print the stack trace in case of an exception and return an empty string
        e.printStackTrace()
        return ""
    }
}

fun downloadFile(url: String, outputPath: String) {
    val url = URL(url)
    val connection = url.openConnection()
    connection.connect()

    val inputStream = connection.getInputStream()
    val outputStream = FileOutputStream(outputPath)

    val buffer = ByteArray(1024)
    var bytesRead: Int

    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }

    outputStream.close()
    inputStream.close()
}

/**
 * Uploads an image to Imgur using a random client ID from a predefined list.
 *
 * @param image The image data to be uploaded.
 * @return The response from the Imgur API as a string, or an empty string in case of an exception.
 */
fun upload(image: String): String {
    try {
        val clientID = IMGUR_KEYS.random() // Assuming IMGUR_KEYS is defined somewhere with the list of client IDs

        // Create a map with the image URL
        val dataMap = mapOf("image" to image)

        // Convert the map to JSON string using Gson
        val gson = Gson()
        val postData = gson.toJson(dataMap)

        // Create a URL object for the Imgur API endpoint
        val url = URL("https://api.imgur.com/3/image")

        // Open a connection to the Imgur API endpoint
        val connection = url.openConnection() as HttpURLConnection

        // Set the request method to POST
        connection.requestMethod = "POST"

        // Set the necessary request headers
        connection.setRequestProperty("Authorization", "Client-ID $clientID")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Content-Length", postData.length.toString())

        // Enable output and send the request data
        connection.doOutput = true
        val outputStream = connection.outputStream
        outputStream.write(postData.toByteArray())
        outputStream.close()

        // Read the response from the server
        val responseCode = connection.responseCode
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()

        // Close the connection
        connection.disconnect()

        // Return the response as a string
        return response.toString()
    } catch (e: Exception) {
        // Return an empty string in case of an exception
        println("Error: ${e.message}")
        return ""
    }
}

/**
 * Extracts the Imgur ID of an image from the provided URL.
 *
 * @param url The URL of the image.
 * @return The Imgur ID as a string, or an empty string if the ID extraction fails or an exception occurs.
 */
fun imgurID(url: String): String {
    // Fetch data from the provided URL
    val image: Any = fetchURLData(url)

    // Split the fetched data to extract the image URL
    val imageArray = image.toString().split(",")[1]
    // Upload the image to Imgur and extract the Imgur ID from the response
    val imageLink = upload(imageArray.drop(7).dropLast(1))
    val imgurID = imageLink.split(",")[26].substringAfter("i.imgur.com/").substringBefore(".")
    // Extract the Imgur ID from the Imgur API response
    return imgurID
}

fun fetch(uri: String): String? {
    HttpClients.createMinimal().use {
        try {
            val httpGet = HttpGet(uri)
            return EntityUtils.toString(it.execute(httpGet).entity)
        } catch (e: Exception) {
            return null
        }
    }
}

fun hasBonusPaulScore(): Boolean = runBlocking {
    val response: String = fetch("https://api.hypixel.net/resources/skyblock/election").toString()
    val jsonObject = Json.parseToJsonElement(response).jsonObject
    val mayor = jsonObject["mayor"]?.jsonObject ?: return@runBlocking false
    val name = mayor["name"]?.jsonPrimitive?.content ?: return@runBlocking false
    if (name == "Paul") {
        return@runBlocking mayor["perks"]?.jsonArray?.any {
            it.jsonObject["name"]?.jsonPrimitive?.content == "EZPZ"
        } ?: false
    }
    false
}
