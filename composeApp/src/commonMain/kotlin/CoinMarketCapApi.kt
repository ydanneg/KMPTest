import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class GitHubUser(
    val login: String,
    val id: Long,
    val name: String,
    val company: String?
)

@Serializable
data class CryptocurrencyListingStatus(
    @SerialName("error_code")
    val errorCode: Int? = null,
    @SerialName("error_message")
    val errorMessage: String? = null
)

@Serializable
data class CryptocurrencyListingResponse(
    val data: List<Cryptocurrency>,
    val status: CryptocurrencyListingStatus
)

@Serializable
data class Cryptocurrency(
    val id: Int,
    val name: String,
    val symbol: String,
    val slug: String,
    val quote: Map<String, Quote>
)

@Serializable
data class Quote(
    val price: Double
)

class CoinMarketCapApi {

    private fun client() = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }

        defaultRequest {
            accept(ContentType.Application.Json)
            header("X-CMC_PRO_API_KEY", "8888bf31-bad8-4d09-84c9-728fd8ae7fb7")
//            header("X-CMC_PRO_API_KEY", "b54bcf4d-1bca-4e8e-9a24-22ff2c3d462c")
        }
    }

    suspend fun getListings(): CryptocurrencyListingResponse = withContext(Dispatchers.IO) {
        client().use {
            it.get("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest") {
//            it.get("https://sandbox-api.coinmarketcap.com/v1/cryptocurrency/listings/latest") {
                parameter("start", 1)
                parameter("limit", 100)
                parameter("convert", "USD")
            }.body()
        }
    }

    suspend fun getInfo(): GitHubUser {
        delay(3000)
        return client().use {
            it.get("https://api.github.com/users/ydanneg").body()
        }
    }
}