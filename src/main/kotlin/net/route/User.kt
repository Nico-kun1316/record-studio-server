package net.route

import db.User
import db.asyncTransaction
import db.register
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import misc.toUUID
import net.AuthorizationException
import net.NotFoundException
import net.data.*
import net.privilegedUser
import net.user
import java.sql.SQLException
import java.util.*

fun Route.createUser() = post("users") {
    try {
        val data = call.receive<RegistrationData>()
        val user = User.register(data.username, data.login, data.password)
        call.respond(HttpStatusCode.Created, IdData(user.id.value))
    } catch (e: SQLException) {
        call.respond(HttpStatusCode.Conflict, "Duplicate user")
    }
}

fun Route.fetchUser() = get("users/{id}") {
    val author = call.user()
    val idString = call.parameters["id"]
    val user = if (idString == "@me")
        author
    else
        asyncTransaction { User.findById(idString.toUUID()) ?: throw NotFoundException("User doesn't exist") }
    call.respond(UserData(user.username, user.discriminator, user.id.value, user.role))
}

fun Route.fetchUsers() = get("users") {
    val page = call.parameters.page
    val users = asyncTransaction {
        User.paged(page).map { UserData(it.username, it.discriminator, it.id.value, it.role) }
    }
    call.respond(users)
}

fun Route.deleteUser() = delete("users/{id}") {
    val subject = call.privilegedUser()
    val id = UUID.fromString(call.parameters["id"])
    asyncTransaction {
        val user = User.findById(id) ?: throw NotFoundException("User doesn't exist")
        if (user.role < subject.role) {
            user.delete()
        } else {
            throw AuthorizationException("Insufficient privileges to delete user")
        }
    }

    call.response.status(HttpStatusCode.NoContent)
}
