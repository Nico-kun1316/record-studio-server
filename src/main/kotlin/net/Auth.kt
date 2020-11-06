package net

import RNG
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import db.User
import db.Users
import db.asyncTransaction
import db.validateCredentials
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import net.Claims.ID
import net.Claims.TYPE
import java.util.*

val secret = RNG.nextBytes(32)
val algorithm: Algorithm = Algorithm.HMAC512(secret)
private const val issuer = "Record Studio"

private enum class TokenType {
    TOKEN, REFRESH_TOKEN
}

private enum class Claims {
    ID, TYPE
}

private fun getIdFromToken(credential: JWTCredential, requiredType: TokenType): UUID? {
    val idClaim = credential.payload.claims[ID.name]?.asString()
    val type = credential.payload.claims[TYPE.name]?.asString()
    val expired = credential.payload.expiresAt.before(Date())
    return if (!expired && idClaim != null && type == requiredType.name)
        UUID.fromString(idClaim)
    else
        null
}

private fun getVerifier() = JWT.require(algorithm).withIssuer(issuer).build()

fun Authentication.Configuration.registerAuth() {
    jwt {
        verifier(getVerifier())
        validate { credential ->
            getIdFromToken(credential, TokenType.TOKEN)?.let { UserUUIDPrincipal(it) }
        }
    }
    jwt("refresh") {
        verifier(getVerifier())
        validate { credential ->
            getIdFromToken(credential, TokenType.REFRESH_TOKEN)?.let { UserUUIDPrincipal(it) }
        }
    }
    basic("basic") {
        realm = "Record Studio"
        validate { credentials ->
            if (User.validateCredentials(credentials.name, credentials.password)) {
                val id = asyncTransaction {
                    User.find { Users.login eq credentials.name }.first().id.value
                }
                UserUUIDPrincipal(id)
            }
            else null
        }
    }
}

private fun createJWTToken(id: UUID, type: TokenType, daysTilExpiry: Int): String {
    val calendar = GregorianCalendar()
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_YEAR, daysTilExpiry)
    return JWT
            .create()
            .withIssuer(issuer)
            .withClaim(ID.name, id.toString())
            .withClaim(TYPE.name, type.name)
            .withExpiresAt(calendar.time)
            .sign(algorithm)
}


fun generateTokensForId(id: UUID): Pair<String, String> {
    val token = createJWTToken(id, TokenType.TOKEN, 1)
    val refreshToken = createJWTToken(id, TokenType.REFRESH_TOKEN, 7)
    return token to refreshToken
}

class AuthorizationException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

class AuthenticationException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
