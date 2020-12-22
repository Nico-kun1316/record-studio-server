package net.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class RecordCreationData(
        val name: String,

        @Serializable(with = BigDecimalSerializer::class)
        val price: BigDecimal,

        @Serializable(with = LocalDateSerializer::class)
        val releasedOn: LocalDate
)
