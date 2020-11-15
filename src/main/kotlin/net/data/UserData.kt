package net.data

import db.Roles
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserData(
        val username: String,
        val discriminator: Short,
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val role: Roles
)
