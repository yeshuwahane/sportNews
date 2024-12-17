package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class NewsModel(
    @SerialName("results")
    val results: List<NewsResult>
)

@Serializable
data class NewsResult(
    @SerialName("title")
    val title: String,
    @SerialName("link")
    val link: String,
//    @SerialName("creator")
//    val creator: List<String>? = null, // creator can be null
    @SerialName("description")
    val description: String? = null, // description can be null
    @SerialName("pubDate")
    val pubDate: String,
    @SerialName("image_url")
    val imageUrl: String? = null, // image_url can be null
    @SerialName("source_name")
    val sourceName: String? = null // source_name added
)



suspend fun fetchNews(apiKey: String, query: String) {
    val client = HttpClient(CIO)
    val apiUrl = "https://newsdata.io/api/1/news"
    val freeApiUrl = "https://newsdata.io/api/1/latest"
    val language = "en"
    val category = "sports"
    val image = "1"


    try {
        // 1. Make API Request
        val response: HttpResponse = client.get(apiUrl) {
            parameter("apikey", apiKey)
            parameter("q", query)
            parameter("language", language)
            parameter("category", category)
            parameter("image", image)


        }

        val responseText = response.bodyAsText()

        // 2. Check for Successful Response
        if (response.status == HttpStatusCode.OK) {
            // Parse JSON Response
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse = json.decodeFromString<NewsModel>(responseText)

            println("Fetched ${apiResponse.results.size} articles.")

            // Save to local file
            saveJsonToFile(apiResponse, "newsdata.json")

            // 3. Print Each Article
            apiResponse.results.forEach { article ->
                println("\nTitle: ${article.title}")
                println("Link: ${article.link}")
//                println("Creator: ${article.creator?.joinToString(", ") ?: "No creator"}")
                println("Published Date: ${article.pubDate}")
                println("Image URL: ${article.imageUrl ?: "No image"}")
                println("Source: ${article.sourceName ?: "No source"}")
                println("Description: ${article.description ?: "No description"}")
            }

            println("numbers of article: ${apiResponse.results.size}")

        } else {
            println("Error: ${response.status}. Response: $responseText")
        }
    } catch (e: Exception) {
        println("An error occurred: ${e.message}")
    } finally {
        client.close()
    }
}

fun saveJsonToFile(apiResponse: NewsModel, fileName: String) {
    val json = Json { prettyPrint = true }
    val jsonString = json.encodeToString(apiResponse)

    val file = File(fileName)
    file.writeText(jsonString)
    println("JSON data saved to $fileName")
}

fun main() {
    val apiKey = "pub_625158243f2444fa9e205cbf0c54c918a55b8"
    val query = "kerala blasters"

    // Use Coroutine for API Call
    runBlocking {
        fetchNews(apiKey, query)
    }
}


