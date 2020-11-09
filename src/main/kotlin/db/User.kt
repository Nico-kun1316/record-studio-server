package db

import RNG
import nextShort
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.and
import java.util.*

enum class Roles(val dbName: String) {
    USER("user"), ADMIN("admin"), OWNER("owner")
}

object Users: UUIDTable() {
    val login = varchar("login", 32).uniqueIndex()
    val password = binary("password", 128)
    val username = varchar("username", 32)
    val discriminator = short("discriminator")
    val salt = binary("salt", 32)
    val role = enumeration<Roles>("role")

    init {
        index(isUnique = true, username, discriminator)
    }
}

class User(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<User>(Users)

    var username by Users.username
    var discriminator by Users.discriminator
    var login by Users.login
    var password by Users.password
    var salt by Users.salt
    var role by Users.role
    override fun toString() = "(uname = $username; login = $login; $id = ${id.value})"
}

suspend fun User.Companion.register(username: String, login: String, password: String): User {
    val (pass, salt) = hashPassword(password)
    return asyncTransaction {
        val count = User.all().count()
        val role = if (count == 0L) Roles.OWNER else Roles.USER
        var exists = true
        var discriminator = RNG.nextShort(0..9999)
        while (exists) {
            discriminator = RNG.nextShort(0..9999)
            exists = !User.find {
                (Users.username eq username) and (Users.discriminator eq discriminator)
            }.empty()
        }

        User.new {
            this.username = username
            this.discriminator = discriminator
            this.password = pass
            this.login = login
            this.salt = salt
            this.role = role
        }
    }
}

suspend fun User.Companion.validateCredentials(login: String, password: String): Boolean {
    val user = asyncTransaction {
        User.find { Users.login eq login }.firstOrNull()
    } ?: return false
    val hash = hashPassword(password, user.salt)
    return hash.contentEquals(user.password)
}
