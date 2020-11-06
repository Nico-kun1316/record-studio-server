package net.data

import kotlinx.serialization.Serializable
import net.UUIDSerializer
import java.util.*

@Serializable
data class PublicUserData(
        val username: String,
        val discriminator: Short,
        @Serializable(with = UUIDSerializer::class)
        val id: UUID
)