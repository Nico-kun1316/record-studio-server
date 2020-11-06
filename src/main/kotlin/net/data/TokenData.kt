package net.data

import kotlinx.serialization.Serializable

@Serializable
data class TokenData(val token: String, val refreshToken: String)
