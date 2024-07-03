import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CryptocurrencyListingStatus(
    @SerialName("error_code")
    val errorCode: Int? = null,
    @SerialName("error_message")
    val errorMessage: String? = null
)

@Serializable
data class CryptocurrencyListingResponse(
    val data: List<Cryptocurrency>?,
    val status: CryptocurrencyListingStatus
)

@Serializable
data class Cryptocurrency(
    val id: Int,
    val name: String,
    val symbol: String,
    @SerialName("cmc_rank")
    val cmcRank: Int,
    val quote: Map<String, Quote>
)

@Serializable
data class Quote(
    val price: Double
)

class CoinMarketCapApi {

    private fun client() = HttpClient() {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    println("<HTTP>")
                    println(message)
                    println("</HTTP>")
                }
            }
        }

        defaultRequest {
            accept(ContentType.Application.Json)
            header("X-CMC_PRO_API_KEY", "8888bf31-bad8-4d09-84c9-728fd8ae7fb7")
        }
    }

    suspend fun getListings(start: Int = 1, limit: Int = 100, priceCurrency: String = "USD"): CryptocurrencyListingResponse = withContext(Dispatchers.IO) {
        client().use {
            it.get("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest") {
                parameter("start", start)
                parameter("limit", limit)
                parameter("convert", priceCurrency)
            }.body()
        }
    }
}
