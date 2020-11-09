package net

import io.ktor.http.*

sealed class StatusException(
        val status: HttpStatusCode,
        message: String? = null,
        cause: Throwable? = null
) : RuntimeException(message, cause)

class AuthorizationException(
        message: String? = null,
        cause: Throwable? = null
) : StatusException(HttpStatusCode.Forbidden, message, cause)

class AuthenticationException(
        message: String? = null,
        cause: Throwable? = null
) : StatusException(HttpStatusCode.Unauthorized, message, cause)

class NotFoundException(
        message: String? = null,
        cause: Throwable? = null
) : StatusException(HttpStatusCode.NotFound, message, cause)
