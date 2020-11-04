package db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.random.asKotlinRandom

const val keyLength = 128
const val iterationCount = 1000
const val saltLength = 32

fun hashPassword(pass: String, salt: ByteArray): ByteArray {
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
    val spec = PBEKeySpec(pass.toCharArray(), salt, iterationCount, keyLength)
    val key = keyFactory.generateSecret(spec)
    return key.encoded
}

fun hashPassword(pass: String): Pair<ByteArray, ByteArray> {
    val rng = SecureRandom.getInstanceStrong().asKotlinRandom()
    val salt = rng.nextBytes(saltLength)
    return hashPassword(pass, salt) to salt
}

suspend fun <T> asyncTransaction(block: Transaction.() -> T): T {
    return withContext(Dispatchers.IO) {
        transaction {
            addLogger(StdOutSqlLogger)
            block()
        }
    }
}
