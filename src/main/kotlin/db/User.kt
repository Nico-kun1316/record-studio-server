package db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

enum class Roles(val dbName: String) {
    ADMIN("admin"), OWNER("owner"), USER("user")
}

object Users: UUIDTable() {
    val username = varchar("username", 32).uniqueIndex()
    val login = varchar("login", 32).uniqueIndex()
    val password = binary("password", 128)
    val salt = binary("salt", 32)
    val role = varchar("role", 32)
}

class User(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<User>(Users)

    var username by Users.username
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
        User.new {
            this.username = username
            this.password = pass
            this.login = login
            this.salt = salt
            this.role = role.dbName
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
