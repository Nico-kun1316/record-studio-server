package net.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Serializable
data class AlbumData(
    val name: String,
    val genres: Set<Genres>,

    @Serializable(with = LocalDateSerializer::class)
    val releasedOn: LocalDate,

    @Serializable(with = LocalDateTimeSerializer::class)
    val addedOn: LocalDateTime,

    @Serializable(with = UUIDSerializer::class)
    val id: UUID,

    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID
)
