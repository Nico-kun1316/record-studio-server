package net.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Serializable
data class RecordData(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,

    @Serializable(with = UUIDSerializer::class)
    val albumId: UUID,

    val name: String,

    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,

    @Serializable(with = LocalDateSerializer::class)
    val releasedOn: LocalDate
)
