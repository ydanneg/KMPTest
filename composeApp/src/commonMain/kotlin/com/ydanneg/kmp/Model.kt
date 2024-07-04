package com.ydanneg.kmp

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ydanneg.kmp.ui.QuoteCurrency
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey val id: Int,
    val symbol: String,
    val name: String,
    val rank: Int
)

@Serializable
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val currencyId: Int,
    val currency: QuoteCurrency,
    val price: Double,
    val change1h: Double,
    val change24h: Double,
    val change7d: Double
)

data class CurrencyWithQuotes(
    @Embedded
    val currency: CurrencyEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "currencyId"
    )
    val quotes: List<QuoteEntity>
)
