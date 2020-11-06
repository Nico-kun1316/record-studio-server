package net

import db.Roles
import db.User
import db.asyncTransaction
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import java.util.*

data class UserUUIDPrincipal(val id: UUID): Principal

fun ApplicationCall.UUIDOrNull(): UUID? = authentication.principal<UserUUIDPrincipal>()?.id
fun ApplicationCall.UUID(): UUID = UUIDOrNull() ?: throw AuthenticationException("Cannot fetch user UUID")

suspend fun ApplicationCall.userOrNull(): User? = UUIDOrNull()?.let { id ->
    asyncTransaction { User.findById(id) }
}

suspend fun ApplicationCall.user(): User = userOrNull() ?: throw AuthenticationException("Cannot fetch user")

suspend fun ApplicationCall.privilegedUser(): User {
    val user = user()
    if (user.role != Roles.ADMIN && user.role != Roles.OWNER)
        throw AuthorizationException("User lacks required permissions")
    else return user
}
