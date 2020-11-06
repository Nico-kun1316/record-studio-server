package net.data

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationData(val username: String, val login: String, val password: String)
