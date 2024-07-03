import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Currency(
    @PrimaryKey val id: Int,
    val symbol: String,
    val name: String,
    val price: Double,
    val rank: Int,
    val btcPrice: Double
)
