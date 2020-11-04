package net

import db.User
import db.Users
import db.asyncTransaction
import db.register
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import java.sql.SQLException

fun HTML.index() {
    head {
        title("Record Server")
    }
    body {
        div {
            h1 {
                +"This website is cursed"
            }
        }
    }
}

fun Routing.registerRoutes() {
    authenticate {
        get("/api/v1/user") {
            val principal: UserIdPrincipal? = call.authentication.principal()
            if (principal == null) {
                call.response.status(HttpStatusCode.Unauthorized)
            } else {
                val user = asyncTransaction {
                    User.find { Users.login eq principal.name }.first()
                }
                call.respond(HttpStatusCode.OK, user.username)
            }
        }
    }
    post("/api/v1/user") {
        try {
            val data = call.receive<RegistrationData>()
            val user = User.register(data.username, data.login, data.password)
            call.respond(HttpStatusCode.Created, user.id.value.toString())
        } catch (e: SQLException) {
            call.respond(HttpStatusCode.Conflict, "Duplicate credentials")
        }
    }
    get("/") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }
}
