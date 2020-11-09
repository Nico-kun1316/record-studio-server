package net.data

import kotlinx.serialization.Serializable
import net.UUIDSerializer
import java.util.*

@Serializable
data class AuthorData(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val name: String
)
