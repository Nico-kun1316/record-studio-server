package net.data

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class IdData(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)
