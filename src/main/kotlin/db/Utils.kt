package db

import RNG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

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
    val salt = RNG.nextBytes(saltLength)
    return hashPassword(pass, salt) to salt
}

suspend fun <T> asyncTransaction(block: Transaction.() -> T): T {
    return withContext(Dispatchers.IO) {
        transaction {
            block()
        }
    }
}

inline fun <reified T: Enum<T>> Table.enumeration(name: String): Column<T> = enumeration(name, T::class)
