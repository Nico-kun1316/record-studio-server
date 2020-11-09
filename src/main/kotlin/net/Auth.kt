package net

import RNG
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import db.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import net.Claims.*
import java.util.*

val secret = RNG.nextBytes(32)
val algorithm: Algorithm = Algorithm.HMAC512(secret)
private const val issuer = "Record Studio"

private enum class TokenType {
    TOKEN, REFRESH_TOKEN
}

private enum class Claims {
    ID, TYPE, ROLE
}

private fun getIdFromToken(
        credential: JWTCredential,
        requiredType: TokenType,
        elevated: Boolean = false
): UUID? {

    val claims = credential.payload.claims
    val roleString = claims[ROLE.name]?.asString()
    val typeString = claims[TYPE.name]?.asString()
    val idString = claims[ID.name]?.asString()

    if (roleString == null || typeString == null || idString == null)
        return null

    val type = TokenType.valueOf(typeString)
    val role = Roles.valueOf(roleString)

    val expired = credential.payload.expiresAt.before(Date())
    val permitted = if (elevated) role >= Roles.ADMIN else true

    return if (!expired && type == requiredType && permitted)
        UUID.fromString(idString)
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
    jwt("admin") {
        verifier(getVerifier())
        validate { credential ->
            getIdFromToken(credential, TokenType.TOKEN, true)?.let { UserUUIDPrincipal(it) }
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

private fun createJWTToken(id: UUID, role: Roles, type: TokenType, daysTilExpiry: Int): String {
    val calendar = GregorianCalendar()
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_YEAR, daysTilExpiry)
    return JWT
            .create()
            .withIssuer(issuer)
            .withClaim(ID.name, id.toString())
            .withClaim(TYPE.name, type.name)
            .withClaim(ROLE.name, role.name)
            .withExpiresAt(calendar.time)
            .sign(algorithm)
}


fun generateTokensForUser(user: User): Pair<String, String> {
    val token = createJWTToken(user.id.value, user.role, TokenType.TOKEN, 1)
    val refreshToken = createJWTToken(user.id.value, user.role, TokenType.REFRESH_TOKEN, 7)
    return token to refreshToken
}

