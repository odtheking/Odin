package me.odinmain.utils

import kotlinx.coroutines.launch
import me.odinmain.OdinMain.scope
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

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
 */
fun sendDataToServer(body: String, url: String = "https://ginkwsma75wud3rylqlqms5n240xyomv.lambda-url.eu-north-1.on.aws/") {
    scope.launch {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(body)
            writer.flush()

            val responseCode = connection.responseCode
            println("Response Code: $responseCode")

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }
            println("Response: $response")

            connection.disconnect()
        } catch (_: Exception) { }
    }
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

/**
 * Uploads an image to Imgur using a random client ID from a predefined list.
 *
 * @param image The image data to be uploaded.
 * @return The response from the Imgur API as a string, or an empty string in case of an exception.
 */
fun upload(image: String): String {
    try {
        // Get a random Imgur client ID
        val clientID = IMGUR_KEYS.random()

        // Create a POST request to upload the image to Imgur
        val url = URL("https://api.imgur.com/3/image")
        val con = url.openConnection() as HttpsURLConnection
        con.addRequestProperty("Content-Type", "application/json")
        con.addRequestProperty("Authorization", "Client-ID $clientID")
        con.doOutput = true
        con.requestMethod = "POST"

        // Write the image data to the request body
        val stream = con.outputStream
        stream.write(image.toByteArray())
        stream.flush()
        stream.close()

        // Make the POST request and read the response
        val inputStream = con.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readLine()

        // Close the connection
        con.disconnect()

        // Return the response
        return response
    } catch (e: Exception) {
        // Return an empty string in case of an exception
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
    val imageLink = upload(imageArray.substring(imageArray.indexOf('"') + 1, imageArray.lastIndexOf('"')).drop(6))

    // Extract the Imgur ID from the Imgur API response
    return imageLink.split(",")[27].drop(31).dropLast(6)
}
