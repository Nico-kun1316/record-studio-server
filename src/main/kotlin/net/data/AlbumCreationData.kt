package net.data

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class AlbumCreationData(
    val name: String,
    @Serializable(with = LocalDateSerializer::class)
    val releasedOn: LocalDate
)
