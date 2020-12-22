package net.data

import kotlinx.serialization.Serializable
import db.Roles

@Serializable
data class UserPatchData(
    val role: Roles,
    val username: String,
)
