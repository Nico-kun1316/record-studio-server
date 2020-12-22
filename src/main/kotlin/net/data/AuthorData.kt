package net.data

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AuthorData(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val name: String,
        val genre: Genres
)
