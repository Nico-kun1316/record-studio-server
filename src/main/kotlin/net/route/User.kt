package net.route

import db.User
import db.asyncTransaction
import db.register
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import net.AuthorizationException
import net.NotFoundException
import net.data.UserData
import net.data.RegistrationData
import net.data.page
import net.data.paged
import net.privilegedUser
import net.user
import java.sql.SQLException
import java.util.*

fun Route.createUser() = post("user") {
    try {
        val data = call.receive<RegistrationData>()
        val user = User.register(data.username, data.login, data.password)
        val response = UserData(user.username, user.discriminator, user.id.value)
        call.respond(HttpStatusCode.Created, response)
    } catch (e: SQLException) {
        call.respond(HttpStatusCode.Conflict, "Duplicate user")
    }
}

fun Route.fetchUser() = get("user/{id?}") {
    val author = call.user()
    val idString = call.parameters["id"]
    val id = UUID.fromString(idString)
    val user = if (idString == null)
        author
    else
        asyncTransaction { User.findById(id) ?: throw NotFoundException("User doesn't exist") }
    call.respond(UserData(user.username, user.discriminator, user.id.value))
}

fun Route.fetchUsers() = get("users") {
    val page = call.parameters.page
    val users = asyncTransaction {
        User.paged(page).map { UserData(it.username, it.discriminator, it.id.value) }
    }
    call.respond(users)
}

fun Route.deleteUser() = delete("user/{id}") {
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
