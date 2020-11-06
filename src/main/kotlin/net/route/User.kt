package net.route

import db.User
import db.asyncTransaction
import db.register
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import net.data.PublicUserData
import net.data.RegistrationData
import net.user
import java.sql.SQLException
import java.util.*

fun Route.createUser() = post("user") {
    try {
        val data = call.receive<RegistrationData>()
        val user = User.register(data.username, data.login, data.password)
        val response = PublicUserData(user.username, user.discriminator, user.id.value)
        call.respond(HttpStatusCode.Created, response)
    } catch (e: SQLException) {
        call.respond(HttpStatusCode.Conflict, "Duplicate user")
    }
}

fun Route.fetchUser() = get("user/{id?}") {
    val author = call.user()
    val id = call.parameters["id"]
    val user = if (id == null) author else asyncTransaction { User[UUID.fromString(id)] }
    call.respond(PublicUserData(user.username, user.discriminator, user.id.value))
}
